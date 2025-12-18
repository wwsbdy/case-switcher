package com.zj.caseswitcher.interfaces.impl;

import org.jetbrains.annotations.NotNull;

/**
 * camelCase
 *
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class CamelCaseModel extends AbsCaseModel {

    public static final CamelCaseModel INSTANCE = new CamelCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        if (text.isEmpty()) {
            return false;
        }
        // 没有下划线，没有连字符，没有空格
        if (text.contains("_") || text.contains("-") || text.contains(" ")) {
            return false;
        }
        // 首字母是小写
        if (!Character.isLowerCase(text.charAt(0))) {
            return false;
        }
        if (text.length() == 1) {
            return true;
        }
        // 并且不是全部小写
        return !text.equals(text.toLowerCase());
    }

    @Override
    protected void appendFirstChar(StringBuilder sb, char c) {
        sb.append(Character.toLowerCase(c));
    }

    @Override
    protected void appendFirstCharOfWord(StringBuilder sb, char c) {
        sb.append(Character.toUpperCase(c));
    }

    @Override
    protected void appendOtherCharOfWord(StringBuilder sb, char c) {
        sb.append(Character.toLowerCase(c));
    }

    @Override
    protected void appendSeparator(StringBuilder sb) {
    }
}
