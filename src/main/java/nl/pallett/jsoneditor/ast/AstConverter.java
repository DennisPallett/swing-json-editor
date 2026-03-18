package nl.pallett.jsoneditor.ast;

import org.jspecify.annotations.Nullable;

import java.util.*;

public class AstConverter {

    public @Nullable Object toObjectTree(AstNode astTree) {
        if (astTree == null) {
            return null;
        }

        Object root = buildNode(astTree);
        return root;
    }

    private Object buildNode(AstNode astNode) {
        Object result = null;
        switch (astNode.getType()) {
            case OBJECT: {
                Map<String, Object> map = new HashMap<>();
                astNode.getChildren().stream()
                    .map(childNode -> MapEntry.of(childNode.getKey(), buildNode(childNode)))
                    .filter(entry -> entry.value() != null)
                    .forEach(entry -> map.put(entry.key(), entry.value));
                result = map;
                break;
            }
            case ARRAY: {
                List<Object> list = new ArrayList<>();
                astNode.getChildren().stream()
                    .map(this::buildNode)
                    .filter(Objects::nonNull)
                    .forEach(list::add);
                result = list;
                break;
            }
            case PROPERTY: {
                result = buildNode(astNode.getChildren().getFirst());
                break;
            }
            case VALUE: {
                try {
                    result = switch (astNode.getValueType()) {
                        case STRING -> astNode.getValue();
                        case INTEGER -> Integer.valueOf(astNode.getValue());
                        case FLOAT -> Float.valueOf(astNode.getValue());
                        case BOOLEAN -> Boolean.valueOf(astNode.getValue());
                        case NULL -> null;
                        case BLOCK -> astNode.getValue();
                        case TIMESTAMP -> astNode.getValue();
                    };
                } catch (NumberFormatException _) {
                    result = astNode.getValue();
                }
                break;
            }
            case DOCUMENT:
                AstNode firstChildNode = astNode.getChildren().stream()
                    .filter(childNode -> childNode.getType() != AstNode.Type.COMMENT)
                    .findFirst()
                    .orElse(null);
                result = firstChildNode != null ? buildNode(firstChildNode) : null;
                break;
            case COMMENT, ALIAS:
                result = null;
                break;
        }

        return result;
    }

    private record MapEntry (String key, Object value) {
        public static MapEntry of (String key, Object value) {
            return new MapEntry(key, value);
        }
    }
}
