package nl.pallett.jsoneditor.ui.editor.code;

import nl.pallett.jsoneditor.model.EditorDocument;
import nl.pallett.jsoneditor.model.EditorDocument.Property;
import nl.pallett.jsoneditor.view.editor.CaretPositionListener;
import nl.pallett.jsoneditor.view.editor.CodePanelView;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class CodePanel extends JPanel implements CodePanelView {
    private final EditorDocument editorDocument;

    private final RSyntaxTextArea textArea;

    private final AdaptiveScroller scroller = new AdaptiveScroller();

    private final StatusBar statusBar;

    private final CodeToolBar toolBar;

    public CodePanel (EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setLayout(new BorderLayout());

        toolBar = new CodeToolBar(editorDocument);

        add(toolBar, BorderLayout.NORTH);

        textArea = new RSyntaxTextArea(editorDocument.getContents(), 20, 60);
        textArea.setCodeFoldingEnabled(true);
        setSyntaxStyle();
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);

        add(scrollPane, BorderLayout.CENTER);

        statusBar = new StatusBar(editorDocument);

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
            if (Property.CONTENTS.name().equals(evt.getPropertyName())) {
                EditorDocument.ContentsChangedEvent newContentsEvent = (EditorDocument.ContentsChangedEvent) evt.getNewValue();

                // Avoid updating if the editor already has the same text
                if (!textArea.getText().equals(newContentsEvent.newContent())) {
                    // Must update on EDT
                    SwingUtilities.invokeLater(() -> textArea.setText(newContentsEvent.newContent()));
                }
            }

            if (Property.IS_VALID.name().equals(evt.getPropertyName())) {
                statusBar.updateStatusBar(editorDocument.isValid(), editorDocument.getParseException());
            }

            if (Property.DOCUMENT_TYPE.name().equals(evt.getPropertyName())) {
                setSyntaxStyle();
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

    private void setSyntaxStyle() {
        String syntaxStyle = switch(editorDocument.getDocumentType()) {
            case JSON -> SyntaxConstants.SYNTAX_STYLE_JSON;
            case YAML -> SyntaxConstants.SYNTAX_STYLE_YAML;
        };
        textArea.setSyntaxEditingStyle(syntaxStyle);
    }
}
