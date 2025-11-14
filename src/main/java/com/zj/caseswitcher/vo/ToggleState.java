package com.zj.caseswitcher.vo;

import com.zj.caseswitcher.enums.CaseModelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
@Data
@AllArgsConstructor
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

}
