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
        if (toggleStateList.size() != 1) {
            logger.info("getToggleStateList carets size is not equal to toggle list size");
            toggleStateList.clear();
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedText);
            toggleStateList.add(new ToggleState(selectedText, selectedText, caseModelEnum));
            cacheVo.setOriginalCaseModelEnum(caseModelEnum);
        }
        ToggleState toggleState = null;
        if (CollectionUtils.isNotEmpty(toggleStateList)) {
            toggleState = toggleStateList.get(0);
            // 当前选择的文本和原始文本不一致，重置
            if (!toggleState.getSelectedText().equals(selectedText)) {
                logger.info("getToggleState change orig:" + toggleState.getOriginalText() + " to " + selectedText);
                toggleState.setOriginalText(selectedText);
                toggleState.setSelectedText(selectedText);
                CaseModelEnum caseModel = CaseUtils.judgment(selectedText);
                toggleState.setCaseModelEnum(caseModel);
                toggleState.setRelated(false);
                cacheVo.setOriginalCaseModelEnum(caseModel);
            }
            logger.info("getToggleState toggleState: " + toggleState);
        }
        if (Objects.isNull(toggleState)) {
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedText);
            toggleState = new ToggleState(selectedText, selectedText, caseModelEnum);
        }
        return toggleState;
    }


    /**
     * 获取多个选择文本缓存，不存在或更改就重新赋值
     */
    public static @NotNull List<ToggleState> getMultiToggleState(@NotNull List<CaretVo> caretVoList, @NotNull CacheVo cacheVo) {
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
            logger.info("getMultiToggleState caretVoList size is not equal to toggle list size");
            toggleStateList.clear();
            // 多个选择文本时，每个文本类型可能不一样，导致替换不同步，统一一下
            List<String> selectedTexts = caretVoList.stream().map(CaretVo::getSelectTest)
                    .collect(Collectors.toList());
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedTexts);
            cacheVo.setOriginalCaseModelEnum(caseModelEnum);
            for (String selectedText : selectedTexts) {
                // 当前类型设置为RESET，因为RESET在allCaseModelEnums中是caseModelEnum的上一位，可能其他文本可以被caseModelEnum转换
                // 直接设置成caseModelEnum会跳过这个caseModelEnum只能等下次循环后才能到caseModelEnum
                toggleStateList.add(new ToggleState(selectedText, selectedText, CaseModelEnum.RESET));
            }
        }
        return toggleStateList;
    }
}