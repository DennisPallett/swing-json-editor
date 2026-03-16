package nl.pallett.jsoneditor.view.editor;

public interface CodePanelView {
    void undo();
    void redo();
    boolean canUndo();
    boolean canRedo();
    void scrollTo(int offset);
}
