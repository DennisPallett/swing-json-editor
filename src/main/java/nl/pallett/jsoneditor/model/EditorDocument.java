package nl.pallett.jsoneditor.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import nl.pallett.jsoneditor.editor.ast.AstNode;
import nl.pallett.jsoneditor.editor.parser.FormatParser;
import nl.pallett.jsoneditor.editor.parser.JsonParserAdapter;
import nl.pallett.jsoneditor.editor.parser.YamlParserAdapter;
import nl.pallett.jsoneditor.util.FileUtil;
import nl.pallett.jsoneditor.util.HashUtil;
import nl.pallett.jsoneditor.util.StringUtil;
import org.jspecify.annotations.Nullable;

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
        FILE_PATH
    }

    public record ContentsChangedEvent (String oldContent, String newContent, ContentsSource source) {}

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String name;

    private @Nullable Path filePath;

    private String contents = "";

    private @Nullable AstNode astTree;

    private boolean dirty = false;

    private long dirtyChecksum;

    private DocumentType documentType = DocumentType.JSON;

    public EditorDocument (String name, @Nullable Path filePath) {
        this.name = name;
        this.filePath = filePath;

        if (filePath != null) {
            try {
                setContents(Files.readString(filePath), ContentsSource.OTHER);
            } catch (IOException e) {
                // TODO: handle this error
                System.err.println("Failed to read file: " + e.getMessage());
            }

            String extension = FileUtil.getExtension(filePath);
            documentType = extension.equals("json") ? DocumentType.JSON : DocumentType.YAML;
        }

        // reset dirty mark on init to mark as "fresh"
        resetDirtyMark();
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
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

    public void setFilePath(Path filePath) {
        Path oldFilePath = this.filePath;
        this.filePath = filePath;

        pcs.firePropertyChange(Property.FILE_PATH.name(), oldFilePath, filePath);
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String newContents, ContentsSource contentsSource) {
        // don't do anything if new contents is exactly the same as the old
        if (newContents.equals(contents)) {
            return;
        }

        String old = this.contents;
        this.contents = newContents;

        pcs.firePropertyChange(Property.CONTENTS.name(), null,
            new ContentsChangedEvent(old, this.contents, contentsSource));

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
        } catch (IOException e) {
            // TODO: handle exrror
            System.err.println(e.getMessage());
        }
    }
}
