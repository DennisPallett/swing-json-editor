package nl.pallett.jsoneditor.editor.ast;

import org.jspecify.annotations.NonNull;

public record FieldPointer (@NonNull String fieldName) implements PointerType {
}
