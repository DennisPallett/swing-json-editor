package nl.pallett.jsoneditor.ui.editor;

import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.ui.editor.tree.TreePanel;
import nl.pallett.jsoneditor.view.EditorPanelView;

import javax.swing.*;
import java.awt.*;

public class EditorPanel extends JPanel implements EditorPanelView {
    private final EditorDocument editorDocument;

    public EditorPanel (EditorDocument editorDocument) {
        super();
        this.editorDocument = editorDocument;

        setLayout(new BorderLayout());

        // Left component
        JPanel leftPanel = new TreePanel(editorDocument);

        // Right component
        JPanel rightPanel = new CodePanel(editorDocument);

        // Create split pane
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftPanel,
            rightPanel
        );
        splitPane.setDividerSize(8);        // thickness of divider
        //splitPane.setOneTouchExpandable(true); // small collapse arrows
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.5);

        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.5));

        add(splitPane);
    }
}
