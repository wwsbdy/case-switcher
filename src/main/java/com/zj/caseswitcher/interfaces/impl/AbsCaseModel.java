package com.zj.caseswitcher.interfaces.impl;

import com.zj.caseswitcher.interfaces.ICaseModel;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        List<Character> charList = new ArrayList<>();
        AtomicBoolean isFirstChar = new AtomicBoolean(true);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (SEPARATOR_SET.contains(c)) {
                // 分隔符，清空队列，让下一个字符为单词首字母
                appendWordAndClear(sb, charList, isFirstChar);
                continue;
            }
            if (CollectionUtils.isEmpty(charList)) {
                charList.add(c);
                continue;
            }
            char previousChar = charList.get(charList.size() - 1);
            // 前一个是大写字母，当前是小写字母，当前作为单词首字母添加到结果中
            if (Character.isLowerCase(c) && Character.isUpperCase(previousChar)) {
                charList.remove(charList.size() - 1);
                appendWordAndClear(sb, charList, isFirstChar);
                charList.add(previousChar);
            } else if (Character.isLetter(previousChar) != Character.isLetter(c) && c == Character.toUpperCase(c)) {
                // 前一个和当前类型异或，将收集到的字母添加到结果中
                appendWordAndClear(sb, charList, isFirstChar);
            } else if (Character.isUpperCase(c) && Character.isLowerCase(previousChar)) {
                // 前一个小写，当前大写，将收集到的字母添加到结果中
                appendWordAndClear(sb, charList, isFirstChar);
            }
            charList.add(c);
        }
        appendWordAndClear(sb, charList, isFirstChar);
        return sb.toString();
    }

    private void appendWordAndClear(StringBuilder sb, List<Character> charList, AtomicBoolean isFirstChar) {
        if (CollectionUtils.isEmpty(charList)) {
            return;
        }
        for (int i = 0; i < charList.size(); i++) {
            char c = charList.get(i);
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
        charList.clear();
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
