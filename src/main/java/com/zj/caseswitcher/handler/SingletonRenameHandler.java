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
        ToggleState toggleState = CaseCache.getToggleState(cacheVo, selectedText);
        List<CaseModelEnum> allCaseModels = CaseUtils.getAllCaseModel(cacheVo.getOriginalCaseModelEnum());
        if (CaseModelSettings.getInstance().isRenameRelated()) {
            PsiNamedElement element = ElementUtils.getPsiNamedElement(project, editor, caretVo);
            if (Objects.nonNull(element)) {
                NamesValidator validator = LanguageNamesValidation.INSTANCE.forLanguage(element.getLanguage());
                CaseVo caseVo = CaseUtils.tryConvert(up, toggleState, allCaseModels, text -> validator.isIdentifier(text, project));
                if (tryRenameRelated(element, toggleState, project, dataContext, caseVo)) {
                    return;
                }
            }
        }
        CaseVo caseVo = CaseUtils.tryConvert(up, toggleState, allCaseModels);
        // 只改当前变量名
        singletonRename(caseVo, editor, project, toggleState, caret);
        logger.info("rename toggleState next: " + toggleState);
    }

    /**
     * 指定重命名
     */
    public static void rename(@NotNull CaretVo caretVo,
                              @NotNull ToggleState toggleState,
                              @NotNull Editor editor,
                              @NotNull Project project,
                              @NotNull DataContext dataContext,
                              @NotNull CaseVo caseVo,
                              @Nullable PsiNamedElement element) {
        Caret caret = caretVo.getCaret();
        String selectedText = caretVo.getSelectTest();
        if (StringUtils.isEmpty(selectedText)) {
            return;
        }
        if (CaseModelSettings.getInstance().isRenameRelated()) {
            if (Objects.nonNull(element)) {
                NamesValidator validator = LanguageNamesValidation.INSTANCE.forLanguage(element.getLanguage());
                if (validator.isIdentifier(caseVo.getAfterText(), project)) {
                    if (tryRenameRelated(element, toggleState, project, dataContext, caseVo)) {
                        return;
                    }
                } else {
                    HintManager.getInstance().showErrorHint(editor, "Invalid identifier: " + caseVo.getAfterText());
                }
            }
        }
        // 只改当前变量名
        singletonRename(caseVo, editor, project, toggleState, caret);
        logger.info("rename toggleState next: " + toggleState);
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
        Document document = editor.getDocument();
        try {
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.replaceString(caret.getSelectionStart(), caret.getSelectionEnd(), next)
            );
        } catch (ReadOnlyModificationException e) {
            HintManager.getInstance().showErrorHint(editor, "File is read-only");
            logger.error(e);
        }
        // 更新选择的文本
        toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
        toggleState.setSelectedText(next);

        if (toggleState.isRelated()) {
            HintManager.getInstance().showErrorHint(editor, "Same identifier not modified");
        }
    }

    /**
     * 尝试使用 RenameProcessor 更新引用
     */
    public static boolean tryRenameRelated(@Nullable PsiNamedElement element,
                                           @NotNull ToggleState toggleState,
                                           @NotNull Project project,
                                           @Nullable DataContext dataContext,
                                           @NotNull CaseVo caseVo) {
        if (Objects.isNull(dataContext)) {
            logger.info("tryRenameRelated dataContext is null");
            return false;
        }
        if (element == null) {
            logger.info("tryRenameRelated element is null");
            return false;
        }
        if (!RenameHandlerRegistry.getInstance().hasAvailableHandler(dataContext)) {
            logger.info("tryRenameRelated no rename handler available");
            return false;
        }
        try {
            if (!toggleState.getSelectedText().equals(element.getName())) {
                logger.info("tryRenameRelated name not equals");
                return false;
            }
            String next = caseVo.getAfterText();
            if (next.equals(toggleState.getSelectedText())) {
//                    HintManager.getInstance().showInformationHint(editor, "Identifier is invalid");
                logger.info("tryRenameRelated next is not a valid identifier");
                return true;
            }
            renameRelation(toggleState, project, element, caseVo);
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    private static void renameRelation(@NotNull ToggleState toggleState, @NotNull Project project,
                                       PsiNamedElement element, @NotNull CaseVo caseVo) {
        String next = caseVo.getAfterText();
        // 尝试使用 RenameProcessor 改名，其他使用这个变量的地方同步改名
        RenameProcessor renameProcessor = new RenameProcessor(project, element, next,
                new ProjectScopeImpl(project, FileIndexFacade.getInstance(project)),
                false, false);
        renameProcessor.run();
        // 更新选择的文本
        toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
        toggleState.setSelectedText(next);
        logger.info("tryRenameRelated toggleState next: " + toggleState);
        toggleState.setRelated(true);
    }
}
