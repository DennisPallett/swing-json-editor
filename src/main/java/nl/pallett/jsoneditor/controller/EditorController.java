package nl.pallett.jsoneditor.controller;

import nl.pallett.jsoneditor.view.editor.CodePanelView;
import nl.pallett.jsoneditor.view.editor.EditorPanelView;
import nl.pallett.jsoneditor.view.editor.TreePanelView;

public class EditorController {
    private final EditorPanelView editorPanelView;

    private final CodePanelView codePanelView;

    private final TreePanelView treePanelView;

    public EditorController(EditorPanelView editorPanelView, CodePanelView codePanelView, TreePanelView treePanelView) {
        this.editorPanelView = editorPanelView;
        this.codePanelView = codePanelView;
        this.treePanelView = treePanelView;

        treePanelView.addNodeSelectedListener(astNode -> {
            if (astNode != null) {
                scrollTo(astNode.startOffset);
            }
        });
    }

    private void scrollTo(int offset) {
        codePanelView.scrollTo(offset);
    }
}
