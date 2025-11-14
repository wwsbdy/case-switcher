package com.zj.caseswitcher.interfaces.impl;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class DashCaseModel extends AbsSeparatorCaseModel {
    public static final DashCaseModel INSTANCE = new DashCaseModel();

    public DashCaseModel() {
        super('-');
    }
}
