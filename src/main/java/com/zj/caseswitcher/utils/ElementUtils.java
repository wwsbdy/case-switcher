package com.zj.caseswitcher.utils;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CaretVo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/12/16
 */
public class ElementUtils {

    private final static Logger logger = Logger.getInstance(ElementUtils.class);


    public static @Nullable PsiNamedElement getPsiNamedElement(@NotNull Project project, @NotNull Editor editor, @NotNull CaretVo caretVo) {
        PsiNamedElement element = getPsiNamedElement(project, editor, caretVo.getCaret());
        logger.info("getPsiNamedElement element: " + element);
        if (Objects.isNull(element)) {
            return null;
        }
        logger.info("getPsiNamedElement element class: " + element.getClass());
        return element;
    }

    public static boolean readOnly(@NotNull Editor editor, @NotNull PsiNamedElement element) {
        PsiFile psiFile = element.getContainingFile();
        if (psiFile == null) {
            return true;
        }
        VirtualFile vFile = psiFile.getVirtualFile();
        if (vFile == null || !vFile.isWritable()) {
            HintManager.getInstance().showInformationHint(editor, "File is read-only");
            logger.info("getPsiNamedElement cannot modify read-only file");
            return true;
        }
        return false;
    }

    @Nullable
    private static PsiNamedElement getPsiNamedElement(Project project, Editor editor, Caret caret) {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            logger.warn("getPsiNamedElement psiFile is null");
            return null;
        }

        int start = caret.getSelectionStart();
        int end = caret.getSelectionEnd();
        if (start >= end) {
            logger.warn("getPsiNamedElement start >= end");
            return null;
        }
        return findSymbolToRename(project, psiFile, start, end);
    }

    public static PsiNamedElement findSymbolToRename(Project project, PsiFile psiFile, int startOffset, int endOffset) {
        // 1) 通过引用查（最准确）
        PsiReference ref = psiFile.findReferenceAt(startOffset);
        if (ref == null && endOffset > startOffset) {
            ref = psiFile.findReferenceAt(endOffset - 1);
        }
        if (ref != null) {
            PsiNamedElement resolved = resolve(project, ref);
            if (Objects.nonNull(resolved)) {
                return resolved;
            }
        }

        // 2) 通过 identifier token 查
        PsiElement element = psiFile.findElementAt(startOffset);
        if (Objects.nonNull(element)) {
            PsiElement parent = element.getParent();
            if (parent instanceof PsiNamedElement) {
                logger.info("findSymbolToRename parent: " + parent);
                return (PsiNamedElement) parent;
            }
        }

        // 3) 用 commonParent（最不可靠，但可兜底）
        PsiElement startElement = psiFile.findElementAt(startOffset);
        PsiElement endElement = psiFile.findElementAt(endOffset - 1);
        if (Objects.isNull(startElement)) {
            return Objects.isNull(endElement) || !(endElement instanceof PsiNamedElement) ? null : (PsiNamedElement) endElement;
        }
        if (Objects.isNull(endElement)) {
            return null;
        }
        // 寻找两者最近公共父节点
        PsiElement commonParent = PsiTreeUtil.findCommonParent(startElement, endElement);
        if (commonParent instanceof PsiNamedElement) {
            return (PsiNamedElement) commonParent;
        }
        return null;
    }

    private static PsiNamedElement resolve(Project project, PsiReference ref) {
        return DumbService.getInstance(project)
                .computeWithAlternativeResolveEnabled(() -> {
                    PsiElement resolved = ref.resolve();
                    return resolved instanceof PsiNamedElement ? (PsiNamedElement) resolved : null;
                });
    }
}
