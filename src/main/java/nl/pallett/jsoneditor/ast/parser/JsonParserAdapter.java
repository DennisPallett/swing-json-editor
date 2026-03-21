package nl.pallett.jsoneditor.ast.parser;

import com.fasterxml.jackson.core.*;
import nl.pallett.jsoneditor.ast.ArrayIndexPointer;
import nl.pallett.jsoneditor.ast.AstNode;
import nl.pallett.jsoneditor.ast.FieldPointer;
import nl.pallett.jsoneditor.ast.PointerType;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class JsonParserAdapter implements FormatParser {

    private final JsonFactory factory = new JsonFactory();

    private final Deque<AstNode> stack = new ArrayDeque<>();
    private final Deque<PointerType> pointerStack = new ArrayDeque<>();
    private final Deque<Integer> arrayIndexStack = new ArrayDeque<>();
    private final Deque<Boolean> inArrayStack = new ArrayDeque<>();

    private String currentField = null;

    public JsonParserAdapter() {

    }

    @Override
    public AstNode parse(String text) throws IOException, JsonParseException {
        JsonParser parser = factory.createParser(text);

        AstNode root = new AstNode(AstNode.Type.DUMMY_ROOT, null, null);
        stack.push(root);

        pointerStack.push(new FieldPointer("$"));

        while (true) {

            JsonToken token = parser.nextToken();
            if (token == null) break;

            JsonLocation startLoc = parser.currentTokenLocation();
            JsonLocation endLoc = parser.currentLocation();

            switch (token) {

                case START_OBJECT: {
                    // create new OBJECT node
                    AstNode obj = new AstNode(
                        AstNode.Type.OBJECT,
                        currentField,
                        null
                    );

                    // set start location of node
                    setStart(obj, startLoc);

                    // start as array item (if part of an array)
                    startArrayItem(obj);

                    // add current fieldname (property) or a dummy value
                    pointerStack.push(PointerType.fieldOrNullPointer(currentField));

                    // add current pointer to node
                    setPointer(obj);

                    attachToParent(obj);
                    stack.push(obj);

                    if (root == null)
                        root = obj;

                    currentField = null;

                    // add indicator that current depth is not an array
                    inArrayStack.push(false);

                    break;
                }

                case END_OBJECT: {
                    inArrayStack.removeFirst();

                    AstNode obj = stack.pop();
                    setEnd(obj, endLoc);

                    finishArrayItem();

                    if (!pointerStack.isEmpty())
                        pointerStack.pop();

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

                    startArrayItem(arr);
                    pointerStack.push(PointerType.fieldOrNullPointer(currentField));

                    setPointer(arr);

                    attachToParent(arr);
                    stack.push(arr);

                    arrayIndexStack.push(0);

                    currentField = null;

                    inArrayStack.push(true);

                    break;
                }

                case END_ARRAY: {

                    AstNode arr = stack.pop();
                    setEnd(arr, endLoc);

                    arr.setArraySize(arrayIndexStack.peek());

                    // current array is ending so remove latest arrayIndex counter and latest array status indication
                    arrayIndexStack.removeFirst();
                    inArrayStack.removeFirst();

                    finishArrayItem();

                    if (!pointerStack.isEmpty())
                        pointerStack.pop();

                    if (!stack.isEmpty() &&
                        stack.peek().getType() == AstNode.Type.PROPERTY) {
                        stack.pop();
                    }

                    break;
                }

                case FIELD_NAME: {

                    currentField = parser.currentName() ;

                    AstNode prop = new AstNode(
                        AstNode.Type.PROPERTY,
                        currentField,
                        null
                    );

                    setStart(prop, startLoc);
                    setEnd(prop, endLoc);

                    attachToParent(prop);
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
                    detectValueType(valueNode, token);

                    startArrayItem(valueNode);

                    if (currentField != null) pointerStack.push(new FieldPointer(currentField));

                    setPointer(valueNode);

                    attachToParent(valueNode);

                    finishArrayItem();

                    if (currentField != null && !pointerStack.isEmpty())
                        pointerStack.removeFirst();

                    currentField = null;

                    if (!stack.isEmpty() && stack.peek().getType() == AstNode.Type.PROPERTY) {
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

    private void startArrayItem(AstNode node) {
        if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek()) && !arrayIndexStack.isEmpty()) {
            setArrayIndex(node);
            int index = arrayIndexStack.peek();
            pointerStack.push(new ArrayIndexPointer(index));
        }
    }

    private void finishArrayItem() {
        if (!inArrayStack.isEmpty() && Boolean.TRUE.equals(inArrayStack.peek()) && !arrayIndexStack.isEmpty()) {
            if (!pointerStack.isEmpty()) pointerStack.removeFirst();
            incrementArrayIndex();
        }
    }

    private void attachToParent(AstNode node) {
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

    private void setArrayIndex(AstNode node) {
        if (!arrayIndexStack.isEmpty()) {
            node.setArrayIndex(arrayIndexStack.peek());
        }
    }

    private void setPointer(AstNode node) {
        node.setPointer(pointerStack.stream().toList().reversed());
    }

    private void incrementArrayIndex() {

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
