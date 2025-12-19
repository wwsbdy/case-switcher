package com.zj.caseswitcher.interfaces.impl;

/**
 * CAMEL-CASE
 *
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class DashUpperCaseModel extends AbsSeparatorCaseModel {
    public static final DashUpperCaseModel INSTANCE = new DashUpperCaseModel();

    public DashUpperCaseModel() {
        super('-', true, true);
    }
}
