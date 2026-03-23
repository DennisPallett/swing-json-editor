package nl.pallett.jsoneditor.ui.editor.tree;

import nl.pallett.jsoneditor.ast.AstNode;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.view.editor.NodeSelectedListener;
import nl.pallett.jsoneditor.view.editor.TreePanelView;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TreePanel extends JPanel implements TreePanelView {
    private final EditorDocument editorDocument;

    private final TreeBuilder treeBuilder = new TreeBuilder();

    private final JTree tree;

    private AstIntervalIndex astIntervalIndex = null;

    public TreePanel (EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setLayout(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("$");
        tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(24);
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.setCellRenderer(new TreeCellRenderer());

        // create initial tree
        refreshTree();

        // Put the tree inside a scroll pane
        JScrollPane scrollPane = new JScrollPane(tree);
        add (scrollPane);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopupMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopupMenu(e);
            }
        });

        addAstListener();
    }

    @Override
    public void addNodeSelectedListener(NodeSelectedListener listener) {
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (node != null && node.getUserObject() instanceof AstNode astNode) {
                listener.onNodeSelected(astNode);
            } else {
                listener.onNodeSelected(null);
            }
        });
    }

    @Override
    public void selectNodeForCaretPosition(int caretPosition) {
        if (astIntervalIndex != null) {
            AstNode node = astIntervalIndex.findDeepest(caretPosition);

            if (node != null) {
                selectAndReveal(node);
            }
        }
    }

    public void selectAndReveal(AstNode node) {
        DefaultMutableTreeNode item = astIntervalIndex.getTreeItemForNode(node);
        if (item == null) {
            return;
        }

        TreePath path = new TreePath(item.getPath());
        tree.expandPath(path);

        // Select the item
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    private void addAstListener() {
        editorDocument.addPropertyChangeListener(event -> {
            if (event.getPropertyName().equals(EditorDocument.Property.AST_TREE.name())) {
                refreshTree();
            }
        });
    }

    private void refreshTree() {
        AstNode astTree = editorDocument.getAstTree();
        if (astTree != null) {
            List<List<String>> expandedNodes = captureExpandedNodes();

            DefaultMutableTreeNode newRoot = treeBuilder.buildTree(astTree);
            tree.setModel(new DefaultTreeModel(newRoot));
            tree.setRootVisible(false);

            restoreExpandedNodes(expandedNodes);

            astIntervalIndex = new AstIntervalIndex(newRoot);
        }
    }

    private void restoreExpandedNodes(List<List<String>> expandedNodes) {
        for (List<String> jsonPath : expandedNodes) {
            TreePath newPath = findPathByJsonPath(tree, jsonPath);
            if (newPath != null) {
                tree.expandPath(newPath);
            }
        }
    }

    private TreePath findPathByJsonPath(JTree tree, List<String> jsonPath) {
        Object root = tree.getModel().getRoot();
        TreePath path = new TreePath(root);

        // Start from the first ID after root (assuming root is always expanded/same)
        for (int i = 1; i < jsonPath.size(); i++) {
            Object currentId = jsonPath.get(i);
            Object parentNode = path.getLastPathComponent();
            boolean found = false;

            int childCount = tree.getModel().getChildCount(parentNode);
            for (int j = 0; j < childCount; j++) {
                Object childNode = tree.getModel().getChild(parentNode, j);
                Object childId = ((AstNode)((DefaultMutableTreeNode)childNode).getUserObject()).getPointerAsJsonPath();

                if (childId != null && childId.equals(currentId)) {
                    path = path.pathByAddingChild(childNode);
                    found = true;
                    break;
                }
            }
            if (!found) return null; // Path no longer exists
        }
        return path;
    }

    private List<List<String>> captureExpandedNodes() {
        List<List<String>> expandedPaths = new ArrayList<>();
        Enumeration<TreePath> expanded = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));

        if (expanded != null) {
            while (expanded.hasMoreElements()) {
                TreePath path = expanded.nextElement();
                List<String> idPath = new ArrayList<>();
                for (Object node : path.getPath()) {
                    String jsonPath = ((AstNode)((DefaultMutableTreeNode)node).getUserObject()).getPointerAsJsonPath();
                    if (jsonPath != null) {
                        idPath.add(jsonPath);
                    }
                }
                expandedPaths.add(idPath);
            }
        }

        return expandedPaths;
    }

    private void showPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            int row = tree.getRowForLocation(e.getX(), e.getY());
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());

            if (row != -1) {
                tree.setSelectionPath(path); // select the node

                // get the node
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                AstNode astNode = (AstNode) node.getUserObject();

                // You can customize menu based on node here
                new TreePopupMenu(editorDocument, astNode, node, tree).show(tree, e.getX(), e.getY());
            }
        }
    }
}
