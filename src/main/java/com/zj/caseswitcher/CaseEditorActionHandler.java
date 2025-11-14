package com.zj.caseswitcher;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
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
        CacheVo cache = getCache(editor);
        if (caretVoList.size() == 1) {
            SingletonRenameHandler.rename(up, caretVoList.get(0), cache, editor, project, dataContext);
        } else {
            MultiRenameHandler.rename(up, caretVoList, cache, editor, project);
        }
//        registerCaretListener(editor);
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
        CACHE_MAP.remove(editor.toString());
    }

    public static @NotNull CacheVo getCache(@NotNull Editor editor) {
        return CACHE_MAP.computeIfAbsent(editor.toString(), key -> new CacheVo(new ArrayList<>(), new ArrayList<>()));
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
