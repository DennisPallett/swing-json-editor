package nl.pallett.jsoneditor.ui.editor.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import nl.pallett.jsoneditor.ast.AstNode;
import org.jspecify.annotations.Nullable;

public class TreeBuilder {

    public DefaultMutableTreeNode buildTree(AstNode root) {
        return buildFlat(root);
    }

    private @Nullable DefaultMutableTreeNode buildFlat(AstNode node) {

        DefaultMutableTreeNode item;

        if (node.getType() == AstNode.Type.PROPERTY &&
            !node.getChildren().isEmpty() &&
            node.getChildren().getFirst().getType() == AstNode.Type.VALUE) {

            // merge key + value
            AstNode valueNode = node.getChildren().getFirst();

            AstNode combined = new AstNode(AstNode.Type.VALUE,
                node.getKey(),
                valueNode.getValue());
            combined.setValueType(valueNode.getValueType());
            combined.setPointer(valueNode.getPointer());
            combined.startOffset = node.startOffset;
            combined.startLine = node.startLine;
            combined.startColumn = node.startColumn;
            combined.endOffset = valueNode.endOffset;
            combined.endLine = valueNode.endLine;
            combined.endColumn = valueNode.endColumn;

            item = new DefaultMutableTreeNode(combined);
        } else if (node.getType() == AstNode.Type.PROPERTY &&
            !node.getChildren().isEmpty() &&
            (
                node.getChildren().getFirst().getType() == AstNode.Type.OBJECT
                    ||
                    node.getChildren().getFirst().getType() == AstNode.Type.ARRAY
            )) {

            AstNode firstChild = node.getChildren().getFirst();

            AstNode combined = AstNode.copyOf(firstChild);
            combined.startColumn = node.startColumn;
            combined.startLine = node.startLine;
            combined.startOffset = node.startOffset;

            node = combined;
            item = new DefaultMutableTreeNode(node);
        } else if (node.getType() == AstNode.Type.COMMENT && AstNode.CommentType.BLOCK != node.getCommentType()) {
            // skip comments except actual block comments
            return null;
        } else {
            item = new DefaultMutableTreeNode(node);
        }

        for (AstNode child : node.getChildren()) {

            // skip child if we already merged it
            if (node.getType() == AstNode.Type.PROPERTY &&
                child.getType() == AstNode.Type.VALUE)
                continue;

            DefaultMutableTreeNode childTreeItem = buildFlat(child);
            if (childTreeItem != null) {
                item.add(childTreeItem);
            }
        }

        return item;
    }

}