package nl.pallett.jsoneditor.model;

import nl.pallett.jsoneditor.editor.ast.AstNode;
import nl.pallett.jsoneditor.util.HashUtil;
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
        DIRTY_MARK
    }

    public record ContentsChangedEvent (String oldContent, String newContent, ContentsSource source) {}

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String name;

    private @Nullable Path filePath;

    private String contents = "";

    private @Nullable AstNode astNodeTree;

    private boolean dirty = false;

    private long dirtyChecksum;

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
        }

        // reset dirty mark on init to mark as "fresh"
        resetDirtyMark();
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
        this.filePath = filePath;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents, ContentsSource contentsSource) {
        String old = this.contents;
        this.contents = contents;

        pcs.firePropertyChange(Property.CONTENTS.name(), null,
            new ContentsChangedEvent(old, this.contents, contentsSource));

        // when setting new contents calculate new hash for dirty property
        recalculateDirtyMark();

        // TODO: when setting (new contents) calculate AST tree
        // TODO: when setting new contents calculate new hash for dirty property
    }

    public boolean isDirty() {
        return dirty;
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

    private void recalculateDirtyMark() {
        boolean oldDirty = dirty;

        long newChecksum = HashUtil.crc32(contents);
        dirty = (dirtyChecksum != newChecksum);

        pcs.firePropertyChange(Property.DIRTY_MARK.name(), oldDirty, dirty);
    }
}
