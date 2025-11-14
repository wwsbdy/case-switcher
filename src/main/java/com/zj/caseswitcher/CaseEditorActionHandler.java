package com.zj.caseswitcher;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.CaretSpecificDataContext;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
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
import com.zj.caseswitcher.utils.CaseUtils;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CaretVo;
import com.zj.caseswitcher.vo.CaseVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class CaseEditorActionHandler extends EditorActionHandler {

    private static final Logger logger = Logger.getInstance(CaseEditorActionHandler.class);
    private static final Pattern SELECT_PATTERN = Pattern.compile("[^A-Za-z0-9_]");
    /**
     * 缓存上一次切换状态（全局）
     */
    private static final Map<String, List<ToggleState>> TOGGLE_MAP = new HashMap<>();

    private final boolean up;

    public CaseEditorActionHandler(boolean up) {
        this.up = up;
    }


    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret primaryCaret, DataContext dataContext) {
        logger.info("editor: " + editor + " PrimaryCaret: " + primaryCaret + " dataContext: " + dataContext);
        Project project = editor.getProject();
        if (project == null) {
            logger.warn("project is null");
            return;
        }
        if (CollectionUtils.isEmpty(CaseUtils.getConfiguredCaseModel())) {
            logger.warn("no case model");
            HintManager.getInstance().showErrorHint(editor, "No case model");
            return;
        }
        List<Caret> carets = editor.getCaretModel().getAllCarets();
        if (CollectionUtils.isEmpty(carets)) {
            logger.warn("carets is empty");
            clearCache(editor);
            return;
        }
        List<CaretVo> caretVoList = carets.stream().map(caret -> new CaretVo(caret, selectedText(editor, caret)))
                .filter(caretVo -> StringUtils.isNotEmpty(caretVo.getSelectTest()))
                .collect(Collectors.toList());
        List<ToggleState> toggleStateList = getCache(editor);
        if (caretVoList.size() == 1) {
            singletonRename(caretVoList.get(0), toggleStateList, editor, project, dataContext);
        } else {
            multiRename(caretVoList, toggleStateList, editor, project);
        }

//        registerCaretListener(editor);
    }

    private void multiRename(@NotNull List<CaretVo> caretVoList,
                             @NotNull List<ToggleState> toggleStateList,
                             @NotNull Editor editor,
                             @NotNull Project project) {
        resetIfChanged(caretVoList, toggleStateList);
        // 找到至少有变化的第一个作为下一个命名风格
        List<CaseVo> caseVoList = CaseUtils.tryConvert(up, toggleStateList, toggleStateList.get(0).getCaseModelEnum());
        if (CollectionUtils.isNotEmpty(caseVoList) && caseVoList.size() == caretVoList.size()) {
            logger.info("multiRename new logic before:" + toggleStateList.get(0).getCaseModelEnum());
            for (int i = 0; i < caseVoList.size(); i++) {
                CaretVo caretVo = caretVoList.get(i);
                Caret caret = caretVo.getCaret();
                CaseVo caseVo = caseVoList.get(i);
                ToggleState toggleState = toggleStateList.get(i);
                toggleState.setSelectedText(caseVo.getAfterText());
                toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
                Document document = editor.getDocument();
                WriteCommandAction.runWriteCommandAction(project, () ->
                        document.replaceString(caret.getSelectionStart(), caret.getSelectionEnd(), caseVo.getAfterText())
                );
            }
            logger.info("multiRename new logic after:" + toggleStateList.get(0).getCaseModelEnum());
            return;
        }
        // 老逻辑 可能全部选择都不变
        for (int i = 0; i < caretVoList.size(); i++) {
            CaretVo caretVo = caretVoList.get(i);
            Caret caret = caretVo.getCaret();
            String selectedText = caretVo.getSelectTest();
            if (StringUtils.isEmpty(selectedText)) {
                continue;
            }
            ToggleState toggleState = null;
            if (toggleStateList.size() == caretVoList.size()) {
                toggleState = toggleStateList.get(i);
            }
            if (toggleState == null) {
                toggleState = new ToggleState(selectedText, selectedText, CaseUtils.judgment(selectedText));
            }
            // 判断下一个命名风格
            CaseModelEnum nextCaseModel = CaseUtils.getNextCaseModel(up, toggleState.getCaseModelEnum());
            toggleState.setCaseModelEnum(nextCaseModel);
            String next = nextCaseModel.getConvert().convert(toggleState.getOriginalText());
            toggleState.setSelectedText(next);
            logger.info("multiRename toggleState next: " + toggleState);
            Document document = editor.getDocument();
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.replaceString(caret.getSelectionStart(), caret.getSelectionEnd(), next)
            );
        }
    }

    private static void resetIfChanged(@NotNull List<CaretVo> caretVoList, @NotNull List<ToggleState> toggleStateList) {
        boolean isChanged = toggleStateList.size() != caretVoList.size();
        if (!isChanged) {
            for (int i = 0; i < caretVoList.size(); i++) {
                CaretVo caretVo = caretVoList.get(i);
                String selectedText = caretVo.getSelectTest();
                if (StringUtils.isEmpty(selectedText)) {
                    continue;
                }
                ToggleState toggleState = toggleStateList.get(i);
                // 当前选择的文本和原始文本不一致，重置
                if (!toggleState.getSelectedText().equals(selectedText)) {
                    isChanged = true;
                    break;
                }
            }
        }
        if (isChanged) {
            logger.info("multiRename caretVoList size is not equal to toggle list size");
            toggleStateList.clear();
            // 多个选择文本时，每个文本类型可能不一样，导致替换不同步，统一一下
            List<String> selectedTexts = caretVoList.stream().map(CaretVo::getSelectTest)
                    .collect(Collectors.toList());
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedTexts);
            for (String selectedText : selectedTexts) {
                toggleStateList.add(new ToggleState(selectedText, selectedText, caseModelEnum));
            }
        }
    }

    private void singletonRename(CaretVo caretVo, List<ToggleState> toggleStateList, Editor editor, Project project, DataContext dataContext) {
        Caret caret = caretVo.getCaret();
        String selectedText = caretVo.getSelectTest();
        if (StringUtils.isEmpty(selectedText)) {
            return;
        }
        if (toggleStateList.size() != 1) {
            logger.info("singletonRename carets size is not equal to toggle list size");
            toggleStateList.clear();
            toggleStateList.add(new ToggleState(selectedText, selectedText, CaseUtils.judgment(selectedText)));
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
            }
            logger.info("singletonRename toggleState: " + toggleState);
        }
        if (Objects.isNull(toggleState)) {
            toggleState = new ToggleState(selectedText, selectedText, CaseUtils.judgment(selectedText));
        }
        // 尝试使用 RenameProcessor 改名，其他使用这个变量的地方同步改名
        if (CaseModelSettings.getInstance().isRenameRelated()
                && tryRenameRelated(toggleState, project, dataContext, editor, caretVo)) {
            return;
        }
        // 只改当前变量名
        // 只有一个选择文本时，找到直到一个有变化的转换
        singletonRename(editor, project, toggleState, caret);
        logger.info("singletonRename toggleState next: " + toggleState);
    }

    private void singletonRename(Editor editor, Project project, ToggleState toggleState, Caret caret) {
        CaseVo caseVo = CaseUtils.tryConvert(up, toggleState);
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
    private boolean tryRenameRelated(@NotNull ToggleState toggleState,
                                     @NotNull Project project,
                                     @Nullable DataContext dataContext,
                                     @NotNull Editor editor,
                                     @NotNull CaretVo caretVo) {
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
                singletonRename(editor, project, toggleState, caretVo.getCaret());
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
            CaseVo caseVo = CaseUtils.tryConvert(up, toggleState, text -> validator.isIdentifier(text, project));
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

    /**
     * 光标移动或编辑后清空缓存
     */
    public static void registerCaretListener(Editor editor) {
        CaretListener listener = new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent event) {
                logger.info("caretPositionChanged");
                clearCache(editor);
                editor.getCaretModel().removeCaretListener(this);
            }
        };
        editor.getCaretModel().addCaretListener(listener);
    }

    public static void clearCache(@NotNull Editor editor) {
        TOGGLE_MAP.remove(editor.toString());
    }

    public static List<ToggleState> getCache(@NotNull Editor editor) {
        return TOGGLE_MAP.computeIfAbsent(editor.toString(), key -> new ArrayList<>());
    }

    private static PsiNamedElement findNamedElement(PsiElement e) {
        if (e == null) {
            return null;
        }
        if (e instanceof PsiNamedElement) {
            return (PsiNamedElement) e;
        }
        return null;
    }

    private @NotNull String selectedText(Editor editor, Caret caret) {
        String text = caret.getSelectedText();
        if (text == null || text.isEmpty()) {
            int start = caret.getOffset();
            if (start <= 0) {
                return "";
            }
            int end = start;
            boolean moveLeft = true;
            boolean moveRight = true;
            while (moveLeft && start > 0) {
                start--;
                caret.setSelection(start, end);
                String selected = caret.getSelectedText();
                if (selected == null || SELECT_PATTERN.matcher(selected).find()) {
                    start++;
                    moveLeft = false;
                }
            }
            while (moveRight && end < editor.getDocument().getTextLength()) {
                end++;
                caret.setSelection(start, end);
                String selected = caret.getSelectedText();
                if (selected == null || SELECT_PATTERN.matcher(selected).find()) {
                    end--;
                    moveRight = false;
                }
            }

            caret.setSelection(start, end);
            text = caret.getSelectedText();
        }

        return Objects.isNull(text) ? "" : text;
    }

}
