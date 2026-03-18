package nl.pallett.jsoneditor.ui.editor.code;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nl.pallett.jsoneditor.model.EditorDocument;

public class StatusBar extends JPanel {
    private final JLabel validLabel;

    private final JLabel positionLabel;

    private final EditorDocument editorDocument;

    public StatusBar(EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        validLabel = new JLabel(" ");
        positionLabel = new JLabel(" ");

        // "Valid JSON ✅"
        // ❌ Invalid JSON

        add(validLabel);
        add(Box.createHorizontalGlue()); // pushes next items right
        //add(new JLabel("UTF-8"));
        add(Box.createHorizontalStrut(10));
        add(positionLabel);

        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8) );
    }

    public void updateStatusBar(int line, int column) {
        positionLabel.setText("Ln " + line + ", Col " + column);
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
            } else {
                message += ": " + exception.getMessage();
            }

            validLabel.setToolTipText(exception.getMessage());
        }
        validLabel.setText(message);
    }

}
