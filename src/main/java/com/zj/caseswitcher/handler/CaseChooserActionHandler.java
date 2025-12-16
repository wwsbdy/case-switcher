package com.zj.caseswitcher.handler;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiNamedElement;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.utils.CaseCache;
import com.zj.caseswitcher.utils.CaseUtils;
import com.zj.caseswitcher.utils.ElementUtils;
import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CacheVo;
import com.zj.caseswitcher.vo.CaretVo;
import com.zj.caseswitcher.vo.CaseVo;
import com.zj.caseswitcher.vo.ToggleState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/12/16
 */
public class CaseChooserActionHandler extends CaseActionHandler {

    private static final Logger logger = Logger.getInstance(CaseChooserActionHandler.class);

    @Override
    protected void execute(@NotNull Editor editor, DataContext dataContext,
                           @NotNull List<CaretVo> caretVoList, @NotNull CacheVo cache, @NotNull Project project) {
        if (caretVoList.size() != 1) {
            logger.warn("CaseChooserActionHandler only support one caret");
            HintManager.getInstance().showInformationHint(editor, "Not only one");
            return;
        }
        CaretVo caretVo = caretVoList.get(0);
        String selectedText = caretVo.getSelectTest();
        if (StringUtils.isEmpty(selectedText)) {
            return;
        }
        List<CaseModelEnum> caseModelEnumList = CaseUtils.getConfiguredCaseModel();
        if (CollectionUtils.isEmpty(caseModelEnumList)) {
            logger.warn("CaseChooserActionHandler no case model");
            HintManager.getInstance().showErrorHint(editor, "No case model");
            return;
        }
        ToggleState toggleState = CaseCache.getToggleState(cache, selectedText);
        PsiNamedElement element = ElementUtils.getPsiNamedElement(project, editor, caretVo);
        NamesValidator validator = Objects.isNull(element) ? null : LanguageNamesValidation.INSTANCE.forLanguage(element.getLanguage());
        // 生成所有转换结果
        List<CaseVo> caseVos = CaseUtils.getAllConvert(false, toggleState, caseModelEnumList,
                text -> Objects.isNull(validator) || validator.isIdentifier(text, project));
        if (CollectionUtils.isEmpty(caseVos)) {
            logger.info("CaseChooserActionHandler no case vo");
            CaseModelEnum caseModelEnum = toggleState.getCaseModelEnum();
            caseVos = Collections.singletonList(new CaseVo(selectedText, selectedText, caseModelEnum, caseModelEnum));
        }
        // 创建动态子菜单组
        DefaultActionGroup subMenuGroup = new DefaultActionGroup();
        for (CaseVo caseVo : caseVos) {
            subMenuGroup.add(new AnAction(caseVo.getAfterText()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    SingletonRenameHandler.rename(caretVo, toggleState, editor, project, dataContext, caseVo, element);
                }
            });
        }
        // 弹出子菜单（Popup Menu）
        JBPopupFactory.getInstance()
                .createActionGroupPopup(null,
                        subMenuGroup,
                        dataContext,
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true)
                .showInBestPositionFor(dataContext);
    }
}
