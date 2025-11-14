package com.zj.caseswitcher.vo;

import com.zj.caseswitcher.enums.CaseModelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author : jie.zhou
 * @date : 2025/11/14
 */
@Data
@AllArgsConstructor
public class CacheVo {
    /**
     * 所有样式
     */
    @NotNull
    private List<CaseModelEnum> allCaseModelEnums;

    /**
     * 选择的文本信息
     */
    @NotNull
    private List<ToggleState> toggleStateList;
}
