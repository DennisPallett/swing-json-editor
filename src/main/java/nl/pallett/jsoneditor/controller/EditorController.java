package nl.pallett.jsoneditor.controller;

import nl.pallett.jsoneditor.view.editor.CodePanelView;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import nl.pallett.jsoneditor.view.editor.TreePanelView;

public class EditorController {
    private final EditorPanelView editorPanelView;

    private final CodePanelView codePanelView;

    private final TreePanelView treePanelView;

    private SyncSource syncSource = null;

    public EditorController(EditorPanelView editorPanelView, CodePanelView codePanelView, TreePanelView treePanelView) {
        this.editorPanelView = editorPanelView;
        this.codePanelView = codePanelView;
        this.treePanelView = treePanelView;

        treePanelView.addNodeSelectedListener(astNode -> {
            if (astNode != null && syncSource != SyncSource.CODE) {
                syncSource = SyncSource.TREE;
                scrollTo(astNode.startOffset, () -> syncSource = null);
            }
        });

        codePanelView.addCaretListener((startOffset, _, line, column) -> {
            if (syncSource != SyncSource.TREE) {
                syncSource = SyncSource.CODE;
                treePanelView.selectNodeForCaretPosition(startOffset);
                syncSource = null;
            }

            codePanelView.updateStatusBar(line, column);
        });
    }

    private void scrollTo(int offset, Runnable runWhenFinished) {
        codePanelView.scrollTo(offset, runWhenFinished);
    }

    private enum SyncSource {CODE, TREE};
}
