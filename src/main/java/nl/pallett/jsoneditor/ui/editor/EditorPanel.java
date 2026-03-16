package nl.pallett.jsoneditor.ui.editor;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.ui.editor.tree.TreePanel;
import nl.pallett.jsoneditor.view.EditorPanelView;

public class EditorPanel extends JPanel implements EditorPanelView {
    private final EditorDocument editorDocument;

    private final TreePanel treePanel;

    private final CodePanel codePanel;

    public EditorPanel (EditorDocument editorDocument) {
        super();
        this.editorDocument = editorDocument;

        setLayout(new BorderLayout());

        // Left component
        treePanel = new TreePanel(editorDocument);

        // Right component
        codePanel = new CodePanel(editorDocument);

        // Create split pane
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            treePanel,
            codePanel
        );
        splitPane.setDividerSize(8);        // thickness of divider
        //splitPane.setOneTouchExpandable(true); // small collapse arrows
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.5);

        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.5));

        add(splitPane);
    }

    @Override
    public void undo () {
        codePanel.undo();
    }

    @Override
    public void redo () {
        codePanel.redo();
    }

    @Override
    public boolean canUndo() {
        return codePanel.canUndo();
    }

    @Override
    public boolean canRedo() {
        return codePanel.canRedo();
    }
}
