package com.zj.caseswitcher.vo;

import lombok.Data;

/**
 * @author : jie.zhou
 * @date : 2025/11/14
 */
@Data
public class NextVo {

    /**
     * 向上查找
     */
    private final boolean up;
    private final int size;
    private int index;
    /**
     * 循环次数
     */
    private int loopCount = 2;

    public NextVo(boolean up, int size) {
        this.up = up;
        this.size = size;
        this.index = up ? size - 1 : 0;
    }

    public boolean condition() {
        return (up ? index >= 0 : index < size) && loopCount > 0;
    }

    public void after() {
        if (up) {
            index--;
            if (index < 0) {
                index = size - 1;
                loopCount--;
            }
        } else {
            index++;
            if (index >= size) {
                index = 0;
                loopCount--;
            }
        }
    }

}
