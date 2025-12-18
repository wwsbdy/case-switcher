package com.zj.caseswitcher.interfaces.impl;

/**
 * Camel Case
 *
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class BlankUpperCaseModel extends AbsSeparatorCaseModel {
    public static final BlankUpperCaseModel INSTANCE = new BlankUpperCaseModel();

    public BlankUpperCaseModel() {
        super(' ', true);
    }
}
