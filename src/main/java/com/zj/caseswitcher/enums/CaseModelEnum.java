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
    CAMEL("camelCase", CamelCaseModel.INSTANCE, 1),
    CAMEL_UPPER("CamelCase", CamelUpperCaseModel.INSTANCE, 1),
    SNAKE("camel_case", SnakeCaseModel.INSTANCE, 1),
    SNAKE_UPPER("CAMEL_CASE", SnakeUpperCaseModel.INSTANCE, 1),
    DASH("camel-case", DashCaseModel.INSTANCE, 1),
    DASH_UPPER("CAMEL-CASE", DashUpperCaseModel.INSTANCE, 1),
    BLANK("camel case", BlankCaseModel.INSTANCE, 1),
    BLANK_UPPER("Camel Case", BlankUpperCaseModel.INSTANCE, 1),
    BLANK_ALL_UPPER("CAMEL CASE", BlankAllUpperCaseModel.INSTANCE, 1),

    @Deprecated
    UPPER("UPPER", UpperCaseModel.INSTANCE, 3),
    @Deprecated
    LOWER("lower", LowerCaseModel.INSTANCE, 3),

    /**
     * 重置，防止连续转换时，回不到初始文本
     * <p>
     * 不给用户展示
     */
    RESET("reset", ResetCaseModel.INSTANCE, 0, 1),
    ;

    private final String name;
    private final ICaseModel convert;

    /**
     * 分组，在同一分组下的才会相互转换
     */
    private final int[] groups;

    CaseModelEnum(String name, ICaseModel convert, int... groups) {
        this.name = name;
        this.convert = convert;
        this.groups = groups;
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
