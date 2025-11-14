package com.zj.caseswitcher.setting;

import com.zj.caseswitcher.utils.log.Logger;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * 通用的 JList 拖拽排序 TransferHandler。
 * 支持 DefaultListModel。
 *
 * @author jie.zhou
 */
public class ListItemTransferHandler<T> extends TransferHandler {

    private static final Logger logger = Logger.getInstance(ListItemTransferHandler.class);

    private final DataFlavor localObjectFlavor;
    private int[] indices = null;
    private int addIndex = -1;
    private int addCount = 0;
    private final JList<T> list;

    public ListItemTransferHandler(JList<T> list) {
        this.list = list;
        this.localObjectFlavor = new ActivationDataFlavor(
                Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Transferable createTransferable(JComponent c) {
        if (!(c instanceof JList)) {
            return null;
        }
        JList<T> source = (JList<T>) c;
        indices = source.getSelectedIndices();
        @SuppressWarnings("unchecked")
        T[] transferedObjects = (T[]) source.getSelectedValuesList().toArray();
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }

        JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
        DefaultListModel<T> listModel = (DefaultListModel<T>) list.getModel();
        int index = dl.getIndex();
        addIndex = index;

        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            for (Object value : values) {
                int idx = index++;
                listModel.add(idx, (T) value);
                list.addSelectionInterval(idx, idx);
            }
            addCount = values.length;
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            logger.error(ex);
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
    }

    private void cleanup(JComponent c, boolean remove) {
        if (remove && indices != null) {
            DefaultListModel<T> model = (DefaultListModel<T>) list.getModel();
            if (addCount > 0) {
                for (int i = indices.length - 1; i >= 0; i--) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                    model.remove(indices[i]);
                }
            }
        }
        indices = null;
        addCount = 0;
        addIndex = -1;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }
}