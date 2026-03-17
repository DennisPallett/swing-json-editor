package nl.pallett.jsoneditor.view.editor;

import java.util.EventListener;

@FunctionalInterface
public interface CaretPositionListener extends EventListener {
    void onCaretPositionChanged(int start, int end, int line, int column);
}
