package nl.pallett.jsoneditor.util;

import javafx.scene.control.TreeItem;

public final class TreeViewUtil {

    private TreeViewUtil() {}

    public static void setExpandedAtLevel(TreeItem<?> root, int targetLevel, boolean expanded) {
        traverse(root, 0, targetLevel, expanded);
    }

    private static void traverse(TreeItem<?> item, int level, int targetLevel, boolean expanded) {
        if (level == targetLevel) {
            item.setExpanded(expanded);
        }

        for (TreeItem<?> child : item.getChildren()) {
            traverse(child, level + 1, targetLevel, expanded);
        }
    }
}