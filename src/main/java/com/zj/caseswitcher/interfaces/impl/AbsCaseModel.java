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
        StringBuilder sb = new StringBuilder();
        CharQueue charQueue = new CharQueue();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (SEPARATOR_SET.contains(c)) {
                // 分隔符，清空队列，让下一个字符为单词首字母
                appendSeparator(sb);
                charQueue.clear();
                continue;
            }
            if (i == 0) {
                appendFirstChar(sb, c);
                charQueue.add(c);
                continue;
            }
            // 队列空，添加大写字母到队列，当前作为单词首字母添加到结果中
            if (charQueue.isEmpty()) {
                charQueue.add(Character.toUpperCase(c));
                appendFirstCharOfWord(sb, c);
            } else {
                char previousChar = charQueue.poll();
                // 前一个是小写字母，当前是大写字母，当前作为单词首字母添加到结果中
                if (Character.isLowerCase(previousChar) && Character.isUpperCase(c)) {
                    appendFirstCharOfWord(sb, c);
                    charQueue.add(Character.toUpperCase(c));
                } else {
                    appendOtherCharOfWord(sb, c);
                    charQueue.add(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
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


    private static class CharQueue {
        private final char[] chars = new char[2];
        private int head = 0;
        private int tail = 0;

        public void add(char c) {
            chars[tail++ % chars.length] = c;
        }

        public char poll() {
            return chars[head++ % chars.length];
        }

        public boolean isEmpty() {
            return head == tail;
        }


        public int size() {
            return (tail - head + chars.length) % chars.length;
        }

        public void clear() {
            head = 0;
            tail = 0;
        }
    }
}
