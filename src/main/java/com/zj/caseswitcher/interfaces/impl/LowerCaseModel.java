package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class LowerCaseModel implements ICaseModel {

    public static final LowerCaseModel INSTANCE = new LowerCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        if (text.isEmpty() || text.isBlank()) {
            return false;
        }
        // 是否全部大写
        if (!text.equals(text.toLowerCase())) {
            return false;
        }
        // 是否只包含大写字母和数字
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        return text.toLowerCase();
    }
}
