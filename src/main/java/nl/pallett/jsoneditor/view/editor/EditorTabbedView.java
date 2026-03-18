package nl.pallett.jsoneditor.view.editor;

import nl.pallett.jsoneditor.controller.EditorManager;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.ui.tabs.EditorTabbedPane;
import nl.pallett.jsoneditor.ui.tabs.EditorTabbedPane.ChangeEditorPanelListener;
import org.jspecify.annotations.Nullable;

public interface EditorTabbedView {
    EditorPanelView addTab(EditorDocument editorDocument);
    void showTab(EditorPanelView editorPanel);
    void setEditorManager(EditorManager editorManager);
    @Nullable EditorPanelView getActiveEditorPanel();
    void addChangeEditorPanelListener(ChangeEditorPanelListener listener);
    void addCloseEditorPanelListener(EditorTabbedPane.CloseEditorPanelListener listener);
}
