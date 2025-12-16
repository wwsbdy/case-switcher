package com.zj.caseswitcher.handler;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author : jie.zhou
 * @date : 2025/12/16
 */
public class CaseChooserActionHandler extends CaseActionHandler {

    private static final Logger logger = Logger.getInstance(CaseChooserActionHandler.class);

    @Override
    protected void execute(@NotNull Editor editor, DataContext dataContext,
                           @NotNull List<CaretVo> caretVoList, @NotNull CacheVo cache, @NotNull Project project) {
        if (caretVoList.size() != 1) {
            logger.warn("CaseChooserActionHandler only support one caret");
            HintManager.getInstance().showInformationHint(editor, "Not only one");
            return;
        }
        // TODO
    }
}
