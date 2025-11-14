package com.zj.caseswitcher.interfaces.impl;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class BlankCaseModel extends AbsSeparatorCaseModel {
    public static final BlankCaseModel INSTANCE = new BlankCaseModel();

    public BlankCaseModel() {
        super(' ');
    }
}
