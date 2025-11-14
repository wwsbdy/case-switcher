package com.zj.caseswitcher;

import com.intellij.openapi.editor.actions.TextComponentEditorAction;


/**
 * @author : jie.zhou
 * @date : 2025/11/13
 */
public class CaseNextAction extends TextComponentEditorAction {

    public CaseNextAction() {
        super(new CaseEditorActionHandler(false));
    }

}
