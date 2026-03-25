package nl.pallett.jsoneditor.ui.editor.tree.toolbar;

import nl.pallett.jsoneditor.ui.editor.tree.TreePanel;

import javax.swing.*;
import java.awt.*;

public class TreeToolbar extends JToolBar {
    private final TreePanel treePanel;

    public TreeToolbar(TreePanel treePanel) {
        this.treePanel = treePanel;

        // Buttons
        SortButton sortButton = new SortButton();
        sortButton.setMargin(new Insets(5, 5, 5, 5));
        add(sortButton);


        sortButton.addOnSortChangeListener(treePanel::setSortState);
    }
}
