package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class CamelUpperCaseModel implements ICaseModel {
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
    public @NotNull String convert(@NotNull String text) {
        String convert = CamelCaseModel.INSTANCE.convert(text);
        if (convert.length() > 1) {
            return convert.substring(0, 1).toUpperCase() + convert.substring(1);
        }
        return convert;
    }
}
