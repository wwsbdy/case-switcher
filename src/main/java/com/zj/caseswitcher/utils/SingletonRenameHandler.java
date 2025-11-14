package com.zj.caseswitcher.utils;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.CaretSpecificDataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.ProjectScopeImpl;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.setting.CaseModelSettings;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import com.zj.caseswitcher.vo.CaseVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.collections.CollectionUtils;
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

    public static void rename(boolean up,
                              @NotNull CaretVo caretVo,
                              @NotNull CacheVo cacheVo,
                              @NotNull Editor editor,
                              @NotNull Project project,
                              @NotNull DataContext dataContext) {
        Caret caret = caretVo.getCaret();
        String selectedText = caretVo.getSelectTest();
        if (StringUtils.isEmpty(selectedText)) {
            return;
        }
        List<ToggleState> toggleStateList = cacheVo.getToggleStateList();
        if (toggleStateList.size() != 1) {
            logger.info("singletonRename carets size is not equal to toggle list size");
            toggleStateList.clear();
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedText);
            toggleStateList.add(new ToggleState(selectedText, selectedText, caseModelEnum));
            cacheVo.setAllCaseModelEnums(CaseUtils.getAllCaseModel(up, caseModelEnum));
        }
        ToggleState toggleState = null;
        if (CollectionUtils.isNotEmpty(toggleStateList)) {
            toggleState = toggleStateList.get(0);
            // 当前选择的文本和原始文本不一致，重置
            if (!toggleState.getSelectedText().equals(selectedText)) {
                logger.info("singletonRename change orig:" + toggleState.getOriginalText() + " to " + selectedText);
                toggleState.setOriginalText(selectedText);
                toggleState.setSelectedText(selectedText);
                CaseModelEnum caseModel = CaseUtils.judgment(selectedText);
                toggleState.setCaseModelEnum(caseModel);
                cacheVo.setAllCaseModelEnums(CaseUtils.getAllCaseModel(up, caseModel));
            }
            logger.info("singletonRename toggleState: " + toggleState);
        }
        if (Objects.isNull(toggleState)) {
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedText);
            toggleState = new ToggleState(selectedText, selectedText, caseModelEnum);
        }
        // 尝试使用 RenameProcessor 改名，其他使用这个变量的地方同步改名
        if (CaseModelSettings.getInstance().isRenameRelated()
                && tryRenameRelated(up, toggleState, project, dataContext, editor, caretVo, cacheVo.getAllCaseModelEnums())) {
            return;
        }
        // 只改当前变量名
        singletonRename(up, editor, project, toggleState, caret, cacheVo.getAllCaseModelEnums());
        logger.info("singletonRename toggleState next: " + toggleState);
    }

    private static void singletonRename(boolean up,
                                        @NotNull Editor editor,
                                        @NotNull Project project,
                                        @NotNull ToggleState toggleState,
                                        @NotNull Caret caret,
                                        @NotNull List<CaseModelEnum> allCaseModelEnums) {
        CaseVo caseVo = CaseUtils.tryConvert(up, toggleState, allCaseModelEnums);
        String next = caseVo.getAfterText();
        Document document = editor.getDocument();
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.replaceString(caret.getSelectionStart(), caret.getSelectionEnd(), next)
        );
        // 更新选择的文本
        toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
        toggleState.setSelectedText(next);
    }

    /**
     * 尝试使用 RenameProcessor 更新引用
     */
    private static boolean tryRenameRelated(boolean up,
                                            @NotNull ToggleState toggleState,
                                            @NotNull Project project,
                                            @Nullable DataContext dataContext,
                                            @NotNull Editor editor,
                                            @NotNull CaretVo caretVo,
                                            @NotNull List<CaseModelEnum> allCaseModelEnums) {
        if (Objects.isNull(dataContext)) {
            return false;
        }
        try {
            // 如果是 PSI 命名元素，则走 RenameProcessor 更新引用
            PsiElement[] psiElementArray = CommonRefactoringUtil.getPsiElementArray(CaretSpecificDataContext.create(dataContext, caretVo.getCaret()));
            if (psiElementArray.length != 1) {
                logger.info("tryRenameRelated psiElementArray size{" + psiElementArray.length + "} is not equal to 1");
                return false;
            }
//            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
//            if (psiFile != null) {
//                PsiElement element = psiFile.findElementAt(caret.getOffset());
//            }
            PsiElement element = psiElementArray[0];
            PsiFile psiFile = element.getContainingFile();
            if (psiFile == null) {
                return false;
            }
            VirtualFile vFile = psiFile.getVirtualFile();
            if (vFile == null || !vFile.isWritable()) {
                singletonRename(up, editor, project, toggleState, caretVo.getCaret(), allCaseModelEnums);
                HintManager.getInstance().showInformationHint(editor, "File is read-only");
                logger.info("tryRenameRelated cannot modify read-only file");
                return true;
            }
            logger.info("tryRenameRelated element: " + element);
            PsiNamedElement named = findNamedElement(element);
            logger.info("tryRenameRelated named: " + named);
            if (Objects.isNull(named)) {
                return false;
            }
            if (!toggleState.getSelectedText().equals(named.getName())) {
                logger.info("tryRenameRelated name not equals");
                return false;
            }
            NamesValidator validator = LanguageNamesValidation.INSTANCE.forLanguage(element.getLanguage());
            // 验证命名是否有效
            CaseVo caseVo = CaseUtils.tryConvert(up, toggleState, allCaseModelEnums, text -> validator.isIdentifier(text, project));
            String next = caseVo.getAfterText();
            if (next.equals(toggleState.getSelectedText())) {
//                    HintManager.getInstance().showInformationHint(editor, "Identifier is invalid");
                logger.info("tryRenameRelated next is not a valid identifier");
                return true;
            }
            RenameProcessor renameProcessor = new RenameProcessor(project, named, next, new ProjectScopeImpl(project, FileIndexFacade.getInstance(project)), false, false);
            renameProcessor.run();
            // 更新选择的文本
            toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
            toggleState.setSelectedText(next);
            logger.info("singletonRename toggleState next: " + toggleState);
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    private static @Nullable PsiNamedElement findNamedElement(PsiElement e) {
        if (e == null) {
            return null;
        }
        if (e instanceof PsiNamedElement) {
            return (PsiNamedElement) e;
        }
        return null;
    }
}
