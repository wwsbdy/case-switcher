package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class DashUpperCaseModel implements ICaseModel {
    public static final DashUpperCaseModel INSTANCE = new DashUpperCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        // 含有- 且全部大写
        return text.contains("-") && text.equals(text.toUpperCase());
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        String convert = DashCaseModel.INSTANCE.convert(text);
        return convert.toUpperCase();
    }
}
