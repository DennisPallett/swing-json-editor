package nl.pallett.jsoneditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import nl.pallett.jsoneditor.util.HashUtil;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class EditorDocument {

    private @Nullable Path path;
    private final CodeArea codeArea;
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    private long dirtyChecksum;

    public EditorDocument(@Nullable Path path, String content) {
        this.path = path;
        JsonCodeEditor editor = new JsonCodeEditor();
        this.codeArea = editor.getCodeArea();

        codeArea.replaceText(content);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        setDirtyChecksum(content);

        codeArea.textProperty().addListener((obs, oldVal, newVal) -> {
            long newChecksum = HashUtil.crc32(newVal);
            dirty.set(dirtyChecksum != newChecksum);
        });
    }

    public void setDirtyChecksum(String content) {
        dirtyChecksum = HashUtil.crc32(content);
        dirty.set(false);
    }

    public void setFile(Path file) {
        this.path = file;
    }

    public CodeArea getEditor() { return codeArea; }
    public BooleanProperty dirtyProperty() { return dirty; }
    public @Nullable Path getPath() { return path; }
}
