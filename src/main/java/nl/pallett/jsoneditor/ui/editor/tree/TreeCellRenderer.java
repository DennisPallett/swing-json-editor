package nl.pallett.jsoneditor.ui.editor.tree;

import nl.pallett.jsoneditor.ast.AstNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class TreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus) {

        super.getTreeCellRendererComponent(
            tree, value, selected, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();

        if (obj instanceof AstNode astNode) {
            formatNode(astNode);
        }

        setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));

        return this;
    }

    private void formatNodeValue(AstNode item) {
        if (item.getValueType() == null) {
            setText((item.getKey() != null) ? item.getKey() + " : \"" + item.getValue() + "\"" : item.getValue());
            return;
        }

        String text = "<html>";
        if (item.isArrayItem()) {
            text += "<span style='color:black'>[" + item.getArrayIndex() + "]<span> ";
        }

        if (item.getKey() != null) {
            text += "<span style='color:orange'>" + item.getKey() + "</span> : ";
        }

        switch (item.getValueType()) {
            case INTEGER, FLOAT -> {
                text += item.getValue();
            }
            case BOOLEAN -> {
                text += item.getValue();
            }
            case NULL -> {
                text += "null";
            }
            default -> {
                text += "\"" + item.getValue() + "\"";
            }
        }

        text += "</html>";

        setText(text);
    }

    private void formatNode(AstNode item) {
        String text = "";
        if (item.isArrayItem()) {
            text += "[" + item.getArrayIndex() + "] ";
        }

        switch (item.getType()) {
            case OBJECT -> {
                text += item.getKey() != null ? item.getKey() + " { }" : "{ }";
                setText(text);

            }
            case ARRAY -> {
                text += item.getKey() != null ? item.getKey() : "";
                text += " [" + item.getArraySize() + "]";
                setText(text);
            }
            case VALUE -> formatNodeValue(item);
            case PROPERTY -> setText(item.getKey());
            case COMMENT -> setText("# " + item.getValue());
            case ALIAS -> setText("*" + item.getAlias());
            case DOCUMENT -> setText("document");
        }
    }
}
