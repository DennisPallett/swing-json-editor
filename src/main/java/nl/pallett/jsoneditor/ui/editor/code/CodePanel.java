package nl.pallett.jsoneditor.ui.editor.code;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.view.editor.CaretPositionListener;
import nl.pallett.jsoneditor.view.editor.CodePanelView;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class CodePanel extends JPanel implements CodePanelView {
    private final EditorDocument editorDocument;

    private final RSyntaxTextArea textArea;

    private final AdaptiveScroller scroller = new AdaptiveScroller();

    private final StatusBar statusBar;

    public CodePanel (EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(editorDocument.getContents(), 20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);

        statusBar = new StatusBar();

        add(sp, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        initModelListener();
        initChangeListener();
    }

    @Override
    public void addCaretListener(CaretPositionListener listener) {
        textArea.addCaretListener(event -> {
            int caretPos = textArea.getCaretPosition();

            int line = 0;
            int column = 0;
            try {
                line = textArea.getLineOfOffset(caretPos);
                int lineStart = textArea.getLineStartOffset(line);
                column = caretPos - lineStart;
            } catch (BadLocationException e) {
                // ignore for now
            }
            listener.onCaretPositionChanged(event.getDot(), event.getMark(), line+1, column);
        });
    }

    @Override
    public void updateStatusBar(int line, int column) {
        statusBar.updateStatusBar(line, column);
    }

    @Override
    public void undo() {
        textArea.undoLastAction();
    }

    @Override
    public void redo() {
        textArea.redoLastAction();
    }

    @Override
    public boolean canUndo() {
        return textArea.canUndo();
    }

    @Override
    public boolean canRedo() {
        return textArea.canRedo();
    }

    @Override
    public void scrollTo(int offset, Runnable runWhenFinished) {
        scroller.scrollToOffsetAdaptive(textArea, offset, runWhenFinished);
    }

    private void initModelListener() {
        editorDocument.addPropertyChangeListener(evt -> {
            if (EditorDocument.Property.CONTENTS.name().equals(evt.getPropertyName())) {
                EditorDocument.ContentsChangedEvent newContentsEvent = (EditorDocument.ContentsChangedEvent) evt.getNewValue();

                // Avoid updating if the editor already has the same text
                if (!textArea.getText().equals(newContentsEvent.newContent())) {
                    // Must update on EDT
                    SwingUtilities.invokeLater(() -> textArea.setText(newContentsEvent.newContent()));
                }
            }
        });
    }

    private void onTextChanged () {
        String text = textArea.getText();
        if (!text.equals(editorDocument.getContents())) {
            editorDocument.setContents(text, EditorDocument.ContentsSource.TEXTAREA);
        }
    }

    private void initChangeListener() {
        Timer debounceTimer = new Timer(300, e -> onTextChanged());
        debounceTimer.setRepeats(false);

        textArea.getDocument().addDocumentListener(new DocumentListener() {

            private void restartTimer() {
                debounceTimer.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                restartTimer();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                restartTimer();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                restartTimer();
            }
        });
    }
}
