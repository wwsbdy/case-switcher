package com.zj.caseswitcher.vo;

import com.zj.caseswitcher.enums.CaseModelEnum;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
@Data
public class ToggleState {
    /**
     * 当前选中的文本
     */
    @NotNull
    private String selectedText;
    /**
     * 原始文本
     */
    @NotNull
    private String originalText;
    /**
     * 当前case样式
     */
    @NotNull
    private CaseModelEnum caseModelEnum;

    /**
     * 是否关联
     */
    private boolean related;

    public ToggleState(@NotNull String selectedText, @NotNull String originalText, @NotNull CaseModelEnum caseModelEnum) {
        this.selectedText = selectedText;
        this.originalText = originalText;
        this.caseModelEnum = caseModelEnum;
    }

}
