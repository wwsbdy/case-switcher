package com.zj.caseswitcher.vo;

import com.zj.caseswitcher.enums.CaseModelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/11
 */
@Data
@AllArgsConstructor
public class CaseVo {
    @NotNull
    private String beforeText;
    @NotNull
    private String afterText;
    @NotNull
    private CaseModelEnum beforeCaseModelEnum;
    @NotNull
    private CaseModelEnum afterCaseModelEnum;
}
