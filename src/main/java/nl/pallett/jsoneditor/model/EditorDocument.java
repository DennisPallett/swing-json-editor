package nl.pallett.jsoneditor.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.pallett.jsoneditor.ast.AstConverter;
import nl.pallett.jsoneditor.ast.AstNode;
import nl.pallett.jsoneditor.ast.parser.FormatParser;
import nl.pallett.jsoneditor.ast.parser.JsonParserAdapter;
import nl.pallett.jsoneditor.ast.parser.YamlParserAdapter;
import nl.pallett.jsoneditor.util.FileUtil;
import nl.pallett.jsoneditor.util.HashUtil;
import nl.pallett.jsoneditor.util.StringUtil;
import org.jspecify.annotations.Nullable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EditorDocument {
    public enum ContentsSource {
        TEXTAREA,
        TREE,
        OTHER
    }

    public enum Property {
        CONTENTS,
        AST_TREE,
        DIRTY_MARK,
        FILE_PATH,
        IS_VALID,
        DOCUMENT_TYPE
    }

    public record ContentsChangedEvent (String oldContent, String newContent, ContentsSource source) {}

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final AstConverter astConverter = new AstConverter();

    private String name;

    private @Nullable Path filePath;

    private String contents = "";

    private @Nullable AstNode astTree;

    private boolean dirty = false;

    private long dirtyChecksum;

    private DocumentType documentType = DocumentType.JSON;

    private boolean valid;

    private @Nullable Exception parseException = null;

    public EditorDocument (String name, @Nullable Path filePath) {
        this.name = name;
        this.filePath = filePath;

        if (filePath != null) {
            String extension = FileUtil.getExtension(filePath);
            documentType = extension.equals("json") ? DocumentType.JSON : DocumentType.YAML;

            try {
                setContents(Files.readString(filePath), ContentsSource.OTHER);
            } catch (IOException e) {
                // TODO: handle this error
                System.err.println("Failed to read file: " + e.getMessage());
            }
        }

        // reset dirty mark on init to mark as "fresh"
        resetDirtyMark();
    }

    public void setDocumentType(DocumentType documentType) {
        DocumentType oldType = this.documentType;
        this.documentType = documentType;

        pcs.firePropertyChange(Property.DOCUMENT_TYPE.name(), oldType, documentType);

        // auto-convert existing contents (using AST tree) to new document type
        String newContent = this.exportAs(documentType);
        if (newContent != null) {
            this.setContents(newContent, ContentsSource.OTHER);
        }
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path getFilePath() {
        return filePath;
    }

    public @Nullable String exportAs(DocumentType convertTo) {
        try {
            String converted = StringUtil.convertOjectTreeToString(astConverter.toObjectTree(astTree), convertTo);
            return StringUtil.formatCode(documentType, converted);
        } catch (JsonProcessingException e) {
            System.err.println(e.getStackTrace());
            return null;
        }
    }

    public void setFilePath(Path filePath) {
        Path oldFilePath = this.filePath;
        this.filePath = filePath;
        this.name = filePath.getFileName().toString();

        pcs.firePropertyChange(Property.FILE_PATH.name(), oldFilePath, filePath);
    }

    public boolean isValid() {
        return valid;
    }

    public Exception getParseException() {
        return parseException;
    }

    public boolean hasContents() {
        return contents != null && !contents.isBlank();
    }

    public String getContents() {
        return contents;
    }

    private void autoDetectDocumentType(String contents) {
        DocumentType documentType = StringUtil.detectFormat(contents);
        if (documentType != null) {
            setDocumentType(documentType);
        }
    }

    public void setContents(String newContents, ContentsSource contentsSource) {
        // don't do anything if new contents is exactly the same as the old
        if (newContents.equals(contents)) {
            return;
        }

        // when pasting in new content try to auto-detect the document type
        if (!this.hasContents() && this.getFilePath() == null) {
            autoDetectDocumentType(newContents);
        }

        String oldContents = this.contents;
        this.contents = newContents;

        pcs.firePropertyChange(Property.CONTENTS.name(), null,
            new ContentsChangedEvent(oldContents, this.contents, contentsSource));

        // when setting new contents calculate new hash for dirty property
        recalculateDirtyMark();

        // when setting (new contents) calculate AST tree
        recalculateAstTree();
    }

    public boolean isDirty() {
        return dirty;
    }

    public AstNode getAstTree() {
        return astTree;
    }


    /**
     * Used to reset the dirty mark of the document. To be called on creation and on-save
     */
    public void resetDirtyMark() {
        boolean oldDirty = dirty;

        this.dirty = false;
        dirtyChecksum = HashUtil.crc32(contents);

        pcs.firePropertyChange(Property.DIRTY_MARK.name(), oldDirty, dirty);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public boolean canBeFormatted() {
        return !contents.isEmpty();
    }

    public void formatContents() {
        if (!canBeFormatted()) {
            return;
        }

        try {
            String formatted = StringUtil.formatCode(documentType, contents);
            setContents(formatted, ContentsSource.OTHER);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private void recalculateDirtyMark() {
        boolean oldDirty = dirty;

        long newChecksum = HashUtil.crc32(contents);
        dirty = (dirtyChecksum != newChecksum);

        pcs.firePropertyChange(Property.DIRTY_MARK.name(), oldDirty, dirty);
    }

    private void recalculateAstTree() {
        AstNode oldTree = astTree;

        FormatParser parser = switch(documentType) {
            case JSON -> new JsonParserAdapter();
            case YAML -> new YamlParserAdapter();
        };
        try {
            astTree = parser.parse(getContents());
            pcs.firePropertyChange(Property.AST_TREE.name(), oldTree, astTree);

            setIsValid(true, null);
        } catch (Exception e) {
            setIsValid(false, e);
            e.printStackTrace(); // TODO: add as debug logging
        }
    }

    private void setIsValid(boolean valid, @Nullable Exception exception) {
        boolean oldValid = this.valid;
        this.valid = valid;
        this.parseException = exception;

        pcs.firePropertyChange(Property.IS_VALID.name(), oldValid, this.valid);
    }
}
