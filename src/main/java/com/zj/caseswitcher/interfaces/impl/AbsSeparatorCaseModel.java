package com.zj.caseswitcher.interfaces.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

/**
 * 抽象分隔符命名风格
 *
 * @author : jie.zhou
 * @date : 2025/11/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbsSeparatorCaseModel extends AbsCaseModel {

    private final String separator;
    private final boolean upperFirstLetter;
    private final boolean allUpperCase;

    public AbsSeparatorCaseModel(char separator) {
        this(separator, false);
    }

    public AbsSeparatorCaseModel(char separator, boolean upperFirstLetter) {
        this(separator, upperFirstLetter, false);
    }

    public AbsSeparatorCaseModel(char separator, boolean upperFirstLetter, boolean allUpperCase) {
        this.separator = String.valueOf(separator);
        this.upperFirstLetter = upperFirstLetter;
        this.allUpperCase = allUpperCase;
    }

    @Override
    public boolean isThisType(@NotNull String text) {
        if (text.isEmpty()) {
            return false;
        }
        // 含有分隔符
        if (!text.contains(separator)) {
            return false;
        }
        boolean allUpperCase = text.equals(text.toUpperCase());
        if (this.allUpperCase) {
            // 全部大写
            return allUpperCase;
        }
        if (allUpperCase) {
            return false;
        }
        if (upperFirstLetter) {
            // 首字母大写
            return Character.isUpperCase(text.charAt(0));
        }
        return true;
    }

    @Override
    protected void appendFirstChar(StringBuilder sb, char c) {
        if (upperFirstLetter || allUpperCase) {
            sb.append(Character.toUpperCase(c));
        } else {
            sb.append(Character.toLowerCase(c));
        }
    }

    @Override
    protected void appendFirstCharOfWord(StringBuilder sb, char c) {
        appendSeparator(sb);
        if (upperFirstLetter || allUpperCase) {
            sb.append(Character.toUpperCase(c));
        } else {
            sb.append(Character.toLowerCase(c));
        }
    }

    @Override
    protected void appendOtherCharOfWord(StringBuilder sb, char c) {
        if (allUpperCase) {
            sb.append(Character.toUpperCase(c));
        } else {
            sb.append(Character.toLowerCase(c));
        }
    }

    @Override
    protected void appendSeparator(StringBuilder sb) {
        sb.append(separator);
    }
}
