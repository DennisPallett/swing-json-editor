package nl.pallett.jsoneditor.editor.document.tree;

import javafx.scene.control.TreeItem;
import nl.pallett.jsoneditor.editor.ast.AstNode;

public class AstTreeBuilder {

    public TreeItem<AstNode> buildTree(AstNode root) {
        return buildFlat(root);
    }

    private TreeItem<AstNode> buildFlat(AstNode node) {

        TreeItem<AstNode> item;

        if (node.getType() == AstNode.Type.PROPERTY &&
                !node.getChildren().isEmpty() &&
                node.getChildren().getFirst().getType() == AstNode.Type.VALUE) {

            // merge key + value
            AstNode valueNode = node.getChildren().getFirst();

            AstNode combined = new AstNode(AstNode.Type.VALUE,
                    node.getKey(),
                    valueNode.getValue());
            combined.setValueType(valueNode.getValueType());

            combined.startOffset = node.startOffset;
            combined.startLine = node.startLine;
            combined.startColumn = node.startColumn;
            combined.endOffset = valueNode.endOffset;
            combined.endLine = valueNode.endLine;
            combined.endColumn = valueNode.endColumn;

            item = new TreeItem<>(combined);
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
            item = new TreeItem<>(node);
        } else {
            item = new TreeItem<>(node);
        }

        for (AstNode child : node.getChildren()) {

            // skip child if we already merged it
            if (node.getType() == AstNode.Type.PROPERTY &&
                    child.getType() == AstNode.Type.VALUE)
                continue;

            item.getChildren().add(buildFlat(child));
        }

        item.setExpanded(true);
        return item;
    }

}