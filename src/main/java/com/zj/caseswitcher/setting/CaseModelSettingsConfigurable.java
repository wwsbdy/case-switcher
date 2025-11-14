package com.zj.caseswitcher.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.zj.caseswitcher.vo.CaseModelEnumVo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * @author : jie.zhou
 * @date : 2025/11/13
 */
public class CaseModelSettingsConfigurable implements Configurable {

    private DefaultListModel<CaseModelEnumVo> listModel;
    private JBCheckBox renameRelatedCheckBox;

    private final CaseModelSettings settings = CaseModelSettings.getInstance();


    @Nls
    @Override
    public String getDisplayName() {
        return "Case Model Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        listModel = new DefaultListModel<>();

        for (CaseModelEnumVo caseModelEnumVo : settings.getOrderOrDefault()) {
            listModel.addElement(new CaseModelEnumVo(caseModelEnumVo.getCaseModelEnumName(), caseModelEnumVo.getEnabled()));
        }

        JBList<CaseModelEnumVo> list = getJbList();

        // --- Case Model Order ---
        JScrollPane orderScrollPane = ScrollPaneFactory.createScrollPane(list);
        orderScrollPane.setPreferredSize(new Dimension(-1, 250));
        orderScrollPane.setBorder(IdeBorderFactory.createTitledBorder("Case Model Order"));

        // --- Others ---
        renameRelatedCheckBox = new JBCheckBox("Rename related");
        renameRelatedCheckBox.setSelected(settings.isRenameRelated());

        JPanel othersPanel = new JPanel(new BorderLayout());
        othersPanel.setBorder(IdeBorderFactory.createTitledBorder("Others"));
        othersPanel.add(renameRelatedCheckBox, BorderLayout.NORTH);

        // --- 将两个 panel 垂直放置 ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(orderScrollPane);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(othersPanel);

        // --- Restore button ---
        JPanel bottomPanel = getRestorePanel();

        // --- main panel ---
        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private @NotNull JPanel getRestorePanel() {
        JButton restoreButton = new JButton("Restore default configuration");
        restoreButton.addActionListener(e -> {
            listModel.clear();
            for (CaseModelEnumVo caseModelEnumVo : CaseModelSettings.DEFAULTS) {
                listModel.addElement(new CaseModelEnumVo(caseModelEnumVo.getCaseModelEnumName(), caseModelEnumVo.getEnabled()));
            }
            renameRelatedCheckBox.setSelected(true);
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(restoreButton);
        return bottomPanel;
    }

    private @NotNull JBList<CaseModelEnumVo> getJbList() {
        JBList<CaseModelEnumVo> list = new JBList<>(listModel);
        list.setCellRenderer(new CaseModelRenderer());
        // 点击行触发应用风格事件
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index < 0) {
                    return;
                }
                Rectangle bounds = list.getCellBounds(index, index);
                CaseModelEnumVo vo = listModel.get(index);
                vo.setEnabled(!vo.getEnabled());
                list.repaint(bounds);
            }
        });

        // 支持拖拽排序
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        list.setTransferHandler(new ListItemTransferHandler<>(list));
        return list;
    }

    @Override
    public boolean isModified() {
        return renameRelatedCheckBox.isSelected() != settings.isRenameRelated() || !getCurrentOrder().equals(settings.getOrderOrDefault());
    }

    @Override
    public void apply() {
        settings.setCaseModelEnumVoList(getCurrentOrder());
        settings.setRenameRelated(renameRelatedCheckBox.isSelected());
    }

    @Override
    public void reset() {
        listModel.clear();
        for (CaseModelEnumVo caseModelEnumVo : settings.getOrderOrDefault()) {
            listModel.addElement(new CaseModelEnumVo(caseModelEnumVo.getCaseModelEnumName(), caseModelEnumVo.getEnabled()));
        }
        renameRelatedCheckBox.setSelected(settings.isRenameRelated());
    }


    private List<CaseModelEnumVo> getCurrentOrder() {
        List<CaseModelEnumVo> order = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            order.add(listModel.get(i));
        }
        return order;
    }

    static class CaseModelRenderer extends JPanel implements ListCellRenderer<CaseModelEnumVo> {

        private final JBCheckBox checkBox = new JBCheckBox();
        private final JLabel label = new JLabel();

        CaseModelRenderer() {
            setLayout(new BorderLayout(5, 0));
            add(checkBox, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends CaseModelEnumVo> list,
                                                      CaseModelEnumVo value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            label.setText(value.getCaseModelEnumName());
            checkBox.setSelected(value.getEnabled());
            // 背景色
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.LIGHT_GRAY));
            return this;
        }
    }
}