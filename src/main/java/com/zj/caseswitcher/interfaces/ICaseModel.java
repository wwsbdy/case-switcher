package com.zj.caseswitcher.interfaces;

import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/10/31
 */
public interface ICaseModel {

    boolean isThisType(@NotNull String text);

    @NotNull
    String convert(@NotNull String text);
}
