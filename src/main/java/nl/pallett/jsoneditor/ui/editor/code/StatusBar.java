package nl.pallett.jsoneditor.ui.editor.code;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import nl.pallett.jsoneditor.model.EditorDocument;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.ParserException;

import javax.swing.*;

public class StatusBar extends JPanel {
    private final JLabel validLabel;

    private final JLabel positionLabel;

    private final JLabel statusLabel;

    private final EditorDocument editorDocument;

    public StatusBar(EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        validLabel = new JLabel(" ");
        positionLabel = new JLabel(" ");
        statusLabel = new JLabel(" ");


        add(validLabel);
        add(Box.createHorizontalGlue()); // pushes next items right
        add(statusLabel);
        add(Box.createHorizontalGlue());
        add(Box.createHorizontalStrut(10));
        add(positionLabel);

        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8) );

        if (editorDocument.hasContents()) {
            updateStatusBar(editorDocument.isValid(), editorDocument.getParseException());
        }
    }

    public void updateStatusBar(int line, int column) {
        positionLabel.setText("Ln " + line + ", Col " + column);
    }

    public void updateStatusBar(String text) {
        statusLabel.setText(text);
    }

    public void updateStatusBar(boolean valid, Exception exception) {
        String message;
        if (valid) {
            message = "✅ Valid " + editorDocument.getDocumentType();
            validLabel.setToolTipText("");
        } else {
            message = "❌ Invalid " + editorDocument.getDocumentType();
            if (exception instanceof JsonParseException jsonParseException) {
                JsonLocation errorLocation = jsonParseException.getLocation();
                if (errorLocation != null) {
                    message += " at line " + errorLocation.getLineNr()
                        + ", column " + errorLocation.getColumnNr();
                }
            } else if (exception instanceof ParserException parserException) {
                 java.util.Optional<Mark> errorLocation = parserException.getProblemMark();
                if (errorLocation.isPresent()) {
                    message += " at line " + (errorLocation.get().getLine()+1)
                        + ", column " + (errorLocation.get().getColumn()+1);
                }
            }

            if (exception != null) {
                validLabel.setToolTipText(exception.getMessage());
            }
        }
        validLabel.setText(message);
    }

}
