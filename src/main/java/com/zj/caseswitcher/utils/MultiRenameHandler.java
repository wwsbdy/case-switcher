package com.zj.caseswitcher.utils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import com.zj.caseswitcher.vo.CaseVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : jie.zhou
 * @date : 2025/11/14
 */
public class MultiRenameHandler {
    private static final Logger logger = Logger.getInstance(MultiRenameHandler.class);

    public static void rename(boolean up,
                              @NotNull List<CaretVo> caretVoList,
                              @NotNull CacheVo cacheVo,
                              @NotNull Editor editor,
                              @NotNull Project project) {
        resetIfChanged(up, caretVoList, cacheVo);
        // 找到至少有变化的第一个作为下一个命名风格
        List<ToggleState> toggleStateList = cacheVo.getToggleStateList();
        List<CaseVo> caseVoList =
                CaseUtils.tryConvert(up, toggleStateList, toggleStateList.get(0).getCaseModelEnum(), cacheVo.getAllCaseModelEnums());
        if (CollectionUtils.isNotEmpty(caseVoList) && caseVoList.size() == caretVoList.size()) {
            logger.info("multiRename new logic before:" + toggleStateList.get(0).getCaseModelEnum());
            for (int i = 0; i < caseVoList.size(); i++) {
                CaretVo caretVo = caretVoList.get(i);
                Caret caret = caretVo.getCaret();
                CaseVo caseVo = caseVoList.get(i);
                ToggleState toggleState = toggleStateList.get(i);
                toggleState.setSelectedText(caseVo.getAfterText());
                toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
                Document document = editor.getDocument();
                WriteCommandAction.runWriteCommandAction(project, () ->
                        document.replaceString(caret.getSelectionStart(), caret.getSelectionEnd(), caseVo.getAfterText())
                );
            }
            logger.info("multiRename new logic after:" + toggleStateList.get(0).getCaseModelEnum());
            return;
        }
        // 老逻辑 可能全部选择都不变
        for (int i = 0; i < caretVoList.size(); i++) {
            CaretVo caretVo = caretVoList.get(i);
            Caret caret = caretVo.getCaret();
            String selectedText = caretVo.getSelectTest();
            if (StringUtils.isEmpty(selectedText)) {
                continue;
            }
            ToggleState toggleState = null;
            if (toggleStateList.size() == caretVoList.size()) {
                toggleState = toggleStateList.get(i);
            }
            if (toggleState == null) {
                CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedText);
                toggleState = new ToggleState(selectedText, selectedText, caseModelEnum);
            }
            // 判断下一个命名风格
            CaseModelEnum nextCaseModel = CaseUtils.getNextCaseModel(up, toggleState.getCaseModelEnum(), cacheVo.getAllCaseModelEnums());
            toggleState.setCaseModelEnum(nextCaseModel);
            String next = nextCaseModel.getConvert().convert(toggleState.getOriginalText());
            toggleState.setSelectedText(next);
            logger.info("multiRename toggleState next: " + toggleState);
            Document document = editor.getDocument();
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.replaceString(caret.getSelectionStart(), caret.getSelectionEnd(), next)
            );
        }
    }

    private static void resetIfChanged(boolean up, @NotNull List<CaretVo> caretVoList, @NotNull CacheVo cacheVo) {
        List<ToggleState> toggleStateList = cacheVo.getToggleStateList();
        boolean isChanged = toggleStateList.size() != caretVoList.size();
        if (!isChanged) {
            for (int i = 0; i < caretVoList.size(); i++) {
                CaretVo caretVo = caretVoList.get(i);
                String selectedText = caretVo.getSelectTest();
                if (StringUtils.isEmpty(selectedText)) {
                    continue;
                }
                ToggleState toggleState = toggleStateList.get(i);
                // 当前选择的文本和原始文本不一致，重置
                if (!toggleState.getSelectedText().equals(selectedText)) {
                    isChanged = true;
                    break;
                }
            }
        }
        if (isChanged) {
            logger.info("multiRename caretVoList size is not equal to toggle list size");
            toggleStateList.clear();
            // 多个选择文本时，每个文本类型可能不一样，导致替换不同步，统一一下
            List<String> selectedTexts = caretVoList.stream().map(CaretVo::getSelectTest)
                    .collect(Collectors.toList());
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedTexts);
            List<CaseModelEnum> allCaseModelEnums = CaseUtils.getAllCaseModel(up, caseModelEnum);
            cacheVo.setAllCaseModelEnums(allCaseModelEnums);
            for (String selectedText : selectedTexts) {
                // 当前类型设置为RESET，因为RESET在allCaseModelEnums中是caseModelEnum的上一位，可能其他文本可以被caseModelEnum转换
                // 直接设置成caseModelEnum会跳过这个caseModelEnum只能等下次循环后才能到caseModelEnum
                toggleStateList.add(new ToggleState(selectedText, selectedText, CaseModelEnum.RESET));
            }
        }
    }
}
