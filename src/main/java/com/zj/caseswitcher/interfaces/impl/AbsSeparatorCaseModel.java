package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 抽象分隔符命名风格
 *
 * @author : jie.zhou
 * @date : 2025/11/13
 */
@Data
public abstract class AbsSeparatorCaseModel implements ICaseModel {

    public static final Set<Character> SEPARATOR_SET;

    private final String separator;
    private final boolean upperFirstLetter;

    static {
        SEPARATOR_SET = new HashSet<>();
        SEPARATOR_SET.add('_');
        SEPARATOR_SET.add('-');
        SEPARATOR_SET.add(' ');
    }

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
    public @NotNull String convert(@NotNull String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        boolean previousSeparator = false;
        boolean previousUpper = false;
        boolean allUpper = text.equals(text.toUpperCase());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && !previousSeparator && !allUpper && !previousUpper) {
                sb.append(separator);
                sb.append(upperFirstLetter ? Character.toUpperCase(c) : Character.toLowerCase(c));
                previousUpper = c == Character.toUpperCase(c);
            } else if (SEPARATOR_SET.contains(c)) {
                sb.append(separator);
                previousSeparator = true;
            } else {
                if ((i == 0 || previousSeparator) && upperFirstLetter) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(Character.toLowerCase(c));
                }
                previousUpper = c == Character.toUpperCase(c);
                if (previousSeparator) {
                    previousSeparator = false;
                }
            }
        }
        return sb.toString();
    }
}
