package nl.pallett.jsoneditor.ui.editor.tree;

import nl.pallett.jsoneditor.ast.AstNode;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.util.ClipboardUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class TreePopupMenu extends JPopupMenu {
    private final AstNode astNode;

    private final EditorDocument editorDocument;

    public TreePopupMenu(EditorDocument editorDocument, AstNode astNode) {
        this.astNode = astNode;
        this.editorDocument = editorDocument;

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

        addSeparator();

        // Expand all
        // Collapse all
        if (astNode.hasChildren()) {
            JMenuItem itemExpandAll = new JMenuItem("Expand all");
            add(itemExpandAll);

            JMenuItem itemCollapseAll = new JMenuItem("Collapse all");
            add(itemCollapseAll);

            addSeparator();
        }

        // Delete node (-> also deletes from code)
        JMenuItem itemDelete = new JMenuItem("Delete");
        add(itemDelete);

    }

    private void copyKey(ActionEvent e) {
        ClipboardUtil.copyToClipboard(astNode.getKey());
    }

    private void copyValue(ActionEvent e) {
        ClipboardUtil.copyToClipboard(astNode.getValue());
    }

    private void copyJson(ActionEvent e) {
        String json = editorDocument.getContents().substring(astNode.startOffset, astNode.endOffset);
        ClipboardUtil.copyToClipboard(json);
    }
}
