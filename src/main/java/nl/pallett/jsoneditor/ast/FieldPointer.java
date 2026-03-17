package nl.pallett.jsoneditor.ast;

import org.jspecify.annotations.NonNull;

public record FieldPointer (@NonNull String fieldName) implements PointerType {
}
