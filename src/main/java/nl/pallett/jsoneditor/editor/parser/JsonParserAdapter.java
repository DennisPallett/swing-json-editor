package nl.pallett.jsoneditor.editor.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import nl.pallett.jsoneditor.editor.ast.AstNode;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class JsonParserAdapter implements FormatParser {

    @Override
    public AstNode parse(String text) throws IOException {

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(text);

        Deque<AstNode> stack = new ArrayDeque<>();
        Deque<String> pointerStack = new ArrayDeque<>();
        Deque<Integer> arrayIndexStack = new ArrayDeque<>();
        Deque<Boolean> inArrayStack = new ArrayDeque<>();

        AstNode root = null;
        String currentField = null;

        pointerStack.push("$");

        while (true) {

            JsonToken token = parser.nextToken();
            if (token == null) break;

            JsonLocation startLoc = parser.getTokenLocation();
            JsonLocation endLoc = parser.getCurrentLocation();

            switch (token) {

                case START_OBJECT: {

                    AstNode obj = new AstNode(
                            AstNode.Type.OBJECT,
                            currentField,
                            null
                    );

                    setStart(obj, startLoc);
                    setPointer(obj, pointerStack);

                    if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek())) setArrayIndex(obj, arrayIndexStack);

                    attach(stack, obj);
                    stack.push(obj);

                    if (root == null)
                        root = obj;

                    if (currentField != null)
                        pointerStack.push(currentField);

                    currentField = null;

                    inArrayStack.push(false);

                    break;
                }

                case END_OBJECT: {
                    inArrayStack.removeFirst();

                    AstNode obj = stack.pop();
                    setEnd(obj, endLoc);

                    if (!pointerStack.isEmpty())
                        pointerStack.pop();

                    if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek())) incrementArrayIndex(arrayIndexStack);

                    if (!stack.isEmpty() &&
                            stack.peek().getType() == AstNode.Type.PROPERTY) {
                        stack.pop();
                    }



                    break;
                }

                case START_ARRAY: {
                    AstNode arr = new AstNode(
                            AstNode.Type.ARRAY,
                            currentField,
                            null
                    );

                    setStart(arr, startLoc);
                    setPointer(arr, pointerStack);
                    if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek())) setArrayIndex(arr, arrayIndexStack);

                    attach(stack, arr);
                    stack.push(arr);

                    arrayIndexStack.push(0);

                    if (currentField != null)
                        pointerStack.push(currentField);

                    currentField = null;

                    inArrayStack.push(true);

                    break;
                }

                case END_ARRAY: {

                    AstNode arr = stack.pop();
                    setEnd(arr, endLoc);

                    arrayIndexStack.pop();
                    inArrayStack.removeFirst();

                    if (!pointerStack.isEmpty())
                        pointerStack.pop();

                    if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek())) incrementArrayIndex(arrayIndexStack);

                    if (!stack.isEmpty() &&
                            stack.peek().getType() == AstNode.Type.PROPERTY) {
                        stack.pop();
                    }



                    break;
                }

                case FIELD_NAME: {

                    currentField = parser.getCurrentName();

                    AstNode prop = new AstNode(
                            AstNode.Type.PROPERTY,
                            currentField,
                            null
                    );

                    setStart(prop, startLoc);
                    setEnd(prop, endLoc);

                    attach(stack, prop);
                    stack.push(prop);

                    break;
                }

                default: { // scalar values

                    AstNode valueNode = new AstNode(
                            AstNode.Type.VALUE,
                            null,
                            parser.getValueAsString()
                    );

                    setStart(valueNode, startLoc);
                    setEnd(valueNode, endLoc);

                    if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek())) setArrayIndex(valueNode, arrayIndexStack);

                    detectValueType(valueNode, token);

                    if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek()) && !arrayIndexStack.isEmpty()) {
                        int index = arrayIndexStack.peek();
                        pointerStack.push(String.valueOf(index));
                    } else {
                        if (currentField != null) pointerStack.push(currentField);
                        currentField = null;
                    }

                    setPointer(valueNode, pointerStack);

                    attach(stack, valueNode);

                    if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek()) && !arrayIndexStack.isEmpty()) {
                        pointerStack.pop();
                        incrementArrayIndex(arrayIndexStack);
                    } else {
                        if (!pointerStack.isEmpty()) pointerStack.removeFirst();
                    }

                    if (!stack.isEmpty() &&
                            stack.peek().getType() == AstNode.Type.PROPERTY) {

                        AstNode prop = stack.pop();
                        setEnd(prop, endLoc);
                    }

                    break;
                }
            }
        }

        parser.close();
        return root;
    }

    private void attach(Deque<AstNode> stack, AstNode node) {
        if (!stack.isEmpty()) {
            stack.peek().addChild(node);
        }
    }

    private void setStart(AstNode node, JsonLocation loc) {

        node.startOffset = (int) loc.getCharOffset();
        node.startLine = loc.getLineNr();
        node.startColumn = loc.getColumnNr();
    }

    private void setEnd(AstNode node, JsonLocation loc) {

        node.endOffset = (int) loc.getCharOffset();
        node.endLine = loc.getLineNr();
        node.endColumn = loc.getColumnNr();
    }

    private void setArrayIndex(AstNode node, Deque<Integer> arrayIndexStack) {
        if (!arrayIndexStack.isEmpty()) {
            node.setArrayIndex(arrayIndexStack.peek());
        }
    }

    private void setPointer(AstNode node, Deque<String> pointerStack) {

        if (pointerStack.size() == 1) {
            node.setPointer(pointerStack.getFirst()); // root node
            return;
        }

        StringBuilder sb = new StringBuilder();

        String[] arr = new String[pointerStack.size()];
        arr = pointerStack.toArray(arr);

        for (int i = arr.length - 1; i >= 0; i--) {
            String field = arr[i];
            if (field.contains(".")) field = "\"" + field + "\"";
            sb.append(field);
            if (i > 0) {
                sb.append(".");
            }
        }

        node.setPointer(sb.toString());
    }

    private void incrementArrayIndex(Deque<Integer> arrayIndexStack) {

        if (!arrayIndexStack.isEmpty()) {

            int idx = arrayIndexStack.pop();
            arrayIndexStack.push(idx + 1);
        }
    }

    private void detectValueType(AstNode node, JsonToken token) {

        switch (token) {

            case VALUE_STRING:
                node.setValueType(AstNode.ValueType.STRING);
                break;

            case VALUE_NUMBER_INT:
                node.setValueType(AstNode.ValueType.INTEGER);
                break;

            case VALUE_NUMBER_FLOAT:
                node.setValueType(AstNode.ValueType.FLOAT);
                break;

            case VALUE_TRUE, VALUE_FALSE:
                node.setValueType(AstNode.ValueType.BOOLEAN);
                break;

            case VALUE_NULL:
                node.setValueType(AstNode.ValueType.NULL);
                break;
        }
    }
}
