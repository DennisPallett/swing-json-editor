package nl.pallett.jsoneditor.ui.editor.code;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class CodeToolBar extends JToolBar {
    public CodeToolBar() {
        setFloatable(false);

        JToggleButton jsonButton = new JToggleButton("JSON");
        JToggleButton yamlButton = new JToggleButton("YAML");

        jsonButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        yamlButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));


        ButtonGroup group = new ButtonGroup();
        group.add(jsonButton);
        group.add(yamlButton);

        jsonButton.setSelected(true);

        add(Box.createHorizontalGlue());
        add(jsonButton);
        add(Box.createHorizontalStrut(5));
        add(yamlButton);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
}
