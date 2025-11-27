package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * Camel Case
 *
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
        String convert = BlankCaseModel.INSTANCE.convert(text);
        if (convert.length() > 1) {
            return convert.substring(0, 1).toUpperCase() + convert.substring(1);
        }
        return convert;
    }
}
