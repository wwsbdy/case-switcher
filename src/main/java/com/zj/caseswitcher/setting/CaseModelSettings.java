package com.zj.caseswitcher.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.vo.CaseModelEnumVo;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * @author : jie.zhou
 * @date : 2025/11/13
 */
@State(name = "CaseModelSettings", storages = @Storage("caseModelSettings.xml"))
public class CaseModelSettings implements PersistentStateComponent<CaseModelSettings.State> {

    public static final List<CaseModelEnumVo> DEFAULTS;

    static {
        DEFAULTS = new ArrayList<>();
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.CAMEL, true));
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.CAMEL_UPPER, true));
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.SNAKE, true));
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.SNAKE_UPPER, true));
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.DASH, false));
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.DASH_UPPER, false));
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.BLANK, false));
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.BLANK_UPPER, false));
        DEFAULTS.add(new CaseModelEnumVo(CaseModelEnum.BLANK_ALL_UPPER, false));
    }

    @Data
    public static class State {
        private List<CaseModelEnumVo> caseModelEnumVoList;
        /**
         * 是否重命名关联
         */
        private Boolean renameRelated;
    }

    private final State state = new State();

    public static CaseModelSettings getInstance() {
        return ApplicationManager
                .getApplication()
                .getService(CaseModelSettings.class);
    }

    @NotNull
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        if (CollectionUtils.isNotEmpty(state.getCaseModelEnumVoList())) {
            this.state.setCaseModelEnumVoList(new ArrayList<>(state.getCaseModelEnumVoList()));
        }
        this.state.setRenameRelated(state.getRenameRelated());
    }

    public synchronized void setCaseModelEnumVoList(List<CaseModelEnumVo> caseModelEnumVoList) {
        if (Objects.isNull(caseModelEnumVoList)) {
            caseModelEnumVoList = Collections.emptyList();
        }
        this.state.setCaseModelEnumVoList(caseModelEnumVoList);
    }

    public synchronized List<CaseModelEnumVo> getOrderOrDefault() {
        if (Objects.isNull(state.getCaseModelEnumVoList())) {
            List<CaseModelEnumVo> defaults = new ArrayList<>(CaseModelSettings.DEFAULTS);
            state.setCaseModelEnumVoList(defaults);
            return defaults;
        }
        return state.getCaseModelEnumVoList();
    }

    public synchronized void setRenameRelated(boolean renameRelated) {
        state.setRenameRelated(renameRelated);
    }

    public boolean isRenameRelated() {
        return Objects.isNull(state.getRenameRelated()) || state.getRenameRelated();
    }
}