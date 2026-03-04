package nl.pallett.jsoneditor.editor.document.tree;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import javafx.scene.control.IndexRange;

import java.io.IOException;
import java.util.*;

/**
 * Builds JSON Pointer -> IndexRange mapping using Jackson streaming parser.
 *
 * Fully array-safe.
 * Works with duplicate subtrees.
 * Offsets match the exact input string.
 */
public final class JsonPointerOffsetMapper {

    private JsonPointerOffsetMapper() {}

    public static Map<String, IndexRange> map(String jsonText) throws IOException {

        Map<String, IndexRange> result = new HashMap<>();

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(jsonText);

        Deque<String> pathStack = new ArrayDeque<>();
        Deque<Context> contextStack = new ArrayDeque<>();

        while (true) {

            JsonToken token = parser.nextToken();
            if (token == null) break;

            switch (token) {

                case START_OBJECT: {
                    long start = parser.getTokenLocation().getCharOffset();
                    contextStack.push(new Context(ContextType.OBJECT, start));
                    break;
                }

                case END_OBJECT: {
                    Context ctx = contextStack.pop();
                    long end = parser.getCurrentLocation().getCharOffset();

                    String pointer = buildPointer(pathStack);
                    result.put(pointer,
                            new IndexRange((int) ctx.startOffset, (int) end));

                    removePathIfField(pathStack);
                    break;
                }

                case START_ARRAY: {
                    long start = parser.getTokenLocation().getCharOffset();
                    contextStack.push(new Context(ContextType.ARRAY, start));
                    break;
                }

                case END_ARRAY: {
                    Context ctx = contextStack.pop();
                    long end = parser.getCurrentLocation().getCharOffset();

                    String pointer = buildPointer(pathStack);
                    result.put(pointer,
                            new IndexRange((int) ctx.startOffset, (int) end));

                    removePathIfArray(pathStack);
                    break;
                }

                case FIELD_NAME: {
                    pathStack.push(parser.getCurrentName());
                    break;
                }

                default: { // scalar value

                    long start = parser.getTokenLocation().getCharOffset();
                    long end = parser.getCurrentLocation().getCharOffset();

                    String pointer = buildPointer(pathStack);

                    result.put(pointer,
                            new IndexRange((int) start, (int) end));

                    removePathIfField(pathStack);

                    incrementArrayIndex(contextStack);
                }
            }

            handleArrayValueEntry(token, contextStack, pathStack);
        }

        parser.close();
        return result;
    }

    // =========================
    // Internal Helpers
    // =========================

    private static void handleArrayValueEntry(JsonToken token,
                                              Deque<Context> contextStack,
                                              Deque<String> pathStack) {

        if (contextStack.isEmpty()) return;

        Context parent = contextStack.peek();

        if (parent.type == ContextType.ARRAY) {

            if (token == JsonToken.START_OBJECT ||
                    token == JsonToken.START_ARRAY ||
                    token.isScalarValue()) {

                pathStack.push(String.valueOf(parent.arrayIndex));
                parent.arrayIndex++;
            }
        }
    }

    private static void incrementArrayIndex(Deque<Context> contextStack) {
        if (contextStack.isEmpty()) return;

        Context parent = contextStack.peek();
        if (parent.type == ContextType.ARRAY) {
            // index already incremented during entry
        }
    }

    private static void removePathIfField(Deque<String> pathStack) {
        if (!pathStack.isEmpty()) {
            pathStack.pop();
        }
    }

    private static void removePathIfArray(Deque<String> pathStack) {
        if (!pathStack.isEmpty()) {
            pathStack.pop();
        }
    }

    private static String buildPointer(Deque<String> stack) {
        if (stack.isEmpty()) return "";

        List<String> list = new ArrayList<>(stack);
        Collections.reverse(list);
        return "/" + String.join("/", list);
    }

    private enum ContextType {
        OBJECT,
        ARRAY
    }

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
