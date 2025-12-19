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
        // 没有下划线，没有连字符，没有空格
        if (text.contains("_") || text.contains("-") || text.contains(" ")) {
            return false;
        }
        // 首字母是大写
        if (!Character.isUpperCase(text.charAt(0))) {
            return false;
        }
        if (text.length() == 1) {
            return true;
        }
        // 并且不是全部大写
        return !text.equals(text.toUpperCase());
    }

    @Override
    protected void appendFirstChar(StringBuilder sb, char c) {
        sb.append(Character.toUpperCase(c));
    }
}
