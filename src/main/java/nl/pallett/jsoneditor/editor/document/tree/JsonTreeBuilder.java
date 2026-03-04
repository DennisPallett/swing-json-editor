package nl.pallett.jsoneditor.editor.document.tree;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.util.*;

public class JsonTreeBuilder {

    public static TreeItem<JsonTreeNode> build(String jsonText)
            throws IOException {

        //JsonFactory factory = new JsonFactory();
        //JsonParser parser = factory.createParser(jsonText);
        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(jsonText);

        Deque<TreeItem<JsonTreeNode>> treeStack = new ArrayDeque<>();
        Deque<String> pathStack = new ArrayDeque<>();
        Deque<Context> contextStack = new ArrayDeque<>();

        TreeItem<JsonTreeNode> rootItem = null;

        long lastFieldStart = 0;

        while (true) {

            JsonToken token = parser.nextToken();
            if (token == null) break;

            switch (token) {

                case START_OBJECT: {
                    long start = parser.getTokenLocation().getCharOffset();

                    String pointer = buildPointer(pathStack);
                    JsonTreeNode node =
                            new JsonTreeNode("{ }", pointer);

                    TreeItem<JsonTreeNode> item =
                            new TreeItem<>(node);

                    attach(treeStack, item);
                    treeStack.push(item);

                    contextStack.push(new Context(ContextType.OBJECT, start));
                    break;
                }

                case END_OBJECT: {
                    Context ctx = contextStack.pop();
                    long end = parser.getCurrentLocation().getCharOffset();

                    TreeItem<JsonTreeNode> item = treeStack.pop();
                    item.getValue().setRange(
                            new IndexRange((int) ctx.startOffset, (int) end)
                    );

                    removePathIfField(pathStack);
                    break;
                }

                case START_ARRAY: {
                    //long start = parser.getTokenLocation().getCharOffset();
                    long start = lastFieldStart;

                    String pointer = buildPointer(pathStack);
                    JsonTreeNode node =
                            new JsonTreeNode(pathStack.getFirst(), "[ ]", pointer, JsonTreeNode.Type.ARRAY);

                    TreeItem<JsonTreeNode> item =
                            new TreeItem<>(node);

                    attach(treeStack, item);
                    treeStack.push(item);

                    contextStack.push(new Context(ContextType.ARRAY, start));
                    break;
                }

                case END_ARRAY: {
                    Context ctx = contextStack.pop();
                    long end = parser.getCurrentLocation().getCharOffset();

                    TreeItem<JsonTreeNode> item = treeStack.pop();
                    item.getValue().setRange(
                            new IndexRange((int) ctx.startOffset, (int) end)
                    );

                    removePathIfArray(pathStack);
                    break;
                }

                case FIELD_NAME: {
                    pathStack.push(parser.getCurrentName());
                    lastFieldStart = parser.getTokenLocation().getCharOffset();
                    break;
                }

                default: { // scalar value

                    long start = parser.getTokenLocation().getCharOffset();
                    long end = parser.getCurrentLocation().getCharOffset();

                    String pointer = buildPointer(pathStack);

                    JsonTreeNode node =
                            new JsonTreeNode(pathStack.getFirst(), parser.getValueAsString(), pointer, JsonTreeNode.Type.STRING);

                    node.setRange(
                            new IndexRange((int) start, (int) end)
                    );

                    TreeItem<JsonTreeNode> item =
                            new TreeItem<>(node);

                    attach(treeStack, item);

                    removePathIfField(pathStack);
                    break;
                }
            }

            handleArrayIndex(token, contextStack, pathStack);
            if (rootItem == null && !treeStack.isEmpty()) {
                rootItem = treeStack.getLast();
            }
        }

        parser.close();
        return rootItem;
    }

    // =============================

    private static void attach(Deque<TreeItem<JsonTreeNode>> stack,
                               TreeItem<JsonTreeNode> child) {

        if (stack.isEmpty()) return;

        stack.peek().getChildren().add(child);
    }

    private static void handleArrayIndex(JsonToken token,
                                         Deque<Context> contextStack,
                                         Deque<String> pathStack) {

        if (contextStack.isEmpty()) return;

        Context parent = contextStack.peek();

        if (parent.type == ContextType.ARRAY &&
                (token == JsonToken.START_OBJECT ||
                        token == JsonToken.START_ARRAY ||
                        token.isScalarValue())) {

            pathStack.push(String.valueOf(parent.arrayIndex));
            parent.arrayIndex++;
        }
    }

    private static void removePathIfField(Deque<String> stack) {
        if (!stack.isEmpty()) stack.pop();
    }

    private static void removePathIfArray(Deque<String> stack) {
        if (!stack.isEmpty()) stack.removeFirst();
        if (!stack.isEmpty()) stack.removeFirst();
    }

    private static String buildPointer(Deque<String> stack) {
        if (stack.isEmpty()) return "";
        List<String> list = new ArrayList<>(stack);
        Collections.reverse(list);
        return "/" + String.join("/", list);
    }

    private enum ContextType { OBJECT, ARRAY }

    private static class Context {
        final ContextType type;
        final long startOffset;
        int arrayIndex = 0;

        Context(ContextType type, long startOffset) {
            this.type = type;
            this.startOffset = startOffset;
        }
    }
}
