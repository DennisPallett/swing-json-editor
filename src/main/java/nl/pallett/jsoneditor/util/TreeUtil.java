package nl.pallett.jsoneditor.util;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;

public class TreeUtil {

    private TreeUtil() {
        /* This utility class should not be instantiated */
    }

    public static void expandAll(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();

        // Traverse children
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
                TreeNode child = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(child);
                expandAll(tree, path);
            }
        }

        // Expand after children (so it works bottom-up)
        tree.expandPath(parent);
    }

    public static void collapseAll(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();

        // Traverse children first
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
                TreeNode child = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(child);
                collapseAll(tree, path);
            }
        }

        // Collapse after children
        tree.collapsePath(parent);
    }
}
