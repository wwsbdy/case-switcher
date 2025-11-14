package com.zj.caseswitcher.vo;

import com.zj.caseswitcher.enums.CaseModelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * @author : jie.zhou
 * @date : 2025/11/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseModelEnumVo {
    private String caseModelEnumName;
    private Boolean enabled;

    public CaseModelEnumVo(CaseModelEnum caseModelEnum, boolean enabled) {
        this.caseModelEnumName = caseModelEnum.getName();
        this.enabled = enabled;
    }

    public @Nullable CaseModelEnum getCaseModelEnum() {
        return CaseModelEnum.getByName(caseModelEnumName);
    }
}
