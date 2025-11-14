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

    public NextVo(boolean up, int size) {
        this.up = up;
        this.size = size;
        this.index = up ? size - 1 : 0;
    }

    public boolean condition() {
        return up ? index >= 0 : index < size;
    }

    public void after() {
        if (up) {
            index--;
        } else {
            index++;
        }
    }

}
