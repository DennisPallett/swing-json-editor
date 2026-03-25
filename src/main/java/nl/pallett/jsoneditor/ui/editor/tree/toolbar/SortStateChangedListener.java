package nl.pallett.jsoneditor.ui.editor.tree.toolbar;

import nl.pallett.jsoneditor.ui.editor.tree.SortState;

import java.util.EventListener;

@FunctionalInterface
public interface SortStateChangedListener extends EventListener {
    void onSortStateChanged(SortState sortState);
}
