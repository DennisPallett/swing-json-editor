package nl.pallett.jsoneditor.controller;

import nl.pallett.jsoneditor.FileOpenIntegration;
import nl.pallett.jsoneditor.actions.AbstractActionWithState;
import nl.pallett.jsoneditor.actions.ActionManager;
import nl.pallett.jsoneditor.actions.ActionManager.Action;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.model.EditorDocument.Property;
import nl.pallett.jsoneditor.ui.MainFrame;
import nl.pallett.jsoneditor.view.MainView;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import nl.pallett.jsoneditor.view.editor.EditorTabbedView;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EditorManager {

    private final MainView mainView;

    private final EditorTabbedView tabbedView;

    private final ActionManager actionManager;

    private final Map<EditorDocument, EditorPanelView> openDocuments = new HashMap<>();

    public EditorManager(MainView mainView, EditorTabbedView tabbedView) {
        this.tabbedView = tabbedView;
        this.mainView = mainView;
        tabbedView.setEditorManager(this);

        this.actionManager = new ActionManager(this);

        FileOpenIntegration.setFileOpenHandler(this::openFile);
        FileOpenIntegration.markReady();

        tabbedView.addChangeEditorPanelListener(actionManager::updateState);
        tabbedView.addCloseEditorPanelListener(this::closeDocument);
    }

    public void newDocument() {
        EditorDocument newDoc = new EditorDocument("Untitled", null);
        EditorPanelView editorPanelView = tabbedView.addTab(newDoc);

        addDocument(newDoc, editorPanelView);
    }

    public void selectFileToOpen() {
        File[] filesToOpen = mainView.showOpenFileDialog();
        for(File file : filesToOpen) {
            openFile(file.toPath());
        }
    }

    public void saveFile(EditorDocument editorDocument) {
        if (editorDocument == null) {
            // TODO: improve error handling
            MainFrame.showError(new IllegalStateException("Unable to save; no active document opened"));
            return;
        }

        if (editorDocument.getFilePath() == null) {
            saveFileAs();
            return;
        }

        try {
            Files.writeString(editorDocument.getFilePath(), editorDocument.getContents());
        } catch (IOException e) {
            // TODO: improve error handling
            MainFrame.showError(e);
        }

        editorDocument.resetDirtyMark();
    }

    public void saveFileAs() {
        EditorDocument activeDocument = getActiveDocument();
        if (activeDocument == null) {
            return;
        }

        File saveToFile = mainView.showSaveFileDialog(activeDocument.getName());
        if (saveToFile == null) {
            return;
        }

        activeDocument.setFilePath(saveToFile.toPath());
        saveFile(activeDocument);
    }

    public void openFile(Path file) {
        Optional<EditorPanelView> alreadyOpenEditor = openDocuments.entrySet().stream()
            .filter(entry -> file.equals(entry.getKey().getFilePath()))
            .map(Map.Entry::getValue)
            .findFirst();

        if (alreadyOpenEditor.isPresent()) {
            tabbedView.showTab(alreadyOpenEditor.get());
            return;
        }

        EditorDocument document = new EditorDocument(file.getFileName().toString(), file);
        EditorPanelView editorPanelView = tabbedView.addTab(document);

        addDocument(document, editorPanelView);
    }

    public void openFile(File file) {
        openFile(file.toPath());
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    private void closeDocument(@Nullable EditorPanelView editorPanelView) {
        if (editorPanelView != null) {
            EditorDocument editorDocument = openDocuments.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), editorPanelView))
                .map(e -> e.getKey())
                .findFirst().orElse(null);

            if (editorDocument != null) {
                openDocuments.remove(editorDocument);
            }
        }
    }

    public @Nullable EditorDocument getActiveDocument() {
        EditorPanelView activeEditorPanel = tabbedView.getActiveEditorPanel();
        if (activeEditorPanel != null) {
            return openDocuments.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), activeEditorPanel))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    public @Nullable EditorPanelView getActiveEditorPanel() {
        return tabbedView.getActiveEditorPanel();
    }

    private void addDocument(EditorDocument document, EditorPanelView editorPanelView) {
        openDocuments.put(document, editorPanelView);

        document.addPropertyChangeListener(evt -> {
            if (EditorDocument.Property.CONTENTS.name().equals(evt.getPropertyName())) {
                this.actionManager.updateState(editorPanelView);
            }

            if (Property.FILE_PATH.name().equals(evt.getPropertyName())) {
                ((AbstractActionWithState)this.actionManager.getAction(Action.SAVE)).updateState(editorPanelView);
            }
        });
    }

}
