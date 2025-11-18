package com.zj.caseswitcher;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.utils.CaseUtils;
import com.zj.caseswitcher.utils.MultiRenameHandler;
import com.zj.caseswitcher.utils.SingletonRenameHandler;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
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
    private static final Map<String, CacheVo> CACHE_MAP = new HashMap<>();

    private final boolean up;

    public CaseEditorActionHandler(boolean up) {
        this.up = up;
    }


    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret primaryCaret, DataContext dataContext) {
        logger.info("editor: " + editor + " primaryCaret: " + primaryCaret + " dataContext: " + dataContext);
        Project project = editor.getProject();
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (Objects.nonNull(vFile) && !vFile.isWritable()) {
            HintManager.getInstance().showInformationHint(editor, "File is read-only");
            logger.info("doExecute cannot modify read-only file");
            return;
        }
        String key = editor.toString();
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
            clearCache(key);
            return;
        }
        List<CaretVo> caretVoList = carets.stream().map(caret -> new CaretVo(caret, selectedText(editor, caret)))
                .filter(caretVo -> StringUtils.isNotEmpty(caretVo.getSelectTest()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(caretVoList)) {
            logger.warn("caretVoList is empty");
            clearCache(key);
            return;
        }
        CacheVo cache = getCache(key);
        if (caretVoList.size() == 1) {
            SingletonRenameHandler.rename(up, caretVoList.get(0), cache, editor, project, dataContext);
        } else {
            MultiRenameHandler.rename(up, caretVoList, cache, editor, project);
        }
        // editor文件名可能改变，导致key不一样了，重置
        if (!key.equals(editor.toString())) {
            clearCache(key);
            setCache(editor.toString(), cache);
        }
//        registerCaretListener(editor);
    }

    private void setCache(String key, CacheVo cache) {
        CACHE_MAP.put(key, cache);
    }

    /**
     * 光标移动或编辑后清空缓存
     */
    public static void registerCaretListener(Editor editor) {
        CaretListener listener = new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent event) {
                logger.info("caretPositionChanged");
                clearCache(editor.toString());
                editor.getCaretModel().removeCaretListener(this);
            }
        };
        editor.getCaretModel().addCaretListener(listener);
    }

    public static void clearCache(@NotNull String editor) {
        CACHE_MAP.remove(editor);
    }

    public static @NotNull CacheVo getCache(@NotNull String editor) {
        return CACHE_MAP.computeIfAbsent(editor, key -> new CacheVo(new ArrayList<>(), CaseModelEnum.RESET));
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
