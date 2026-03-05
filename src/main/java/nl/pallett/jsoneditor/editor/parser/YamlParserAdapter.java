package nl.pallett.jsoneditor.editor.parser;

import nl.pallett.jsoneditor.editor.ast.AstNode;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.*;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class YamlParserAdapter implements FormatParser {

    @Override
    public AstNode parse(String text) {

        LoadSettings settings = LoadSettings.builder().build();

        StreamReader reader = new StreamReader(settings, text);
        ScannerImpl scanner = new ScannerImpl(settings, reader);
        Parser parser = new org.snakeyaml.engine.v2.parser.ParserImpl(settings, scanner);

        Deque<AstNode> stack = new ArrayDeque<>();
        AstNode root = new AstNode(AstNode.Type.DOCUMENT, null, null);

        stack.push(root);

        String currentKey = null;

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

                    AstNode obj = new AstNode(AstNode.Type.OBJECT, currentKey, null);
                    setStart(obj, event.getStartMark());

                    if (mapStart.getAnchor().isPresent())
                        obj.setAnchor(mapStart.getAnchor().get().getValue());

                    stack.peek().addChild(obj);
                    stack.push(obj);

                    currentKey = null;

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

                    AstNode arr = new AstNode(AstNode.Type.ARRAY, currentKey, null);
                    setStart(arr, event.getStartMark());

                    if (seqStart.getAnchor().isPresent())
                        arr.setAnchor(seqStart.getAnchor().get().getValue());

                    stack.peek().addChild(arr);
                    stack.push(arr);

                    currentKey = null;

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

                    if (currentKey == null &&
                            stack.peek().getType() == AstNode.Type.OBJECT) {

                        currentKey = scalar.getValue();

                        AstNode prop =
                                new AstNode(AstNode.Type.PROPERTY, currentKey, null);
                        setStart(prop, event.getStartMark());
                        setEnd(prop, event.getEndMark());

                        stack.peek().addChild(prop);
                        stack.push(prop);

                    } else {

                        AstNode value =
                                new AstNode(AstNode.Type.VALUE, null, scalar.getValue());
                        setStart(value, event.getStartMark());
                        setEnd(value, event.getEndMark());

                        if (scalar.getAnchor().isPresent())
                            value.setAnchor(scalar.getAnchor().get().getValue());

                        detectBlockStyle(value, scalar);

                        stack.peek().addChild(value);

                        if (stack.peek().getType() == AstNode.Type.PROPERTY)
                            stack.pop();

                        currentKey = null;
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

    private void detectBlockStyle(AstNode node, ScalarEvent scalar) {

        switch (scalar.getScalarStyle()) {

            case LITERAL:
            case FOLDED:
                node.setValueType(AstNode.ValueType.BLOCK);
                break;

            default:
                node.setValueType(AstNode.ValueType.STRING);
        }
    }
}