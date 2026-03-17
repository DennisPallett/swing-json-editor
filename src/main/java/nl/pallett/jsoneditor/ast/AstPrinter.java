package nl.pallett.jsoneditor.ast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class AstPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AstPrinter.class);

    public static void printAst(AstNode root) {
        System.out.println(astToString(root, 0));
    }

    public static void logAst(Consumer<String> logFunction, AstNode root) {
        logFunction.accept(astToString(root, 0));
    }

    private static String astToString(AstNode node, int indent) {
        if (node == null) return "";

        StringBuilder sb = new StringBuilder();

        // indentation
        for (int i = 0; i < indent; i++) sb.append("  ");

        sb.append(node);
        sb.append("\n");

        // recurse into children
        for (AstNode child : node.getChildren()) {
            sb.append(astToString(child, indent + 1));
        }

        return sb.toString();
    }
}