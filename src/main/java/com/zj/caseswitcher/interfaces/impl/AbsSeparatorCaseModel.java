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

    public AbsSeparatorCaseModel(char separator) {
        this(separator, false);
    }

    public AbsSeparatorCaseModel(char separator, boolean upperFirstLetter) {
        this.separator = String.valueOf(separator);
        this.upperFirstLetter = upperFirstLetter;
    }

    @Override
    public boolean isThisType(@NotNull String text) {
        // 含有分隔符 且不是全部大写
        return text.contains(separator) && !text.equals(text.toUpperCase());
    }

    @Override
    protected void appendFirstChar(StringBuilder sb, char c) {
        if (upperFirstLetter) {
            sb.append(Character.toUpperCase(c));
        } else {
            sb.append(Character.toLowerCase(c));
        }
    }

    @Override
    protected void appendFirstCharOfWord(StringBuilder sb, char c) {
        appendSeparator(sb);
        if (upperFirstLetter) {
            sb.append(Character.toUpperCase(c));
        } else {
            sb.append(Character.toLowerCase(c));
        }
    }

    @Override
    protected void appendOtherCharOfWord(StringBuilder sb, char c) {
        sb.append(Character.toLowerCase(c));
    }

    @Override
    protected void appendSeparator(StringBuilder sb) {
        sb.append(separator);
    }
}
