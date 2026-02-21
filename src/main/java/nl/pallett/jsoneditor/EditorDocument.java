package nl.pallett.jsoneditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import nl.pallett.jsoneditor.util.HashUtil;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.nio.file.Path;

public class EditorDocument {

    private Path path;
    private CodeArea codeArea;
    private BooleanProperty dirty = new SimpleBooleanProperty(false);

    public EditorDocument(Path path, String content) {
        this.path = path;
        JsonCodeEditor editor = new JsonCodeEditor();
        this.codeArea = editor.getCodeArea();

        codeArea.replaceText(content);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        long initialChecksum = HashUtil.crc32(content);

        codeArea.textProperty().addListener((obs, oldVal, newVal) -> {
            long newChecksum = HashUtil.crc32(newVal);
            dirty.set(initialChecksum != newChecksum);
        });
    }

    public CodeArea getEditor() { return codeArea; }
    public BooleanProperty dirtyProperty() { return dirty; }
    public Path getPath() { return path; }
}
