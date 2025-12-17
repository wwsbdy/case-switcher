package com.zj.caseswitcher.interfaces.impl;

import org.jetbrains.annotations.NotNull;

/**
 * CamelCase
 *
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class CamelUpperCaseModel extends CamelCaseModel {
    public static final CamelUpperCaseModel INSTANCE = new CamelUpperCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        if (text.isEmpty()) {
            return false;
        }
        // 首字母是大写，并且不是全部大写，并且没有下划线，没有连字符，没有空格
        return Character.isUpperCase(text.charAt(0)) && !text.equals(text.toUpperCase())
                && !text.contains("_") && !text.contains("-") && !text.contains(" ");
    }

    @Override
    protected void appendFirstChar(StringBuilder sb, char c) {
        sb.append(Character.toUpperCase(c));
    }
}
