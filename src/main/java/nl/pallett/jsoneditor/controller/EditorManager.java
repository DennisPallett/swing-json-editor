package nl.pallett.jsoneditor.controller;

import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.view.EditorPanelView;
import nl.pallett.jsoneditor.view.EditorTabbedView;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EditorManager {

    private final EditorTabbedView tabbedView;

    private final Map<EditorDocument, EditorPanelView> openDocuments = new HashMap<>();

    public EditorManager(EditorTabbedView tabbedView) {
        this.tabbedView = tabbedView;
        tabbedView.setEditorManager(this);
    }

    public void newDocument() {
        EditorDocument newDoc = new EditorDocument("Untitled", null);
        EditorPanelView editorPanelView = tabbedView.addTab(newDoc);

        openDocuments.put(newDoc, editorPanelView);
    }

    public void openFile(Path file) {
        EditorDocument newDoc = new EditorDocument(file.getFileName().toString(), file);
        EditorPanelView editorPanelView = tabbedView.addTab(newDoc);

        openDocuments.put(newDoc, editorPanelView);
    }

}
