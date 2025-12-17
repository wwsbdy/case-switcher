package com.zj.caseswitcher.interfaces.impl;

import org.jetbrains.annotations.NotNull;

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

    @Override
    public boolean isThisType(@NotNull String text) {
        // 含有空格 且不是全大写 且首字母大写
        return text.contains(" ") && !text.equals(text.toUpperCase()) && Character.isUpperCase(text.charAt(0));
    }
}
