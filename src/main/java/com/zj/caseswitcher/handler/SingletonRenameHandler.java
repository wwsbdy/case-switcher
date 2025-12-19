package com.zj.caseswitcher.handler;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ReadOnlyModificationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.ProjectScopeImpl;
import com.intellij.refactoring.rename.RenameHandlerRegistry;
import com.intellij.refactoring.rename.RenameProcessor;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.setting.CaseModelSettings;
import com.zj.caseswitcher.utils.CaseCache;
import com.zj.caseswitcher.utils.CaseUtils;
import com.zj.caseswitcher.utils.ElementUtils;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import com.zj.caseswitcher.vo.CaseVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/11/14
 */
public class SingletonRenameHandler {

    private static final Logger logger = Logger.getInstance(SingletonRenameHandler.class);

    /**
     * 自动重命名
     */
    public static void rename(boolean up,
                              @NotNull CaretVo caretVo,
                              @NotNull CacheVo cacheVo,
                              @NotNull Editor editor,
                              @NotNull Project project,
                              @Nullable DataContext dataContext) {
        Caret caret = caretVo.getCaret();
        String selectedText = caretVo.getSelectTest();
        if (StringUtils.isEmpty(selectedText)) {
            return;
        }
        
        ToggleState toggleState = CaseCache.getSingleToggleState(cacheVo, selectedText);
        List<CaseModelEnum> allCaseModels = CaseUtils.getAllCaseModel(cacheVo.getOriginalCaseModelEnum());
        PsiNamedElement element = ElementUtils.getPsiNamedElement(project, editor, caretVo);
        
        // 尝试关联重命名
        if (tryRelatedRename(up, toggleState, allCaseModels, element, project, dataContext, editor)) {
            return;
        }
        
        // 执行单选中重命名
        CaseVo caseVo = CaseUtils.tryConvert(up, toggleState, allCaseModels);
        singletonRename(caseVo, editor, project, toggleState, caret);
        
        // 处理只读元素提示
        if (Objects.nonNull(element) && ElementUtils.readOnly(editor, element)) {
            HintManager.getInstance().showInformationHint(editor, "Element is read-only");
            logger.info("rename: cannot rename related, element is read-only");
        }
        
        logger.info("rename: singleton rename completed, toggleState: " + toggleState);
    }

    /**
     * 尝试关联重命名
     */
    private static boolean tryRelatedRename(boolean up,
                                           ToggleState toggleState,
                                           List<CaseModelEnum> allCaseModels,
                                           PsiNamedElement element,
                                           Project project,
                                           DataContext dataContext,
                                           Editor editor) {
        if (CaseModelSettings.getInstance().isRenameRelated()
                && Objects.nonNull(element)
                && !ElementUtils.readOnly(editor, element)) {
            NamesValidator validator = LanguageNamesValidation.INSTANCE.forLanguage(element.getLanguage());
            CaseVo caseVo = CaseUtils.tryConvert(up, toggleState, allCaseModels, text -> validator.isIdentifier(text, project));
            
            if (tryRenameRelated(element, toggleState, project, dataContext, caseVo)) {
                logger.info("tryRelatedRename: related rename successful");
                return true;
            }
        }
        return false;
    }

    /**
     * 单选中重命名，只改当前变量名
     */
    public static void singletonRename(@NotNull CaseVo caseVo,
                                       @NotNull Editor editor,
                                       @NotNull Project project,
                                       @NotNull ToggleState toggleState,
                                       @NotNull Caret caret) {
        String next = caseVo.getAfterText();
        
        // 执行文档替换
        replaceDocumentText(project, editor, caret, next);
        
        // 更新转换状态
        updateToggleState(toggleState, caseVo);
        
        // 处理关联重命名状态
        if (toggleState.isRelated()) {
            HintManager.getInstance().showErrorHint(editor, "Same identifier not modified");
        }
    }
    
    /**
     * 执行文档替换
     */
    private static void replaceDocumentText(Project project, Editor editor, Caret caret, String newText) {
        Document document = editor.getDocument();
        try {
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.replaceString(caret.getSelectionStart(), caret.getSelectionEnd(), newText)
            );
        } catch (ReadOnlyModificationException e) {
            HintManager.getInstance().showInformationHint(editor, "File is read-only");
            logger.error("replaceDocumentText: cannot modify read-only file: " + e.getMessage());
        }
    }
    
    /**
     * 更新转换状态
     */
    private static void updateToggleState(ToggleState toggleState, CaseVo caseVo) {
        toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
        toggleState.setSelectedText(caseVo.getAfterText());
    }

    /**
     * 尝试使用 RenameProcessor 更新引用
     */
    public static boolean tryRenameRelated(@NotNull PsiNamedElement element,
                                           @NotNull ToggleState toggleState,
                                           @NotNull Project project,
                                           @Nullable DataContext dataContext,
                                           @NotNull CaseVo caseVo) {
        // 验证数据上下文
        if (Objects.isNull(dataContext)) {
            logger.info("tryRenameRelated: dataContext is null");
            return false;
        }
        
        // 验证重命名处理器是否可用
        if (!RenameHandlerRegistry.getInstance().hasAvailableHandler(dataContext)) {
            logger.info("tryRenameRelated: no rename handler available");
            return false;
        }
        
        try {
            // 验证当前选中文本与元素名称是否一致
            if (!toggleState.getSelectedText().equals(element.getName())) {
                logger.info("tryRenameRelated: name not equals");
                return false;
            }
            
            String next = caseVo.getAfterText();
            // 验证新名称是否与当前名称相同
            if (next.equals(toggleState.getSelectedText())) {
                logger.info("tryRenameRelated: next is not a valid identifier");
                return true;
            }
            
            // 执行关联重命名
            renameRelation(toggleState, project, element, caseVo);
            return true;
        } catch (Exception e) {
            logger.error("tryRenameRelated: " + e.getMessage());
        }
        return false;
    }

    /**
     * 执行关联重命名
     */
    private static void renameRelation(@NotNull ToggleState toggleState, @NotNull Project project,
                                       PsiNamedElement element, @NotNull CaseVo caseVo) {
        String next = caseVo.getAfterText();
        // 尝试使用 RenameProcessor 改名，其他使用这个变量的地方同步改名
        RenameProcessor renameProcessor = new RenameProcessor(project, element, next,
                new ProjectScopeImpl(project, FileIndexFacade.getInstance(project)),
                false, false);
        renameProcessor.run();
        
        // 更新选择的文本和状态
        toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
        toggleState.setSelectedText(next);
        toggleState.setRelated(true);
        
        logger.info("renameRelation: tryRenameRelated toggleState next: " + toggleState);
    }
}
