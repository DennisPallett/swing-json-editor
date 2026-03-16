package nl.pallett.jsoneditor.view;

public interface CodePanelView {
    void undo();
    void redo();
    boolean canUndo();
    boolean canRedo();
}
