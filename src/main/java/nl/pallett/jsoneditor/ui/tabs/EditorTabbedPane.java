package nl.pallett.jsoneditor.ui.tabs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JTabbedPane;
import javax.swing.TransferHandler;
import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.ui.editor.EditorPanel;
import nl.pallett.jsoneditor.view.EditorPanelView;
import nl.pallett.jsoneditor.view.EditorTabbedView;
import org.jspecify.annotations.Nullable;

public class EditorTabbedPane extends JTabbedPane implements EditorTabbedView {
    private EditorManager editorManager;

    public EditorTabbedPane() {
        super();

        // Add sample tabs
        //addTab(this, "Dashboard");
        //addTab(this, "Settings");
        //addTab(this, "Logs");

        enableTabReordering(this);
    }

    public void setEditorManager(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    public @Nullable EditorPanelView getActiveEditorPanel() {
        return (EditorPanelView) this.getSelectedComponent();
    }

    public EditorPanelView addTab(EditorDocument editorDocument) {
        // create new editor panel and add to tabbed pane
        EditorPanel editorPanel = new EditorPanel(editorDocument);
        add(editorPanel);

        // use custom component as "tab"
        int index = indexOfComponent(editorPanel);
        setTabComponentAt(index, new IDETab(this, editorDocument));

        // select new tab
        setSelectedIndex(index);

        return editorPanel;
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