package nl.pallett.jsoneditor.ui.tabs;

import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.ui.editor.EditorPanel;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import nl.pallett.jsoneditor.view.editor.EditorTabbedView;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventListener;

public class EditorTabbedPane extends JTabbedPane implements EditorTabbedView {
    private EditorManager editorManager;

    public EditorTabbedPane() {
        super();
        enableTabReordering(this);
    }

    @Override
    public void setEditorManager(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    @Override
    public @Nullable EditorPanelView getActiveEditorPanel() {
        return (EditorPanelView) this.getSelectedComponent();
    }

    @Override
    public void addChangeEditorPanelListener(ChangeEditorPanelListener listener) {
        addChangeListener(e -> {
            Component selected = getSelectedComponent();

            if (selected instanceof EditorPanelView editorPanelView) {
                listener.onTabChange(editorPanelView);
            } else {
                listener.onTabChange(null);
            }
        });
    }

    @Override
    public void addCloseEditorPanelListener(CloseEditorPanelListener listener) {
        addContainerListener(new ContainerAdapter() {
            @Override
            public void componentRemoved(ContainerEvent e) {
                Component removed = e.getChild();
                if (removed instanceof EditorPanelView editorPanelView) {
                    listener.onTabClose(editorPanelView);
                } else {
                    listener.onTabClose(null);
                }
            }
        });
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

    public interface ChangeEditorPanelListener extends EventListener {
        void onTabChange(@Nullable EditorPanelView editorPanel);
    }

    public interface CloseEditorPanelListener extends EventListener {
        void onTabClose(@Nullable EditorPanelView editorPanel);
    }


}