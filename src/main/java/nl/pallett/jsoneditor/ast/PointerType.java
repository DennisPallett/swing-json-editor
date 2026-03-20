package nl.pallett.jsoneditor.ast;

import org.jspecify.annotations.Nullable;

public sealed interface PointerType permits FieldPointer, ArrayIndexPointer, NullPointer, DocumentPointer {
    static PointerType fieldOrNullPointer(@Nullable String fieldName) {
        if (fieldName == null) {
            return new NullPointer();
        } else {
            return new FieldPointer(fieldName);
        }
    }
}
