package nl.pallett.jsoneditor.ui.tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class TabTransferHandlerWithPreview extends TransferHandler {
    private int sourceIndex;
    private BufferedImage dragImage;
    private JWindow previewWindow;

    // @Override
    // protected Transferable createTransferable(JComponent c) {
    //     JTabbedPane tabbedPane = (JTabbedPane) c;
    //     sourceIndex = tabbedPane.getSelectedIndex();
    //     dragImage = new BufferedImage(120, 24, BufferedImage.TYPE_INT_ARGB);
    //     Graphics2D g2 = dragImage.createGraphics();
    //     tabbedPane.getTabComponentAt(sourceIndex).paint(g2);
    //     g2.dispose();
    //
    //     previewWindow = new JWindow();
    //     previewWindow.setBackground(new Color(0, 0, 0, 0));
    //     JLabel label = new JLabel(new ImageIcon(dragImage));
    //     previewWindow.add(label);
    //     previewWindow.pack();
    //     previewWindow.setVisible(true);
    //     return new StringSelection(""); // dummy
    // }

    @Override
    public int getSourceActions(JComponent c) { return MOVE; }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.getComponent() instanceof JTabbedPane;
    }

    @Override
    public boolean importData(TransferSupport support) {
        try {
            JTabbedPane target = (JTabbedPane) support.getComponent();
            Point dropPoint = support.getDropLocation().getDropPoint();
            int targetIndex = target.indexAtLocation(dropPoint.x, dropPoint.y);
            if (targetIndex == -1 || targetIndex == sourceIndex) return false;

            Component comp = target.getComponentAt(sourceIndex);
            Component tabComp = target.getTabComponentAt(sourceIndex);
            String title = target.getTitleAt(sourceIndex);

            target.remove(sourceIndex);
            target.insertTab(title, null, comp, null, targetIndex);
            target.setTabComponentAt(targetIndex, tabComp);
            target.setSelectedIndex(targetIndex);

            if (previewWindow != null) previewWindow.dispose();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
        if (previewWindow != null && e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            Point p = me.getLocationOnScreen();
            previewWindow.setLocation(p.x - dragImage.getWidth()/2, p.y - dragImage.getHeight()/2);
        }
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (previewWindow != null) previewWindow.dispose();
    }
}