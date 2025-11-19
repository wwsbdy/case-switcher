package com.zj.caseswitcher.interfaces;

import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/10/31
 */
public interface ICaseModel {

    /**
     * 判断是否为当前命名风格
     */
    boolean isThisType(@NotNull String text);

    /**
     * 转换为当前命名风格
     */
    @NotNull
    String convert(@NotNull String text);
}
