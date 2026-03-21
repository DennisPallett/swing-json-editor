package nl.pallett.jsoneditor.view.editor;

public interface CodePanelView {
    void undo();
    void redo();
    boolean canUndo();
    boolean canRedo();
    void scrollTo(int offset, Runnable runWhenFinished);
    void addCaretListener(CaretPositionListener listener);
    void updateStatusBar(int line, int column);

    void showFindDialog();
    void showReplaceDialog();
    void showGotoLineDialog();
}
