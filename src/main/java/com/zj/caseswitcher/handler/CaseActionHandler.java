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
        logger.info("doExecute: editor=" + editor + ", primaryCaret=" + primaryCaret + ", dataContext=" + dataContext);

        // 验证项目是否存在
        Project project = editor.getProject();
        if (project == null) {
            logger.warn("validateExecutionEnvironment: project is null");
            return;
        }
        // 验证文件是否可写
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (Objects.nonNull(vFile) && !vFile.isWritable()) {
            logger.info("validateExecutionEnvironment: cannot modify read-only file: " + vFile.getName());
            HintManager.getInstance().showErrorHint(editor, "File is read-only");
        }

        // 验证是否配置了转换模型
        if (CollectionUtils.isEmpty(CaseUtils.getConfiguredCaseModel())) {
            logger.warn("validateExecutionEnvironment: no case model configured");
            HintManager.getInstance().showErrorHint(editor, "No case model");
        }

        String key = editor.toString();
        CaseCache caseCache = CaseCache.getInstance(project);

        // 验证选区
        List<CaretVo> caretVoList = validateCarets(editor, caseCache, key);
        if (caretVoList == null) {
            return;
        }

        CacheVo cache = caseCache.getCacheOrDefault(key);
        // 执行具体的操作
        execute(editor, dataContext, caretVoList, cache, project);

        // 处理编辑器key变化
        handleEditorKeyChange(editor, key, caseCache, cache);
    }

    /**
     * 验证选区
     */
    private List<CaretVo> validateCarets(@NotNull Editor editor, CaseCache caseCache, String key) {
        List<Caret> carets = editor.getCaretModel().getAllCarets();
        if (CollectionUtils.isEmpty(carets)) {
            logger.warn("validateCarets: no carets found");
            caseCache.clearCache(key);
            return null;
        }

        List<CaretVo> caretVoList = carets.stream()
                .map(caret -> new CaretVo(caret, CaseUtils.selectedText(editor, caret)))
                .filter(caretVo -> StringUtils.isNotEmpty(caretVo.getSelectTest()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(caretVoList)) {
            logger.warn("validateCarets: no selected text");
            caseCache.clearCache(key);
            return null;
        }

        return caretVoList;
    }

    /**
     * 处理编辑器key变化
     */
    private void handleEditorKeyChange(@NotNull Editor editor, String oldKey, CaseCache caseCache, CacheVo cache) {
        String newKey = editor.toString();
        if (!oldKey.equals(newKey)) {
            logger.info("handleEditorKeyChange: editor key changed, updating cache: " + oldKey + " -> " + newKey);
            caseCache.clearCache(oldKey);
            caseCache.setCache(newKey, cache);
        }
    }

    protected abstract void execute(@NotNull Editor editor, DataContext dataContext,
                                    @NotNull List<CaretVo> caretVoList, @NotNull CacheVo cache,
                                    @NotNull Project project);

}
