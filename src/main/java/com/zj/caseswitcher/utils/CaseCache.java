package com.zj.caseswitcher.utils;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.vo.CacheVo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * @author : jie.zhou
 * @date : 2025/12/16
 */
@Service(Service.Level.PROJECT)
public final class CaseCache {
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

}