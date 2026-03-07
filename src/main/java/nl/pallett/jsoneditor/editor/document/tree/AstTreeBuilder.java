package nl.pallett.jsoneditor.editor.document.tree;

import javafx.scene.control.TreeItem;
import nl.pallett.jsoneditor.editor.ast.AstNode;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AstTreeBuilder {

    private final Map<AstNode, TreeItem<AstNode>> nodeToItem = new HashMap<>();

    public TreeItem<AstNode> buildTree(AstNode root) {
        nodeToItem.clear();
        return buildFlat(root);
    }

    public @Nullable TreeItem<AstNode> getTreeItemForNode(AstNode node) {
        return nodeToItem.get(node);
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
            combined.endOffset = valueNode.endOffset;

            item = new TreeItem<>(combined);
        } else if (node.getType() == AstNode.Type.PROPERTY &&
                !node.getChildren().isEmpty() &&
                (
                        node.getChildren().getFirst().getType() == AstNode.Type.OBJECT
                        ||
                        node.getChildren().getFirst().getType() == AstNode.Type.ARRAY
                )) {

            node = node.getChildren().getFirst();
            item = new TreeItem<>(node);
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

}