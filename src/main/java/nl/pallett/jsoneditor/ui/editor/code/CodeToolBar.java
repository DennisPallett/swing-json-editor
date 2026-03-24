package nl.pallett.jsoneditor.ui.editor.code;

import nl.pallett.jsoneditor.model.DocumentType;
import nl.pallett.jsoneditor.model.EditorDocument;

import javax.swing.*;
import java.awt.*;

public class CodeToolBar extends JToolBar {
    private final EditorDocument editorDocument;

    private final JToggleButton jsonButton;

    private final JToggleButton yamlButton;

    public CodeToolBar(EditorDocument editorDocument) {
        this.editorDocument = editorDocument;

        setFloatable(false);

        jsonButton = new JToggleButton("JSON");
        jsonButton.setMargin(new Insets(5, 5, 5, 5));
        yamlButton = new JToggleButton("YAML");
        yamlButton.setMargin(new Insets(5, 5, 5, 5));

        setSelectedMode();

        ButtonGroup group = new ButtonGroup();
        group.add(jsonButton);
        group.add(yamlButton);

        jsonButton.setSelected(editorDocument.getDocumentType() == DocumentType.JSON);
        yamlButton.setSelected(editorDocument.getDocumentType() == DocumentType.YAML);

        add(Box.createHorizontalGlue());
        add(jsonButton);
        add(Box.createHorizontalStrut(5));
        add(yamlButton);

        jsonButton.addActionListener(event -> changeType(DocumentType.JSON));
        yamlButton.addActionListener(event -> changeType(DocumentType.YAML));

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        editorDocument.addPropertyChangeListener(changeEvent -> {
            if (changeEvent.getPropertyName().equals(EditorDocument.Property.DOCUMENT_TYPE.name())) {
                setSelectedMode();
            }
        });
    }

    private void changeType(DocumentType newType) {
        if (editorDocument.getDocumentType() == newType) {
            // do nothing
        }

        // show warning when converting from YAML -> JSON because comments are lost
        if (newType == DocumentType.JSON && editorDocument.hasContents()) {
            int result = JOptionPane.showConfirmDialog(
                this.getParent(),
                "When converting from JSON to YAML all comments will be lost.\n\nAre you sure you want to continue?",
                "Converting to JSON",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (result == JOptionPane.NO_OPTION) {
                return;
            }
        }

        editorDocument.setDocumentType(newType);
    }

    private void setSelectedMode() {
        jsonButton.setSelected(editorDocument.getDocumentType() == DocumentType.JSON);
        yamlButton.setSelected(editorDocument.getDocumentType() == DocumentType.YAML);
    }
}
