package nl.pallett.jsoneditor.ui.editor;

import nl.pallett.jsoneditor.view.EditorPanelView;

import javax.swing.*;
import java.awt.*;

public class EditorPanel extends JPanel implements EditorPanelView {
    public EditorPanel (String title) {
        super();

        setLayout(new BorderLayout());

        // Left component
        JPanel leftPanel = new TreePanel();

        // Right component
        JPanel rightPanel = new CodePanel();

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
