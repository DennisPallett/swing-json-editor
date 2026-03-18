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
            DefaultMutableTreeNode newRoot = treeBuilder.buildTree(astTree);
            tree.setModel(new DefaultTreeModel(newRoot));

            astIntervalIndex = new AstIntervalIndex(newRoot);
        }
    }
}
