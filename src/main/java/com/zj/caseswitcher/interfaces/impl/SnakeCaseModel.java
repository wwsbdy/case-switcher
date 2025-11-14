package com.zj.caseswitcher.interfaces.impl;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class SnakeCaseModel extends AbsSeparatorCaseModel {

    public static final SnakeCaseModel INSTANCE = new SnakeCaseModel();

    public SnakeCaseModel() {
        super('_');
    }
}
