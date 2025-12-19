package com.zj.caseswitcher.utils;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.setting.CaseModelSettings;
import com.zj.caseswitcher.utils.log.Logger;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author : jie.zhou
 * @date : 2025/11/7
 */
public class CaseUtils {

    private static final Logger logger = Logger.getInstance(CaseUtils.class);

    private static final Pattern SELECT_PATTERN = Pattern.compile("[^A-Za-z0-9_]");

    /**
     * 判断当前命名风格
     */
    public static @NotNull CaseModelEnum judgment(@Nullable String text) {
        if (StringUtils.isBlank(text)) {
            return CaseModelEnum.RESET;
        }
        List<CaseModelEnum> allCaseModel = getAllCaseModel();
        return CollectionUtils.isEmpty(allCaseModel) ? CaseModelEnum.RESET : getCaseModelEnum(text, allCaseModel);
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
    public static @NotNull CaseModelEnum judgment(@Nullable List<String> texts) {
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
        
        // 获取所有文本的命名风格集合（去除RESET）
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

    /**
     * 获取所有命名风格
     */
    public static @NotNull List<CaseModelEnum> getAllCaseModel() {
        List<CaseModelEnum> list = new ArrayList<>();
        // 这个不给用户展示
        list.add(CaseModelEnum.RESET);

        list.addAll(getConfiguredCaseModel());
        return list;
    }

    /**
     * 获取所有命名风格
     * <p>
     * 此方法与 {@link #getAllCaseModel()} 的区别是，
     * 此方法将重置逻辑放到当前命名风格前面，避免B->C->B->A->B->C(理论上是B->C->A->B->C->...)
     * 因为第二个B是一轮循环以后开头的{@link CaseModelEnum#RESET}重置的
     * <p>
     * 如果放到当前类型的前面就不会有这个问题，插件会跳过相同的文本
     *
     * @param originalCaseModelEnum 原始命名风格
     * @return 根据当前风格和顺序插入 {@link CaseModelEnum#RESET}
     */
    public static @NotNull List<CaseModelEnum> getAllCaseModel(@Nullable CaseModelEnum originalCaseModelEnum) {
        if (originalCaseModelEnum == null || originalCaseModelEnum == CaseModelEnum.RESET) {
            return getAllCaseModel();
        }
        List<CaseModelEnum> list = getConfiguredCaseModel();
        if (CollectionUtils.isEmpty(list)) {
            list.add(CaseModelEnum.RESET);
            return list;
        }
        // 将REST放到originalCaseModelEnum前面
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            CaseModelEnum caseModelEnum = list.get(i);
            if (caseModelEnum.equals(originalCaseModelEnum)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            list.add(index, CaseModelEnum.RESET);
        } else {
            list.add(0, CaseModelEnum.RESET);
        }
        return list;
    }

    public static @NotNull List<CaseModelEnum> getConfiguredCaseModel() {
        return CaseModelSettings.getInstance().getOrderOrDefault().stream()
                .filter(CaseModelEnumVo::getEnabled)
                .map(CaseModelEnumVo::getCaseModelEnum)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static @NotNull CaseModelEnum getNextCaseModel(boolean up,
                                                          @NotNull CaseModelEnum caseModel,
                                                          @NotNull List<CaseModelEnum> allCaseModel) {
        if (CollectionUtils.isEmpty(allCaseModel)) {
            return CaseModelEnum.RESET;
        }
        Set<Integer> groupSet = new HashSet<>();
        for (int group : caseModel.getGroups()) {
            groupSet.add(group);
        }
        boolean find = false;
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
            }
        }
        return CaseModelEnum.RESET;
    }

    public static @NotNull CaseVo tryConvert(boolean up,
                                             @NotNull ToggleState toggleState,
                                             @NotNull List<CaseModelEnum> allCaseModelEnums) {
        return tryConvert(up, toggleState, allCaseModelEnums, null);
    }

    public static @NotNull CaseVo tryConvert(boolean up,
                                             @NotNull ToggleState toggleState,
                                             @NotNull List<CaseModelEnum> allCaseModelEnums,
                                             @Nullable Function<String, Boolean> func) {
        List<CaseVo> caseVoList =
                tryConvert(up, Collections.singletonList(toggleState), toggleState.getCaseModelEnum(), allCaseModelEnums, func);
        if (CollectionUtils.isEmpty(caseVoList)) {
            String selectedText = toggleState.getSelectedText();
            CaseModelEnum caseModelEnum = toggleState.getCaseModelEnum();
            return new CaseVo(selectedText, selectedText, caseModelEnum, caseModelEnum);
        }
        return caseVoList.get(0);
    }

    public static @NotNull List<CaseVo> tryConvert(boolean up,
                                                   @NotNull List<ToggleState> toggleStateList,
                                                   @NotNull CaseModelEnum caseModel,
                                                   @NotNull List<CaseModelEnum> allCaseModelEnums) {
        return tryConvert(up, toggleStateList, caseModel, allCaseModelEnums, null);
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
                                                   @NotNull List<CaseModelEnum> allCaseModelEnums,
                                                   @Nullable Function<String, Boolean> func) {
        if (CollectionUtils.isEmpty(toggleStateList)) {
            return Collections.emptyList();
        }
        
        List<CaseVo> caseVoList = new ArrayList<>();
        CaseModelEnum nextCaseModel = getNextCaseModel(up, caseModel, allCaseModelEnums);
        
        // 找到toggleStateList中第一个有变化的命名风格
        boolean foundChangedStyle = false;
        // 最多循环两遍，避免死循环
        int maxIterations = CaseModelEnum.values().length * 2;
        int iterationCount = 0;
        
        while (nextCaseModel != caseModel && iterationCount < maxIterations) {
            iterationCount++;
            
            // 检查当前命名风格是否能引起任何文本变化
            for (ToggleState toggleState : toggleStateList) {
                String selectedText = toggleState.getSelectedText();
                String originalText = toggleState.getOriginalText();
                String nextText = nextCaseModel.getConvert().convert(originalText);
                
                // 如果转换后的文本不同且符合自定义条件，则找到目标风格
                if (!nextText.equals(selectedText) && (Objects.isNull(func) || func.apply(nextText))) {
                    foundChangedStyle = true;
                    break;
                }
            }
            
            if (foundChangedStyle) {
                break;
            }
            
            // 继续查找下一个命名风格
            nextCaseModel = getNextCaseModel(up, nextCaseModel, allCaseModelEnums);
        }
        
        // 为所有toggleState生成转换结果
        for (ToggleState toggleState : toggleStateList) {
            String convertedText = nextCaseModel.getConvert().convert(toggleState.getOriginalText());
            caseVoList.add(new CaseVo(
                    toggleState.getSelectedText(),
                    convertedText,
                    toggleState.getCaseModelEnum(),
                    nextCaseModel));
        }
        
        return caseVoList;
    }

    /**
     * 获取所有可能的转换结果
     *
     * @param func 自定义判断是否符合条件
     */
    public static @NotNull List<CaseVo> getAllConvert(@NotNull ToggleState toggleState,
                                                      @NotNull List<CaseModelEnum> allCaseModelEnums,
                                                      @Nullable Function<String, Boolean> func) {
        Map<String, CaseVo> caseVoMap = new LinkedHashMap<>();
        CaseModelEnum caseModel = toggleState.getCaseModelEnum();
        String selectedText = toggleState.getSelectedText();
        String originalText = toggleState.getOriginalText();
        
        // 生成所有转换结果
        for (CaseModelEnum caseModelEnum : allCaseModelEnums) {
            String nextText = caseModelEnum.getConvert().convert(originalText);
            if (Objects.isNull(func) || func.apply(nextText)) {
                caseVoMap.computeIfAbsent(nextText, k -> new CaseVo(selectedText, nextText, caseModel, caseModelEnum));
            }
        }
        
        // 添加原始文本对应的转换结果
        caseVoMap.computeIfAbsent(originalText, k -> new CaseVo(selectedText, originalText, caseModel, CaseModelEnum.RESET));
        
        return new ArrayList<>(caseVoMap.values());
    }

    public static @NotNull String selectedText(Editor editor, Caret caret) {
        // 如果已有选区，直接返回
        String text = caret.getSelectedText();
        if (text != null && !text.isEmpty()) {
            return text;
        }
        
        // 自动扩展选区
        int start = caret.getOffset();
        int end = start;
        Document document = editor.getDocument();
        int textLength = document.getTextLength();
        
        // 向左扩展
        while (start > 0) {
            start--;
            caret.setSelection(start, end);
            String selected = caret.getSelectedText();
            if (selected == null || SELECT_PATTERN.matcher(selected).find()) {
                start++;
                break;
            }
        }
        
        // 向右扩展
        while (end < textLength) {
            end++;
            caret.setSelection(start, end);
            String selected = caret.getSelectedText();
            if (selected == null || SELECT_PATTERN.matcher(selected).find()) {
                end--;
                break;
            }
        }
        
        // 获取最终选区文本
        caret.setSelection(start, end);
        text = caret.getSelectedText();
        
        return text != null ? text : "";
    }

    /**
     * 光标移动或编辑后清空缓存
     */
    public static void registerCaretListener(Editor editor) {
        Project project = editor.getProject();
        if (project == null) {
            logger.warn("project is null");
            return;
        }
        CaretListener listener = new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent event) {
                logger.info("caretPositionChanged");
                CaseCache.getInstance(project).clearCache(editor.toString());
                editor.getCaretModel().removeCaretListener(this);
            }
        };
        editor.getCaretModel().addCaretListener(listener);
    }
}
