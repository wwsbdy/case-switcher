package com.zj.caseswitcher.handler;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiNamedElement;
import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.setting.CaseModelSettings;
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
import org.jetbrains.annotations.Nullable;

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
        ToggleState toggleState = CaseCache.getSingleToggleState(cache, selectedText);
        PsiNamedElement element = ElementUtils.getPsiNamedElement(project, editor, caretVo);
        NamesValidator validator = Objects.isNull(element) ? null : LanguageNamesValidation.INSTANCE.forLanguage(element.getLanguage());
        // 生成所有转换结果
        List<CaseVo> caseVos = CaseUtils.getAllConvert(toggleState, caseModelEnumList,
                text -> Objects.isNull(validator) || validator.isIdentifier(text, project));
        if (CollectionUtils.isEmpty(caseVos)) {
            logger.info("CaseChooserActionHandler no case vo");
            CaseModelEnum caseModelEnum = toggleState.getCaseModelEnum();
            caseVos = Collections.singletonList(new CaseVo(selectedText, selectedText, caseModelEnum, caseModelEnum));
        }
        // 创建动态子菜单组
        DefaultActionGroup subMenuGroup = new DefaultActionGroup();
        for (CaseVo caseVo : caseVos) {
            AnAction anAction = new AnAction() {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    rename(caretVo, toggleState, editor, project, dataContext, caseVo, element);
                }
            };
            // 设置子菜单选项文本，mayContainMnemonic=false防止快捷键转义
            anAction.getTemplatePresentation().setText(caseVo.getAfterText(), false);
            subMenuGroup.add(anAction);
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

    /**
     * 指定重命名
     */
    public static void rename(@NotNull CaretVo caretVo,
                              @NotNull ToggleState toggleState,
                              @NotNull Editor editor,
                              @NotNull Project project,
                              @NotNull DataContext dataContext,
                              @NotNull CaseVo caseVo,
                              @Nullable PsiNamedElement element) {
        Caret caret = caretVo.getCaret();
        String selectedText = caretVo.getSelectTest();
        if (StringUtils.isEmpty(selectedText)) {
            return;
        }
        if (CaseModelSettings.getInstance().isRenameRelated()
                && Objects.nonNull(element)
                && !ElementUtils.readOnly(editor, element)) {
            // 检查新名称是否有效
            NamesValidator validator = LanguageNamesValidation.INSTANCE.forLanguage(element.getLanguage());
            if (!validator.isIdentifier(caseVo.getAfterText(), project)) {
                HintManager.getInstance().showErrorHint(editor, "Invalid identifier: " + caseVo.getAfterText());
            } else if (SingletonRenameHandler.tryRenameRelated(element, toggleState, project, dataContext, caseVo)) {
                // 重命名成功
                return;
            }
        }
        // 只改当前变量名
        SingletonRenameHandler.singletonRename(caseVo, editor, project, toggleState, caret);
        if (Objects.nonNull(element) && ElementUtils.readOnly(editor, element)) {
            HintManager.getInstance().showInformationHint(editor, "File is read-only");
            logger.info("tryRenameRelated cannot modify read-only file");
        }
        logger.info("CaseChooserActionHandler rename toggleState next: " + toggleState);
    }
}
