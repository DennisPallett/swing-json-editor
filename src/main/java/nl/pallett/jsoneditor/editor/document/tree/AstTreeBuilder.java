package nl.pallett.jsoneditor.editor.document.tree;

import javafx.scene.control.TreeItem;
import nl.pallett.jsoneditor.editor.ast.AstNode;

import java.util.HashMap;
import java.util.Map;

public class AstTreeBuilder {

    private final Map<AstNode, TreeItem<AstNode>> nodeToItem = new HashMap<>();

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

            combined.startOffset = node.startOffset;
            combined.endOffset = valueNode.endOffset;

            item = new TreeItem<>(combined);

        } else {

            item = new TreeItem<>(node);
        }

        nodeToItem.put(node, item);

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

    public TreeItem<AstNode> getTreeItem(AstNode node) {
        return nodeToItem.get(node);
    }
}