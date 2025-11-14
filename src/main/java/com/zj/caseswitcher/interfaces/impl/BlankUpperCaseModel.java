package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class BlankUpperCaseModel implements ICaseModel {
    public static final BlankUpperCaseModel INSTANCE = new BlankUpperCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        // 含有空格 且不是全大写 且首字母大写
        return text.contains(" ") && !text.equals(text.toUpperCase()) && Character.isUpperCase(text.charAt(0));
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        StringBuilder sb = new StringBuilder();
        boolean previousSnake = false;
        boolean allUpper = text.equals(text.toUpperCase());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && !previousSnake && !allUpper) {
                sb.append(" ");
                sb.append(Character.toUpperCase(c));
            } else if (AbsSeparatorCaseModel.SEPARATOR_SET.contains(c)) {
                sb.append(" ");
                previousSnake = true;
            } else {
                if (i == 0 || previousSnake) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(Character.toLowerCase(c));
                }
                previousSnake = false;
            }
        }
        return sb.toString();
    }
}
