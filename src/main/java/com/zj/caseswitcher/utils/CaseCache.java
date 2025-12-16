package com.zj.caseswitcher.utils;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


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

    public static @NotNull ToggleState getToggleState(@NotNull CacheVo cacheVo,
                                                       String selectedText) {
        // 获取/设置缓存数据
        List<ToggleState> toggleStateList = getToggleStateList(cacheVo, selectedText);
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

    public static @NotNull List<ToggleState> getToggleStateList(@NotNull CacheVo cacheVo, String selectedText) {
        List<ToggleState> toggleStateList = cacheVo.getToggleStateList();
        if (toggleStateList.size() != 1) {
            logger.info("getToggleStateList carets size is not equal to toggle list size");
            toggleStateList.clear();
            CaseModelEnum caseModelEnum = CaseUtils.judgment(selectedText);
            toggleStateList.add(new ToggleState(selectedText, selectedText, caseModelEnum));
            cacheVo.setOriginalCaseModelEnum(caseModelEnum);
        }
        return toggleStateList;
    }

}