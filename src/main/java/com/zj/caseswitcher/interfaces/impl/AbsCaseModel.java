package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象分隔符命名风格
 *
 * @author : jie.zhou
 * @date : 2025/11/13
 */
@Data
public abstract class AbsCaseModel implements ICaseModel {

    public static final Set<Character> SEPARATOR_SET;


    static {
        SEPARATOR_SET = new HashSet<>();
        SEPARATOR_SET.add('_');
        SEPARATOR_SET.add('-');
        SEPARATOR_SET.add(' ');
    }

    @Override
    public @NotNull String convert(@NotNull String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        Character previousChar = null;
        StringBuilder wordSb = new StringBuilder();
        AtomicBoolean isFirstChar = new AtomicBoolean(true);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (SEPARATOR_SET.contains(c)) {
                // 分隔符，清空队列，让下一个字符为单词首字母
                if (Objects.nonNull(previousChar)) {
                    wordSb.append(previousChar);
                }
                appendWordAndClear(sb, wordSb, isFirstChar);
                previousChar = null;
                continue;
            }
            if (Objects.isNull(previousChar)) {
                previousChar = c;
                continue;
            }
            // 前一个是大写字母，当前是小写字母，当前作为单词首字母添加到结果中
            if (Character.isLowerCase(c) && Character.isUpperCase(previousChar)) {
                appendWordAndClear(sb, wordSb, isFirstChar);
                wordSb.append(previousChar);
                previousChar = c;
                continue;
            } else if (Character.isLetter(previousChar) != Character.isLetter(c)) {
                // 前一个和当前类型异或，将收集到的字母添加到结果中
                wordSb.append(previousChar);
                appendWordAndClear(sb, wordSb, isFirstChar);
                previousChar = c;
                continue;
            } else if (Character.isUpperCase(c) && Character.isLowerCase(previousChar)) {
                // 前一个小写，当前大写，将收集到的字母添加到结果中
                wordSb.append(previousChar);
                appendWordAndClear(sb, wordSb, isFirstChar);
                previousChar = c;
                continue;
            }
            wordSb.append(previousChar);
            previousChar = c;
        }
        if (Objects.nonNull(previousChar)) {
            wordSb.append(previousChar);
        }
        appendWordAndClear(sb, wordSb, isFirstChar);
        return sb.toString();
    }

    private void appendWordAndClear(StringBuilder sb, StringBuilder wordSb, AtomicBoolean isFirstChar) {
        if (StringUtils.isEmpty(wordSb)) {
            return;
        }
        for (int i = 0; i < wordSb.length(); i++) {
            char c = wordSb.charAt(i);
            if (i == 0) {
                if (isFirstChar.get()) {
                    appendFirstChar(sb, c);
                    isFirstChar.set(false);
                } else {
                    appendFirstCharOfWord(sb, c);
                }
                continue;
            }
            appendOtherCharOfWord(sb, c);
        }
        wordSb.setLength(0);
    }

    /**
     * 添加首字母
     */
    protected abstract void appendFirstChar(StringBuilder sb, char c);

    /**
     * 添加单词首字母
     */
    protected abstract void appendFirstCharOfWord(StringBuilder sb, char c);

    /**
     * 添加单词非首字母
     */
    protected abstract void appendOtherCharOfWord(StringBuilder sb, char c);

    /**
     * 添加分隔符
     */
    protected abstract void appendSeparator(StringBuilder sb);
}
