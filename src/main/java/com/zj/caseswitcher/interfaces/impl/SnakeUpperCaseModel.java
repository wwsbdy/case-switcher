package com.zj.caseswitcher.interfaces.impl;

/**
 * CAMEL_CASE
 *
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class SnakeUpperCaseModel extends AbsSeparatorCaseModel {

    public static final SnakeUpperCaseModel INSTANCE = new SnakeUpperCaseModel();

    public SnakeUpperCaseModel() {
        super('_', true, true);
    }
}
