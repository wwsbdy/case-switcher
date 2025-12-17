package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * camelCase
 *
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class CamelCaseModel implements ICaseModel {

    public static final CamelCaseModel INSTANCE = new CamelCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        if (text.isEmpty()) {
            return false;
        }
        // 首字母是小写，并且不是全部小写，并且没有下划线，没有连字符，没有空格
        return Character.isLowerCase(text.charAt(0)) && !text.equals(text.toLowerCase())
                && !text.contains("_") && !text.contains("-") && !text.contains(" ");
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        boolean previousUpper = false;
        int continuousUpperCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 是大写字母
            if (c == Character.toUpperCase(c) && Character.isLetter(c)) {
                continuousUpperCount++;
            } else {
                // 连续大写超过1，且当前不再连续大写，将前一个字母变为大写
                if (continuousUpperCount > 1 && sb.length() > 0 && Character.isLetter(c)) {
                    char previousChar = sb.charAt(sb.length() - 1);
                    if (Character.isLetter(previousChar)) {
                        sb.setCharAt(sb.length() - 1, Character.toUpperCase(previousChar));
                    }
                }
                continuousUpperCount = 0;
            }
            if (c == '_' || c == '-' || c == ' ') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
                previousUpper = true;
            } else {
                if (previousUpper || i == 0) {
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
                previousUpper = c == Character.toUpperCase(c);
            }
        }
        return sb.toString();
    }
}
