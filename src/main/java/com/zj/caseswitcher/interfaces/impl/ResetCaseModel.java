package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import org.jetbrains.annotations.NotNull;

/**
 * 重置
 *
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class ResetCaseModel implements ICaseModel {

    public static final ResetCaseModel INSTANCE = new ResetCaseModel();

    @Override
    public boolean isThisType(@NotNull String text) {
        return false;
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        return text;
    }
}
