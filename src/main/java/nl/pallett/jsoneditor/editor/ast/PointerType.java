package nl.pallett.jsoneditor.editor.ast;

import org.jspecify.annotations.Nullable;

public sealed interface PointerType permits FieldPointer, ArrayIndexPointer, NullPointer {
    static PointerType fieldOrNullPointer(@Nullable String fieldName) {
        if (fieldName == null) {
            return new NullPointer();
        } else {
            return new FieldPointer(fieldName);
        }
    }
}
