package com.zj.caseswitcher.interfaces.impl;

/**
 * CAMEL CASE
 *
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class BlankAllUpperCaseModel extends AbsSeparatorCaseModel {
    public static final BlankAllUpperCaseModel INSTANCE = new BlankAllUpperCaseModel();

    private BlankAllUpperCaseModel() {
        super(' ', true, true);
    }
}
