package nl.pallett.jsoneditor.editor.ast;

import javafx.scene.control.TreeItem;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstIntervalIndex {

    private IntervalNode root;

    private final Map<AstNode, TreeItem<AstNode>> nodeToItem = new HashMap<>();

    public AstIntervalIndex(TreeItem<AstNode> astRoot) {
        List<AstNode> nodes = new ArrayList<>();
        collect(astRoot, nodes);
        root = build(nodes);
    }

    public @Nullable TreeItem<AstNode> getTreeItemForNode(AstNode node) {
        return nodeToItem.get(node);
    }

    public @Nullable AstNode findDeepest(int offset) {
        return findDeepest(root, offset, null);
    }

    private @Nullable AstNode findDeepest(IntervalNode node, int offset, @Nullable AstNode best) {

        if (node == null)
            return best;

        for (AstNode n : node.overlapping) {

            if (n.startOffset <= offset && offset <= n.endOffset) {

                if (best == null ||
                        (n.endOffset - n.startOffset) < (best.endOffset - best.startOffset)) {

                    best = n;
                }
            }
        }

        if (offset < node.center)
            return findDeepest(node.left, offset, best);
        else
            return findDeepest(node.right, offset, best);
    }

    public IntervalNode build(List<AstNode> nodes) {

        if (nodes.isEmpty())
            return null;

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (AstNode n : nodes) {

            min = Math.min(min, n.startOffset);
            max = Math.max(max, n.endOffset);
        }

        int center = (min + max) / 2;

        List<AstNode> left = new ArrayList<>();
        List<AstNode> right = new ArrayList<>();
        List<AstNode> overlap = new ArrayList<>();

        for (AstNode n : nodes) {

            if (n.endOffset < center)
                left.add(n);
            else if (n.startOffset > center)
                right.add(n);
            else
                overlap.add(n);
        }

        IntervalNode node = new IntervalNode();
        node.center = center;
        node.overlapping = overlap;

        node.left = build(left);
        node.right = build(right);

        return node;
    }

    private void collect(TreeItem<AstNode> item, List<AstNode> list) {
        AstNode node = item.getValue();
        list.add(item.getValue());

        nodeToItem.put(node, item);

        for (TreeItem<AstNode> c : item.getChildren())
            collect(c, list);
    }
}
