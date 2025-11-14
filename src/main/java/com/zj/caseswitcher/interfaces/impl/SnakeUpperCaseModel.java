package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class SnakeUpperCaseModel implements ICaseModel {

    public static final SnakeUpperCaseModel INSTANCE = new SnakeUpperCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        return text.contains("_") && Character.isUpperCase(text.charAt(0));
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        String convert = SnakeCaseModel.INSTANCE.convert(text);
        return convert.toUpperCase();
    }
}
