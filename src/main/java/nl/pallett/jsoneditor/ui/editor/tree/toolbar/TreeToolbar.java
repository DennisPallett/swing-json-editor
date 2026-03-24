package nl.pallett.jsoneditor.ui.editor.tree.toolbar;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TreeToolbar extends JToolBar {
    public TreeToolbar() {

        // Create icons
        Icon newIcon = FontIcon.of(FontAwesomeSolid.FILE, 24);
        Icon saveIcon = FontIcon.of(FontAwesomeSolid.SAVE, 24);
        Icon editIcon = FontIcon.of(FontAwesomeSolid.EDIT, 24);

        Icon sortAlphaIcon = FontIcon.of(FontAwesomeSolid.SORT_ALPHA_UP, 16);

        // Buttons
        JButton button = new SortButton();
        button.setMargin(new Insets(5, 5, 5, 5));

        add(button);

        add(new JButton(new AbstractAction("", saveIcon) {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Save");
            }
        }));

        add(new JButton(new AbstractAction("", editIcon) {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Edit");
            }
        }));
    }
}
