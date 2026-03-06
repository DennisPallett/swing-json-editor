package nl.pallett.jsoneditor.editor.ast;

public class AstPrinter {

    public static void printAst(AstNode root) {
        printAst(root, 0);
    }

    private static void printAst(AstNode node, int indent) {
        if (node == null) return;

        StringBuilder sb = new StringBuilder();

        // indentation
        for (int i = 0; i < indent; i++) sb.append("  ");

        // node type
        sb.append(node.getType());

        // key for property or value with key
        if (node.getKey() != null) sb.append(" key=").append(node.getKey());

        // value if scalar
        if (node.getValue() != null) {
            sb.append(" value=").append(node.getValue());
            sb.append(" (").append(node.getValueType()).append(")");
        }

        // YAML-specific info
        if (node.getAnchor() != null) sb.append(" anchor=").append(node.getAnchor());
        if (node.getAlias() != null) sb.append(" alias=").append(node.getAlias());

        // optional: offsets
        sb.append(" [").append(node.startOffset).append(", ").append(node.endOffset).append("]");

        // print the line
        System.out.println(sb.toString());

        // recurse into children
        for (AstNode child : node.getChildren()) {
            printAst(child, indent + 1);
        }
    }
}