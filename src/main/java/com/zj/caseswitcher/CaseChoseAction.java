package com.zj.caseswitcher;

import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.zj.caseswitcher.handler.CaseChooserActionHandler;


/**
 * @author : jie.zhou
 * @date : 2025/11/13
 */
public class CaseChoseAction extends TextComponentEditorAction {

    public CaseChoseAction() {
        super(new CaseChooserActionHandler());
    }

}
