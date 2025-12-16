package com.zj.caseswitcher.handler;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zj.caseswitcher.utils.CaseCache;
import com.zj.caseswitcher.utils.CaseUtils;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public abstract class CaseActionHandler extends EditorActionHandler {

    private static final Logger logger = Logger.getInstance(CaseActionHandler.class);


    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret primaryCaret, DataContext dataContext) {
        logger.info("editor: " + editor + " primaryCaret: " + primaryCaret + " dataContext: " + dataContext);
        Project project = editor.getProject();
        if (project == null) {
            logger.warn("project is null");
            return;
        }
        CaseCache caseCache = CaseCache.getInstance(project);
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (Objects.nonNull(vFile) && !vFile.isWritable()) {
            HintManager.getInstance().showInformationHint(editor, "File is read-only");
            logger.info("doExecute cannot modify read-only file");
            return;
        }
        String key = editor.toString();
        if (CollectionUtils.isEmpty(CaseUtils.getConfiguredCaseModel())) {
            logger.warn("no case model");
            HintManager.getInstance().showErrorHint(editor, "No case model");
            return;
        }
        List<Caret> carets = editor.getCaretModel().getAllCarets();
        if (CollectionUtils.isEmpty(carets)) {
            logger.warn("carets is empty");
            caseCache.clearCache(key);
            return;
        }
        List<CaretVo> caretVoList = carets.stream().map(caret -> new CaretVo(caret, CaseUtils.selectedText(editor, caret)))
                .filter(caretVo -> StringUtils.isNotEmpty(caretVo.getSelectTest()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(caretVoList)) {
            logger.warn("caretVoList is empty");
            caseCache.clearCache(key);
            return;
        }
        CacheVo cache = caseCache.getCacheOrDefault(key);
        // 执行具体的操作
        execute(editor, dataContext, caretVoList, cache, project);
        // editor文件名可能改变，导致key不一样了，重置
        if (!key.equals(editor.toString())) {
            caseCache.clearCache(key);
            caseCache.setCache(editor.toString(), cache);
        }
//        CaseUtils.registerCaretListener(editor);
    }

    protected abstract void execute(@NotNull Editor editor, DataContext dataContext,
                                    @NotNull List<CaretVo> caretVoList, @NotNull CacheVo cache,
                                    @NotNull Project project);

}
