package nl.pallett.jsoneditor.ast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AstIndex {

    private final List<AstNode> nodes = new ArrayList<>();

    public void index(AstNode root) {
        walk(root);
        nodes.sort(Comparator.comparingInt(AstNode::getStartOffset));
    }

    private void walk(AstNode node) {
        nodes.add(node);
        node.getChildren().forEach(this::walk);
    }

    public AstNode findNode(int offset) {

        AstNode best = null;

        for (AstNode node : nodes) {

            if (node.contains(offset)) {

                if (best == null ||
                        (node.getEndOffset() - node.getStartOffset()) <
                                (best.getEndOffset() - best.getStartOffset())) {

                    best = node;
                }
            }
        }

        return best;
    }
}