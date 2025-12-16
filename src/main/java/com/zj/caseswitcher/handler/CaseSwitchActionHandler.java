package com.zj.caseswitcher.handler;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class CaseSwitchActionHandler extends CaseActionHandler {

    private final boolean up;

    public CaseSwitchActionHandler(boolean up) {
        this.up = up;
    }

    @Override
    protected void execute(@NotNull Editor editor, DataContext dataContext,
                           @NotNull List<CaretVo> caretVoList, @NotNull CacheVo cache, @NotNull Project project) {
        if (caretVoList.size() == 1) {
            SingletonRenameHandler.rename(up, caretVoList.get(0), cache, editor, project, dataContext);
        } else {
            MultiRenameHandler.rename(up, caretVoList, cache, editor, project);
        }
    }

}
