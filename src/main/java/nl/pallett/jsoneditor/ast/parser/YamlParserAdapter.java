package nl.pallett.jsoneditor.ast.parser;

import nl.pallett.jsoneditor.ast.ArrayIndexPointer;
import nl.pallett.jsoneditor.ast.AstNode;
import nl.pallett.jsoneditor.ast.FieldPointer;
import nl.pallett.jsoneditor.ast.PointerType;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.*;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class YamlParserAdapter implements FormatParser {
    private final LoadSettings settings = LoadSettings.builder().setParseComments(true).build();
    private final Deque<AstNode> stack = new ArrayDeque<>();
    private final Deque<PointerType> pointerStack = new ArrayDeque<>();
    private final Deque<Integer> arrayIndexStack = new ArrayDeque<>();
    private final Deque<Boolean> inArrayStack = new ArrayDeque<>();

    private String currentField = null;

    public enum YamlScalarType {
        STRING,
        INTEGER,
        FLOAT,
        BOOLEAN,
        NULL,
        TIMESTAMP
    }

    private static final Resolver resolver = new Resolver();

    @Override
    public AstNode parse(String text) {
        StreamReader reader = new StreamReader(settings, text);
        ScannerImpl scanner = new ScannerImpl(settings, reader);
        Parser parser = new ParserImpl(settings, scanner);

        pointerStack.push(new FieldPointer("$"));

        AstNode root = new AstNode(AstNode.Type.DOCUMENT, null, null);

        stack.push(root);

        while (parser.hasNext()) {

            Event event = parser.next();

            switch (event.getEventId()) {

                case DocumentStart: {

                    AstNode doc = new AstNode(AstNode.Type.DOCUMENT, null, null);
                    setStart(doc, event.getStartMark());

                    attachToParent(doc);
                    stack.push(doc);

                    if (root == null)
                        root = doc;

                    break;
                }
                case DocumentEnd: {
                    AstNode doc = stack.pop();
                    setEnd(doc, event.getEndMark());

                    break;
                }
                case MappingStart: {

                    MappingStartEvent mapStart = (MappingStartEvent) event;

                    AstNode obj = new AstNode(AstNode.Type.OBJECT, currentField, null);
                    setStart(obj, event.getStartMark());

                    // start as array item (if part of an array)
                    startArrayItem(obj);

                    // add current fieldname (property) or a dummy value
                    pointerStack.push(PointerType.fieldOrNullPointer(currentField));

                    // add current pointer to node
                    setPointer(obj);

                    if (mapStart.getAnchor().isPresent())
                        obj.setAnchor(mapStart.getAnchor().get().getValue());

                    attachToParent(obj);
                    stack.push(obj);

                    currentField = null;

                    // add indicator that current depth is not an array
                    inArrayStack.push(false);

                    break;
                }
                case MappingEnd: {
                    inArrayStack.removeFirst();

                    AstNode obj = stack.pop();
                    setEnd(obj, event.getEndMark());

                    finishArrayItem();

                    if (!pointerStack.isEmpty())
                        pointerStack.pop();

                    if (!stack.isEmpty() && stack.peek().getType() == AstNode.Type.PROPERTY) {
                        stack.removeFirst();
                    }

                    break;
                }
                case SequenceStart: {

                    SequenceStartEvent seqStart = (SequenceStartEvent) event;

                    AstNode arr = new AstNode(AstNode.Type.ARRAY, currentField, null);
                    setStart(arr, event.getStartMark());

                    startArrayItem(arr);
                    pointerStack.push(PointerType.fieldOrNullPointer(currentField));

                    setPointer(arr);

                    if (seqStart.getAnchor().isPresent())
                        arr.setAnchor(seqStart.getAnchor().get().getValue());

                    attachToParent(arr);
                    stack.push(arr);

                    arrayIndexStack.push(0);

                    currentField = null;
                    inArrayStack.push(true);

                    break;
                }
                case SequenceEnd: {

                    AstNode arr = stack.removeFirst();
                    setEnd(arr, event.getEndMark());

                    arr.setArraySize(arrayIndexStack.peek());

                    // current array is ending so remove latest arrayIndex counter and latest array status indication
                    arrayIndexStack.removeFirst();
                    inArrayStack.removeFirst();

                    finishArrayItem();

                    if (!pointerStack.isEmpty())
                        pointerStack.pop();

                    if (!stack.isEmpty() && stack.peek().getType() == AstNode.Type.PROPERTY) {
                        stack.removeFirst();
                    }

                    break;
                }
                case Scalar:

                    ScalarEvent scalar = (ScalarEvent) event;

                    if (currentField == null &&
                        stack.peek().getType() == AstNode.Type.OBJECT) {

                        currentField = scalar.getValue();

                        AstNode prop =
                            new AstNode(AstNode.Type.PROPERTY, currentField, null);
                        setStart(prop, event.getStartMark());
                        setEnd(prop, event.getEndMark());

                        attachToParent(prop);
                        stack.push(prop);

                    } else {

                        AstNode valueNode =
                            new AstNode(AstNode.Type.VALUE, null, scalar.getValue());
                        setStart(valueNode, event.getStartMark());
                        setEnd(valueNode, event.getEndMark());

                        valueNode.setValueType(toValueType(detectType(scalar)));

                        startArrayItem(valueNode);

                        if (currentField != null) pointerStack.push(new FieldPointer(currentField));

                        setPointer(valueNode);

                        if (scalar.getAnchor().isPresent())
                            valueNode.setAnchor(scalar.getAnchor().get().getValue());

                        attachToParent(valueNode);

                        finishArrayItem();

                        if (currentField != null && !pointerStack.isEmpty())
                            pointerStack.removeFirst();

                        currentField = null;

                        if (!stack.isEmpty() && stack.peek().getType() == AstNode.Type.PROPERTY)
                            stack.pop();
                    }

                    break;

                case Alias:

                    AliasEvent alias = (AliasEvent) event;

                    AstNode aliasNode =
                        new AstNode(AstNode.Type.ALIAS, null, null);
                    setStart(aliasNode, event.getStartMark());
                    setEnd(aliasNode, event.getEndMark());


                    aliasNode.setAlias(alias.getAlias().getValue());

                    stack.peek().addChild(aliasNode);

                    break;

                case Comment:

                    CommentEvent comment = (CommentEvent) event;

                    AstNode commentNode =
                        new AstNode(AstNode.Type.COMMENT, null, comment.getValue());
                    setStart(commentNode, event.getStartMark());
                    setEnd(commentNode, event.getEndMark());

                    commentNode.setCommentType(
                        switch (comment.getCommentType()) {
                            case BLANK_LINE -> AstNode.CommentType.BLANK_LINE;
                            case BLOCK -> AstNode.CommentType.BLOCK;
                            case IN_LINE -> AstNode.CommentType.IN_LINE;
                        }
                    );

                    stack.peek().addChild(commentNode);

                    break;
            }
        }

        return root;
    }

    private void setStart(AstNode node, Optional<Mark> optionalMark) {
        if (optionalMark.isEmpty()) return;

        Mark mark = optionalMark.get();

        node.startOffset = mark.getIndex();
        node.startLine = mark.getLine();
        node.startColumn = mark.getColumn();
    }

    private void setEnd(AstNode node, Optional<Mark> optionalMark) {
        if (optionalMark.isEmpty()) return;

        Mark mark = optionalMark.get();

        node.endOffset = mark.getIndex();
        node.endLine = mark.getLine();
        node.endColumn = mark.getColumn();
    }

    public static YamlScalarType detectType(ScalarEvent event) {

        String value = event.getValue();
        Optional<String> tag = event.getTag();

        Tag resolved = tag.map(Tag::new).orElseGet(() -> resolver.resolve(NodeId.scalar, value, true));

        if (Tag.INT.equals(resolved)) return YamlScalarType.INTEGER;
        if (Tag.FLOAT.equals(resolved)) return YamlScalarType.FLOAT;
        if (Tag.BOOL.equals(resolved)) return YamlScalarType.BOOLEAN;
        if (Tag.NULL.equals(resolved)) return YamlScalarType.NULL;
        if (Tag.TIMESTAMP.equals(resolved)) return YamlScalarType.TIMESTAMP;

        return YamlScalarType.STRING;
    }

    public static AstNode.ValueType toValueType(YamlScalarType yamlType) {
        return switch (yamlType) {
            case STRING -> AstNode.ValueType.STRING;
            case INTEGER -> AstNode.ValueType.INTEGER;
            case FLOAT -> AstNode.ValueType.FLOAT;
            case BOOLEAN -> AstNode.ValueType.BOOLEAN;
            case NULL -> AstNode.ValueType.NULL;
            case TIMESTAMP -> AstNode.ValueType.TIMESTAMP;
        };
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

    private void incrementArrayIndex() {
        if (!arrayIndexStack.isEmpty()) {
            int idx = arrayIndexStack.pop();
            arrayIndexStack.push(idx + 1);
        }
    }

    private void setArrayIndex(AstNode node) {
        if (!arrayIndexStack.isEmpty()) {
            node.setArrayIndex(arrayIndexStack.peek());
        }
    }

    private void setPointer(AstNode node) {
        node.setPointer(pointerStack.stream().toList().reversed());
    }

    private void attachToParent(AstNode node) {
        if (!stack.isEmpty()) {
            stack.peek().addChild(node);
        }
    }
}