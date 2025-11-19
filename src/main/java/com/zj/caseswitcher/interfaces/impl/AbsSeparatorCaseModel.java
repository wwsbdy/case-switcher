package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import lombok.Data;
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

    static {
        SEPARATOR_SET = new HashSet<>();
        SEPARATOR_SET.add('_');
        SEPARATOR_SET.add('-');
        SEPARATOR_SET.add(' ');
    }

    public AbsSeparatorCaseModel(char separator) {
        this.separator = String.valueOf(separator);
    }

    @Override
    public boolean isThisType(@NotNull String text) {
        // 含有分隔符 且全小写
        return text.contains(separator) && text.equals(text.toLowerCase());
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        StringBuilder sb = new StringBuilder();
        boolean previousSeparator = false;
        boolean allUpper = text.equals(text.toUpperCase());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && !previousSeparator && !allUpper) {
                sb.append(separator);
                sb.append(Character.toLowerCase(c));
            } else if (SEPARATOR_SET.contains(c)) {
                sb.append(separator);
                previousSeparator = true;
            } else {
                previousSeparator = false;
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
