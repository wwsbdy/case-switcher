package com.zj.caseswitcher;

import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.zj.caseswitcher.handler.CaseSwitchActionHandler;


/**
 * @author : jie.zhou
 * @date : 2025/11/13
 */
public class CaseUpAction extends TextComponentEditorAction {

    public CaseUpAction() {
        super(new CaseSwitchActionHandler(true));
    }

}
