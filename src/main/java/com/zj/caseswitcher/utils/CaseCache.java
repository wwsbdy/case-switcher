package com.zj.caseswitcher.utils;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author : jie.zhou
 * @date : 2025/12/16
 */
@Service(Service.Level.PROJECT)
public final class CaseCache {

    private final static Logger logger = Logger.getInstance(CaseCache.class);
    /**
     * 缓存上一次切换状态（全局）
     */
    private final Map<String, CacheVo> CACHE_MAP = new HashMap<>();

    public static CaseCache getInstance(Project project) {
        return project.getService(CaseCache.class);
    }

    public void clearCache(@NotNull String key) {
        CACHE_MAP.remove(key);
    }

    public @NotNull CacheVo getCacheOrDefault(@NotNull String key) {
        return CACHE_MAP.computeIfAbsent(key, k -> new CacheVo(new ArrayList<>(), CaseModelEnum.RESET));
    }

    public void setCache(@NotNull String key, @Nullable CacheVo cache) {
        if (cache == null) {
            return;
        }
        CACHE_MAP.put(key, cache);
    }

    /**
     * 获取单个选择文本缓存，不存在或更改就重新赋值
     */
    public static @NotNull ToggleState getSingleToggleState(@NotNull CacheVo cacheVo,
                                                            String selectedText) {
        List<ToggleState> toggleStateList = cacheVo.getToggleStateList();
        
        // 如果缓存列表大小不是1，重置缓存
        if (toggleStateList.size() != 1) {
            logger.info("getSingleToggleState: carets size is not equal to toggle list size");
            toggleStateList.clear();
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedText);
            toggleStateList.add(new ToggleState(selectedText, selectedText, caseModelEnum));
            cacheVo.setOriginalCaseModelEnum(caseModelEnum);
        }
        
        ToggleState toggleState = toggleStateList.get(0);
        
        // 当前选择的文本和原始文本不一致，重置
        if (!toggleState.getSelectedText().equals(selectedText)) {
            logger.info("getSingleToggleState: change orig:" + toggleState.getOriginalText() + " to " + selectedText);
            toggleState.setOriginalText(selectedText);
            toggleState.setSelectedText(selectedText);
            CaseModelEnum caseModel = CaseUtils.judgment(selectedText);
            toggleState.setCaseModelEnum(caseModel);
            toggleState.setRelated(false);
            cacheVo.setOriginalCaseModelEnum(caseModel);
        }
        
        logger.info("getSingleToggleState: toggleState: " + toggleState);
        return toggleState;
    }


    /**
     * 获取多个选择文本缓存，不存在或更改就重新赋值
     */
    public static @NotNull List<ToggleState> getMultiToggleState(@NotNull List<CaretVo> caretVoList, @NotNull CacheVo cacheVo) {
        List<ToggleState> toggleStateList = cacheVo.getToggleStateList();
        
        // 检查是否需要重置缓存
        boolean needsReset = toggleStateList.size() != caretVoList.size();
        
        if (!needsReset) {
            // 检查每个选区文本是否有变化
            for (int i = 0; i < caretVoList.size(); i++) {
                CaretVo caretVo = caretVoList.get(i);
                String selectedText = caretVo.getSelectTest();
                if (StringUtils.isEmpty(selectedText)) {
                    continue;
                }
                
                ToggleState toggleState = toggleStateList.get(i);
                // 当前选择的文本和原始文本不一致，需要重置
                if (!toggleState.getSelectedText().equals(selectedText)) {
                    needsReset = true;
                    break;
                }
            }
        }
        
        if (needsReset) {
            logger.info("getMultiToggleState: caretVoList size is not equal to toggle list size");
            toggleStateList.clear();
            
            // 获取所有选中的文本
            List<String> selectedTexts = caretVoList.stream()
                    .map(CaretVo::getSelectTest)
                    .collect(Collectors.toList());
            
            // 统一判断命名风格，避免替换不同步
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedTexts);
            cacheVo.setOriginalCaseModelEnum(caseModelEnum);
            
            // 重新生成ToggleState对象
            for (String selectedText : selectedTexts) {
                toggleStateList.add(new ToggleState(selectedText, selectedText, CaseModelEnum.RESET));
            }
        }
        
        return toggleStateList;
    }
}