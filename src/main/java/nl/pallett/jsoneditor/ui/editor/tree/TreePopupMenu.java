package nl.pallett.jsoneditor.ui.editor.tree;

import nl.pallett.jsoneditor.ast.AstNode;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.util.ClipboardUtil;
import nl.pallett.jsoneditor.util.TreeUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

public class TreePopupMenu extends JPopupMenu {
    private final AstNode astNode;

    private final EditorDocument editorDocument;

    private final DefaultMutableTreeNode treeNode;

    private final JTree tree;

    public TreePopupMenu(EditorDocument editorDocument, AstNode astNode, DefaultMutableTreeNode treeNode, JTree tree) {
        this.astNode = astNode;
        this.editorDocument = editorDocument;
        this.treeNode = treeNode;
        this.tree = tree;

        // Copy key
        if (astNode.getKey() != null) {
            JMenuItem itemCopykey = new JMenuItem("Copy key");
            itemCopykey.addActionListener(this::copyKey);
            add(itemCopykey);
        }

        if (astNode.getValue() != null) {
            JMenuItem itemCopyValue = new JMenuItem("Copy value");
            itemCopyValue.addActionListener(this::copyValue);
            add(itemCopyValue);
        } else {
            JMenuItem itemCopyJson = new JMenuItem("Copy " + editorDocument.getDocumentType());
            itemCopyJson.addActionListener(this::copyJson);
            add(itemCopyJson);
        }
        
        // Expand all
        // Collapse all
        if (astNode.hasChildren()) {
            addSeparator();

            JMenuItem itemExpandAll = new JMenuItem("Expand all");
            itemExpandAll.addActionListener(this::expandAll);
            add(itemExpandAll);

            JMenuItem itemCollapseAll = new JMenuItem("Collapse all");
            itemCollapseAll.addActionListener(this::collapseAll);
            add(itemCollapseAll);
        }
    }

    private void expandAll(ActionEvent e) {
        TreeUtil.expandAll(tree, new TreePath(treeNode.getPath()));
    }

    private void collapseAll(ActionEvent e) {
        TreeUtil.collapseAll(tree, new TreePath(treeNode.getPath()));
    }

    private void copyKey(ActionEvent e) {
        ClipboardUtil.copyToClipboard(astNode.getKey());
    }

    private void copyValue(ActionEvent e) {
        ClipboardUtil.copyToClipboard(astNode.getValue());
    }

    private void copyJson(ActionEvent e) {
        String contents = editorDocument.getContents().substring(astNode.startOffset, astNode.endOffset);
        ClipboardUtil.copyToClipboard(contents);
    }
}
