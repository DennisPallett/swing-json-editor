package nl.pallett.jsoneditor.view.editor;

public interface TreePanelView {
    void addNodeSelectedListener(NodeSelectedListener listener);
    void selectNodeForCaretPosition(int caretPosition);
}
