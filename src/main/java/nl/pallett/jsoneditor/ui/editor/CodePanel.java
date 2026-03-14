package nl.pallett.jsoneditor.ui.editor;

import nl.pallett.jsoneditor.model.EditorDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class CodePanel extends JPanel {
    private final EditorDocument editorDocument;

    private final RSyntaxTextArea textArea;

    public CodePanel (EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        add(sp);

        textArea.setText(editorDocument.getContents());

        initModelListener();
        initChangeListener();
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
