package nl.pallett.jsoneditor.ui.editor.tabs;

import nl.pallett.jsoneditor.view.EditorPanelView;

import javax.swing.*;

public class EditorPanel extends JPanel implements EditorPanelView {
    public EditorPanel (String title) {
        super();
        add(new JLabel("Content of " + title));
    }
}
