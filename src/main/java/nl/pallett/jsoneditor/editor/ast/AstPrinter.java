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

        sb.append(node);

        // print the line
        System.out.println(sb);

        // recurse into children
        for (AstNode child : node.getChildren()) {
            printAst(child, indent + 1);
        }
    }
}