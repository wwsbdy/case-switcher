package com.zj.caseswitcher.enums;

import com.zj.caseswitcher.interfaces.ICaseModel;
import com.zj.caseswitcher.interfaces.impl.*;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * 命名风格枚举
 *
 * @author : jie.zhou
 * @date : 2025/10/31
 */
@Getter
public enum CaseModelEnum {
    CAMEL("camelCase", CamelCaseModel.INSTANCE),
    CAMEL_UPPER("CamelCase", CamelUpperCaseModel.INSTANCE),
    SNAKE("camel_case", SnakeCaseModel.INSTANCE),
    SNAKE_UPPER("CAMEL_CASE", SnakeUpperCaseModel.INSTANCE),
    DASH("camel-case", DashCaseModel.INSTANCE),
    DASH_UPPER("CAMEL-CASE", DashUpperCaseModel.INSTANCE),
    BLANK("camel case", BlankCaseModel.INSTANCE),
    BLANK_UPPER("Camel Case", BlankUpperCaseModel.INSTANCE),
    BLANK_ALL_UPPER("CAMEL CASE", BlankAllUpperCaseModel.INSTANCE),

    @Deprecated
    UPPER("UPPER", UpperCaseModel.INSTANCE),
    @Deprecated
    LOWER("lower", LowerCaseModel.INSTANCE),

    /**
     * 重置，防止连续转换时，回不到初始文本
     * <p>
     * 不给用户展示
     */
    RESET("reset", ResetCaseModel.INSTANCE),
    ;

    private final String name;
    private final ICaseModel convert;

    CaseModelEnum(String name, ICaseModel convert) {
        this.name = name;
        this.convert = convert;
    }

    @Nullable
    public static CaseModelEnum getByName(String caseModelEnumName) {
        for (CaseModelEnum value : values()) {
            if (value.name.equals(caseModelEnumName)) {
                return value;
            }
        }
        return null;
    }
}
