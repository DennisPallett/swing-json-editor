package nl.pallett.jsoneditor.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.TransferHandler;
import nl.pallett.jsoneditor.ui.editor.tabs.IDETab;
import nl.pallett.jsoneditor.ui.editor.tabs.TabTransferHandler;

public class EditorManager extends JTabbedPane {

    public EditorManager () {
        super();

        // Add sample tabs
        addTab(this, "Dashboard");
        addTab(this, "Settings");
        addTab(this, "Logs");

        enableTabReordering(this);
    }

    private static void addTab(JTabbedPane tabbedPane, String title) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Content of " + title));
        tabbedPane.add(panel);
        int index = tabbedPane.indexOfComponent(panel);
        tabbedPane.setTabComponentAt(index, new IDETab(tabbedPane, title));
    }

    private static void enableTabReordering(JTabbedPane tabbedPane) {
        TabTransferHandler handler = new TabTransferHandler(tabbedPane);
        tabbedPane.setTransferHandler(handler);
        tabbedPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                tabbedPane.getTransferHandler().exportAsDrag(tabbedPane, e, TransferHandler.MOVE);
                //handler.updatePreviewLocation(e);
            }
        });
    }

}