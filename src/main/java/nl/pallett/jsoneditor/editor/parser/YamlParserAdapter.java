package nl.pallett.jsoneditor.editor.parser;

import nl.pallett.jsoneditor.editor.ast.AstNode;
import nl.pallett.jsoneditor.editor.ast.FieldPointer;
import nl.pallett.jsoneditor.editor.ast.PointerType;
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
                    stack.peek().addChild(doc);
                    stack.push(doc);

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

                    if (mapStart.getAnchor().isPresent())
                        obj.setAnchor(mapStart.getAnchor().get().getValue());

                    stack.peek().addChild(obj);
                    stack.push(obj);

                    currentField = null;

                    break;
                }
                case MappingEnd: {

                    AstNode obj = stack.pop();
                    setEnd(obj, event.getEndMark());

                    if (!stack.isEmpty() && stack.peek().getType() == AstNode.Type.PROPERTY) {
                        stack.removeFirst();
                    }

                    break;
                }
                case SequenceStart: {

                    SequenceStartEvent seqStart = (SequenceStartEvent) event;

                    AstNode arr = new AstNode(AstNode.Type.ARRAY, currentField, null);
                    setStart(arr, event.getStartMark());

                    if (seqStart.getAnchor().isPresent())
                        arr.setAnchor(seqStart.getAnchor().get().getValue());

                    stack.peek().addChild(arr);
                    stack.push(arr);

                    currentField = null;

                    break;
                }
                case SequenceEnd: {

                    AstNode arr = stack.removeFirst();
                    setEnd(arr, event.getEndMark());

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

                        stack.peek().addChild(prop);
                        stack.push(prop);

                    } else {

                        AstNode value =
                                new AstNode(AstNode.Type.VALUE, null, scalar.getValue());
                        setStart(value, event.getStartMark());
                        setEnd(value, event.getEndMark());

                        value.setValueType(toValueType(detectType(scalar)));

                        if (scalar.getAnchor().isPresent())
                            value.setAnchor(scalar.getAnchor().get().getValue());

                        stack.peek().addChild(value);

                        if (stack.peek().getType() == AstNode.Type.PROPERTY)
                            stack.pop();

                        currentField = null;
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
                        switch(comment.getCommentType()) {
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

    public static AstNode.ValueType toValueType (YamlScalarType yamlType) {
        return switch (yamlType) {
            case STRING -> AstNode.ValueType.STRING;
            case INTEGER -> AstNode.ValueType.INTEGER;
            case FLOAT -> AstNode.ValueType.FLOAT;
            case BOOLEAN -> AstNode.ValueType.BOOLEAN;
            case NULL -> AstNode.ValueType.NULL;
            case TIMESTAMP -> AstNode.ValueType.TIMESTAMP;
        };
    }
}