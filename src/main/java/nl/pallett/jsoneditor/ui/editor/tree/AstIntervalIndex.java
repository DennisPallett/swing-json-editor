package nl.pallett.jsoneditor.ui.editor.tree;

import nl.pallett.jsoneditor.ast.AstNode;
import org.jspecify.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstIntervalIndex {

    private final IntervalNode root;

    private final Map<AstNode, DefaultMutableTreeNode> nodeToItem = new HashMap<>();

    public AstIntervalIndex(DefaultMutableTreeNode astRoot) {
        List<AstNode> nodes = new ArrayList<>();
        collect(astRoot, nodes);
        root = build(nodes);
    }

    public @Nullable DefaultMutableTreeNode getTreeItemForNode(AstNode node) {
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

    private IntervalNode build(List<AstNode> nodes) {

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

        return new IntervalNode(center, overlap, build(left), build(right));
    }

    private void collect(DefaultMutableTreeNode item, List<AstNode> list) {
        AstNode node = (AstNode) item.getUserObject();
        list.add(node);

        nodeToItem.put(node, item);

        for (int i = 0; i < item.getChildCount(); i++) {
            DefaultMutableTreeNode child =
                (DefaultMutableTreeNode) item.getChildAt(i);

            collect(child, list);
        }
    }

    private record IntervalNode(
        int center,
        List<AstNode> overlapping,
        IntervalNode left,
        IntervalNode right
    ) {}

}