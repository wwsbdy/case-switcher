package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class BlankAllUpperCaseModel implements ICaseModel {
    public static final BlankAllUpperCaseModel INSTANCE = new BlankAllUpperCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        return text.contains(" ") && text.equals(text.toUpperCase());
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        String convert = BlankCaseModel.INSTANCE.convert(text);
        return convert.toUpperCase();
    }
}
