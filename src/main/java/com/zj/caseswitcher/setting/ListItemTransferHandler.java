package com.zj.caseswitcher.setting;

import com.zj.caseswitcher.utils.log.Logger;
import com.zj.caseswitcher.vo.CaseModelEnumVo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * 通用的 JList 拖拽排序 TransferHandler。
 * 支持 DefaultListModel。
 *
 * @author jie.zhou
 */
public class ListItemTransferHandler extends TransferHandler {
    private static final Logger logger = Logger.getInstance(ListItemTransferHandler.class);

    private int draggedIndex = -1;

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JList<?> list = (JList<?>) c;
        draggedIndex = list.getSelectedIndex();
        if (draggedIndex == -1) {
            return null;
        }

        CaseModelEnumVo caseModelEnumVo = (CaseModelEnumVo) list.getModel().getElementAt(draggedIndex);
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.stringFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DataFlavor.stringFlavor);
            }

            @Override
            public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return caseModelEnumVo.getCaseModelEnumName();
            }
        };
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.stringFlavor) &&
                support.getComponent() instanceof JList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            JList<?> targetList = (JList<?>) support.getComponent();
            DefaultListModel<CaseModelEnumVo> model = (DefaultListModel<CaseModelEnumVo>) targetList.getModel();

            // 获取拖拽的数据
            String enumName = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);

            // 查找对应的CaseModelItem
            CaseModelEnumVo draggedItem = null;
            for (int i = 0; i < model.getSize(); i++) {
                CaseModelEnumVo item = model.getElementAt(i);
                if (item.getCaseModelEnumName().equals(enumName)) {
                    draggedItem = item;
                    break;
                }
            }

            if (draggedItem == null) {
                return false;
            }

            // 获取放置位置
            JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
            int dropIndex = dropLocation.getIndex();
            if (dropIndex < 0) {
                dropIndex = model.getSize();
            }

            // 调整索引（如果从前往后拖拽）
            if (draggedIndex >= 0 && draggedIndex < dropIndex) {
                dropIndex--;
            }

            // 更新模型：先移除再插入
            model.removeElement(draggedItem);
            model.insertElementAt(draggedItem, dropIndex);

            return true;

        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        draggedIndex = -1;
    }
}