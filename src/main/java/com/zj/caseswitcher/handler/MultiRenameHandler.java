package com.zj.caseswitcher.handler;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ReadOnlyModificationException;
import com.intellij.openapi.project.Project;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.utils.CaseCache;
import com.zj.caseswitcher.utils.CaseUtils;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import com.zj.caseswitcher.vo.CaseVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        if (CollectionUtils.isEmpty(caretVoList)) {
            logger.info("rename: no caret");
            return;
        }
        
        List<ToggleState> toggleStateList = CaseCache.getMultiToggleState(caretVoList, cacheVo);
        if (CollectionUtils.isEmpty(toggleStateList)) {
            logger.info("rename: no toggleState");
            return;
        }
        
        List<CaseModelEnum> allCaseModels = CaseUtils.getAllCaseModel(cacheVo.getOriginalCaseModelEnum());
        
        // 找到至少有变化的第一个作为下一个命名风格
        List<CaseVo> caseVoList = 
                CaseUtils.tryConvert(up, toggleStateList, toggleStateList.get(0).getCaseModelEnum(), allCaseModels);
        
        if (CollectionUtils.isEmpty(caseVoList)) {
            logger.info("rename: no change");
            return;
        }
        
        // 新逻辑：处理caseVoList与caretVoList数量匹配的情况
        if (caseVoList.size() == caretVoList.size()) {
            logger.info("rename: new logic before:" + toggleStateList.get(0).getCaseModelEnum());
            for (int i = 0; i < caseVoList.size(); i++) {
                CaretVo caretVo = caretVoList.get(i);
                Caret caret = caretVo.getCaret();
                CaseVo caseVo = caseVoList.get(i);
                ToggleState toggleState = toggleStateList.get(i);
                
                // 更新转换状态
                updateToggleState(toggleState, caseVo);
                
                // 执行文档替换
                if (!replaceDocumentText(project, editor, caret, caseVo.getAfterText())) {
                    return;
                }
            }
            logger.info("rename: new logic after:" + toggleStateList.get(0).getCaseModelEnum());
            return;
        }
        
        // 老逻辑：处理caseVoList与caretVoList数量不匹配的情况
        logger.info("rename: using old logic");
        for (int i = 0; i < caretVoList.size(); i++) {
            CaretVo caretVo = caretVoList.get(i);
            Caret caret = caretVo.getCaret();
            String selectedText = caretVo.getSelectTest();
            if (StringUtils.isEmpty(selectedText)) {
                continue;
            }
            
            ToggleState toggleState = getToggleState(toggleStateList, caretVoList, i, selectedText);
            
            // 判断下一个命名风格
            CaseModelEnum nextCaseModel = CaseUtils.getNextCaseModel(up, toggleState.getCaseModelEnum(), allCaseModels);
            String next = nextCaseModel.getConvert().convert(toggleState.getOriginalText());
            
            // 更新转换状态
            updateToggleState(toggleState, nextCaseModel, next);
            
            // 执行文档替换
            if (!replaceDocumentText(project, editor, caret, next)) {
                return;
            }
        }
    }
    
    /**
     * 获取或创建转换状态
     */
    private static ToggleState getToggleState(List<ToggleState> toggleStateList, List<CaretVo> caretVoList, 
                                            int index, String selectedText) {
        ToggleState toggleState = null;
        if (toggleStateList.size() == caretVoList.size()) {
            toggleState = toggleStateList.get(index);
        }
        
        if (toggleState == null) {
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedText);
            toggleState = new ToggleState(selectedText, selectedText, caseModelEnum);
        }
        return toggleState;
    }
    
    /**
     * 更新转换状态
     */
    private static void updateToggleState(ToggleState toggleState, CaseVo caseVo) {
        toggleState.setSelectedText(caseVo.getAfterText());
        toggleState.setCaseModelEnum(caseVo.getAfterCaseModelEnum());
    }
    
    /**
     * 更新转换状态
     */
    private static void updateToggleState(ToggleState toggleState, CaseModelEnum caseModelEnum, String text) {
        toggleState.setCaseModelEnum(caseModelEnum);
        toggleState.setSelectedText(text);
        logger.info("updateToggleState: " + toggleState);
    }
    
    /**
     * 执行文档替换
     */
    private static boolean replaceDocumentText(Project project, Editor editor, Caret caret, String newText) {
        Document document = editor.getDocument();
        try {
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.replaceString(caret.getSelectionStart(), caret.getSelectionEnd(), newText)
            );
            return true;
        } catch (ReadOnlyModificationException e) {
            HintManager.getInstance().showErrorHint(editor, "File is read-only");
            logger.error("replaceDocumentText: cannot modify read-only file: " + e.getMessage());
            return false;
        }
    }
}
