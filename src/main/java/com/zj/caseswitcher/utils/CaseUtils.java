package com.zj.caseswitcher.utils;

import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.setting.CaseModelSettings;
import com.zj.caseswitcher.vo.CaseModelEnumVo;
import com.zj.caseswitcher.vo.CaseVo;
import com.zj.caseswitcher.vo.NextVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class CaseUtils {

    /**
     * 判断当前命名风格
     */
    public static @NotNull CaseModelEnum judgment(@Nullable String text) {
        if (StringUtils.isBlank(text)) {
            return CaseModelEnum.RESET;
        }
        List<CaseModelEnum> allCaseModel = getAllCaseModel();
        if (CollectionUtils.isEmpty(allCaseModel)) {
            return CaseModelEnum.RESET;
        }
        return getCaseModelEnum(text, allCaseModel);
    }

    private static @NotNull CaseModelEnum getCaseModelEnum(@NotNull String text, @NotNull List<CaseModelEnum> allCaseModel) {
        for (CaseModelEnum caseModel : allCaseModel) {
            if (caseModel.getConvert().isThisType(text)) {
                return caseModel;
            }
        }
        return CaseModelEnum.RESET;
    }

    /**
     * 判断当前命名风格
     */
    public static @NotNull CaseModelEnum judgment(List<String> texts) {
        if (CollectionUtils.isEmpty(texts)) {
            return CaseModelEnum.RESET;
        }
        List<CaseModelEnum> allCaseModel = getAllCaseModel();
        if (CollectionUtils.isEmpty(allCaseModel)) {
            return CaseModelEnum.RESET;
        }
        if (texts.size() == 1) {
            return getCaseModelEnum(texts.get(0), allCaseModel);
        }
        Set<CaseModelEnum> caseModelEnumSet = texts.stream()
                .map(text -> getCaseModelEnum(text, allCaseModel))
                .filter(caseModelEnum -> caseModelEnum != CaseModelEnum.RESET)
                .collect(Collectors.toSet());
        // 取优先级最高的一个
        for (CaseModelEnum caseModelEnum : allCaseModel) {
            if (caseModelEnumSet.contains(caseModelEnum)) {
                return caseModelEnum;
            }
        }
        return CaseModelEnum.RESET;
    }

    public static List<CaseModelEnum> getAllCaseModel() {
        List<CaseModelEnum> list = new ArrayList<>();
        // 这个不给用户展示
        list.add(CaseModelEnum.RESET);

        list.addAll(getConfiguredCaseModel());
        return list;
    }

    public static List<CaseModelEnum> getConfiguredCaseModel() {
        return CaseModelSettings.getInstance().getOrderOrDefault().stream()
                .filter(CaseModelEnumVo::getEnabled)
                .map(CaseModelEnumVo::getCaseModelEnum)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static CaseModelEnum getNextCaseModel(boolean up, CaseModelEnum caseModel) {
        List<CaseModelEnum> allCaseModel = getAllCaseModel();
        if (CollectionUtils.isEmpty(allCaseModel)) {
            return CaseModelEnum.RESET;
        }
        Set<Integer> groupSet = new HashSet<>();
        for (int group : caseModel.getGroups()) {
            groupSet.add(group);
        }
        boolean find = caseModel == CaseModelEnum.RESET;
        int foundIndex = -1;
        NextVo nextVo = new NextVo(up, allCaseModel.size());
        for (; nextVo.condition(); nextVo.after()) {
            CaseModelEnum nextModel = allCaseModel.get(nextVo.getIndex());
            if (find) {
                // 在找到目标元素后，检查后续元素是否符合条件
                if (nextModel != caseModel &&
                        Arrays.stream(nextModel.getGroups()).anyMatch(groupSet::contains)) {
                    return nextModel;
                }
            } else if (nextModel.equals(caseModel)) {
                find = true;
                foundIndex = nextVo.getIndex();
            }
        }
        // 如果找到目标元素但后面没有符合条件的，从列表开头重新查找
        NextVo secondNextVo = new NextVo(up, foundIndex + 1);
        if (find) {
            for (; secondNextVo.condition(); secondNextVo.after()) {
                CaseModelEnum nextModel = allCaseModel.get(secondNextVo.getIndex());
                if (nextModel != caseModel &&
                        Arrays.stream(nextModel.getGroups()).anyMatch(groupSet::contains)) {
                    return nextModel;
                }
            }
        }
        return CaseModelEnum.RESET;
    }

    public static CaseVo tryConvert(boolean up,
                                    @NotNull ToggleState toggleState) {
        return tryConvert(up, toggleState, null);
    }

    public static CaseVo tryConvert(boolean up,
                                    @NotNull ToggleState toggleState, @Nullable Function<String, Boolean> func) {
        List<CaseVo> caseVoList = tryConvert(up, Collections.singletonList(toggleState), toggleState.getCaseModelEnum(), func);
        if (CollectionUtils.isEmpty(caseVoList)) {
            String selectedText = toggleState.getSelectedText();
            CaseModelEnum caseModelEnum = toggleState.getCaseModelEnum();
            return new CaseVo(selectedText, selectedText, caseModelEnum, caseModelEnum);
        }
        return caseVoList.get(0);
    }

    public static @NotNull List<CaseVo> tryConvert(boolean up,
                                                   @NotNull List<ToggleState> toggleStateList,
                                                   @NotNull CaseModelEnum caseModel) {
        return tryConvert(up, toggleStateList, caseModel, null);
    }

    /**
     * 尝试转换
     * 找到toggleStateList文本中至少有一个变化的命名风格，其他toggleStateList中的文本按照这个命名风格转换
     *
     * @param func 自定义判断是否符合条件
     */
    public static @NotNull List<CaseVo> tryConvert(boolean up,
                                                   @NotNull List<ToggleState> toggleStateList,
                                                   @NotNull CaseModelEnum caseModel,
                                                   @Nullable Function<String, Boolean> func) {
        if (CollectionUtils.isEmpty(toggleStateList)) {
            return Collections.emptyList();
        }
        List<CaseVo> caseVoList = new ArrayList<>();
        CaseModelEnum nextCaseModel = getNextCaseModel(up, caseModel);
        // 找到toggleStateList中第一个有变化的命名风格
        boolean changed = false;
        // 最多循环两遍，避免死循环
        int i = CaseModelEnum.values().length * 2;
        while (nextCaseModel != caseModel) {
            if (i-- <= 0) {
                break;
            }
            for (ToggleState toggleState : toggleStateList) {
                String selectedText = toggleState.getSelectedText();
                String originalText = toggleState.getOriginalText();
                String nextText = nextCaseModel.getConvert().convert(originalText);
                if (!nextText.equals(selectedText) && (Objects.isNull(func) || func.apply(nextText))) {
                    changed = true;
                    break;
                }
            }
            if (changed) {
                break;
            }
            nextCaseModel = getNextCaseModel(up, nextCaseModel);
        }
        for (ToggleState toggleState : toggleStateList) {
            caseVoList.add(new CaseVo(toggleState.getSelectedText(),
                    nextCaseModel.getConvert().convert(toggleState.getOriginalText()),
                    toggleState.getCaseModelEnum(),
                    nextCaseModel));
        }
        return caseVoList;
    }
}
