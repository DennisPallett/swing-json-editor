package nl.pallett.jsoneditor.ui.editor.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JWindow;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

public class TabTransferHandler extends TransferHandler {
    private final JTabbedPane tabbedPane;
    private int sourceIndex;
    private BufferedImage dragImage;
    private JWindow previewWindow;
    private int ghostIndex = -1;

    public TabTransferHandler(JTabbedPane tabbedPane) { this.tabbedPane = tabbedPane; }

    @Override
    protected Transferable createTransferable(JComponent c) {
        sourceIndex = tabbedPane.getSelectedIndex();
        Component tabComp = tabbedPane.getTabComponentAt(sourceIndex);
        dragImage = new BufferedImage(tabComp.getWidth(), tabComp.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dragImage.createGraphics();
        tabComp.paint(g2);
        g2.dispose();

        previewWindow = new JWindow();
        previewWindow.setBackground(new Color(0,0,0,0));
        JLabel label = new JLabel(new ImageIcon(dragImage));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        previewWindow.add(label);
        previewWindow.pack();
        previewWindow.setVisible(true);

        return new StringSelection("");
    }

    public void updatePreviewLocation(MouseEvent e) {
        if (previewWindow != null) {
            Point p = e.getLocationOnScreen();
            previewWindow.setLocation(p.x - dragImage.getWidth()/2, p.y - dragImage.getHeight()/2);

            // Ghost tab indicator
            int idx = tabbedPane.indexAtLocation(e.getX(), e.getY());
            if (idx != ghostIndex) {
                ghostIndex = idx;
                tabbedPane.repaint();
            }
        }
    }

    @Override
    public int getSourceActions(JComponent c) { return MOVE; }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.getComponent() instanceof JTabbedPane;
    }

    @Override
    public boolean importData(TransferSupport support) {
        try {
            Point dropPoint = support.getDropLocation().getDropPoint();
            int targetIndex = tabbedPane.indexAtLocation(dropPoint.x, dropPoint.y);
            if (targetIndex == -1) targetIndex = tabbedPane.getTabCount() - 1;
            if (targetIndex == sourceIndex) return false;

            Component comp = tabbedPane.getComponentAt(sourceIndex);
            Component tabComp = tabbedPane.getTabComponentAt(sourceIndex);
            String title = tabbedPane.getTitleAt(sourceIndex);
            Icon icon = tabbedPane.getIconAt(sourceIndex);

            tabbedPane.remove(sourceIndex);
            tabbedPane.insertTab(title, icon, comp, null, targetIndex);
            tabbedPane.setTabComponentAt(targetIndex, tabComp);
            tabbedPane.setSelectedIndex(targetIndex);

            if (previewWindow != null) previewWindow.dispose();
            ghostIndex = -1;
            tabbedPane.repaint();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (previewWindow != null) previewWindow.dispose();
        ghostIndex = -1;
        tabbedPane.repaint();
    }
}