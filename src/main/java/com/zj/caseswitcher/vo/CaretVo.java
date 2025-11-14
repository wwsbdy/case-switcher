package com.zj.caseswitcher.vo;

import com.intellij.openapi.editor.Caret;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * @author : jie.zhou
 * @date : 2025/11/12
 */
@Data
@AllArgsConstructor
public class CaretVo {
    @NotNull
    private Caret caret;
    @NotNull
    private String selectTest;
}
