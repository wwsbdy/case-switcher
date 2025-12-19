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
        
        StringBuilder result = new StringBuilder();
        StringBuilder currentWord = new StringBuilder();
        boolean isFirstChar = true;
        
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            
            // 处理分隔符
            if (SEPARATOR_SET.contains(current)) {
                if (currentWord.length() > 0) {
                    appendWord(result, currentWord, isFirstChar);
                    isFirstChar = false;
                    currentWord.setLength(0);
                }
                continue;
            }
            
            // 检查是否需要开始新单词
            if (currentWord.length() > 0) {
                char previous = currentWord.charAt(currentWord.length() - 1);
                
                // 1. 小写到大写 (camelCase → camel + Case)
                if (Character.isLowerCase(previous) && Character.isUpperCase(current)) {
                    appendWord(result, currentWord, isFirstChar);
                    isFirstChar = false;
                    currentWord.setLength(0);
                }
                // 2. 类型转换 (字母/数字之间转换，如 user123 → user + 123)
                else if (Character.isLetter(previous) != Character.isLetter(current)) {
                    appendWord(result, currentWord, isFirstChar);
                    isFirstChar = false;
                    currentWord.setLength(0);
                }
                // 3. 优化大写序列处理 (如 XMLHttpRequest → XML + Http + Request)
                // 当多个大写字母后面跟着小写字母时，前面的大写字母作为一个单词
                else if (Character.isUpperCase(previous) && Character.isUpperCase(current)) {
                    // 检查下一个字符是否存在且为小写
                    if (i + 1 < text.length() && Character.isLowerCase(text.charAt(i + 1))) {
                        appendWord(result, currentWord, isFirstChar);
                        isFirstChar = false;
                        currentWord.setLength(0);
                    }
                }
            }
            
            currentWord.append(current);
        }
        
        // 处理最后一个单词
        if (currentWord.length() > 0) {
            appendWord(result, currentWord, isFirstChar);
        }
        
        return result.toString();
    }

    private void appendWord(StringBuilder result, StringBuilder word, boolean isFirstChar) {
        if (word.length() == 0) {
            return;
        }
        
        // 处理首字母
        char firstChar = word.charAt(0);
        if (isFirstChar) {
            appendFirstChar(result, firstChar);
        } else {
            appendFirstCharOfWord(result, firstChar);
        }
        
        // 处理剩余字符
        for (int i = 1; i < word.length(); i++) {
            appendOtherCharOfWord(result, word.charAt(i));
        }
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
