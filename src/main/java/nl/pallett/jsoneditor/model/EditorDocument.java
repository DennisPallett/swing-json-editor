package nl.pallett.jsoneditor.model;

import nl.pallett.jsoneditor.editor.ast.AstNode;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class EditorDocument {
    private String name;

    private @Nullable Path filePath;

    private String contents;

    private @Nullable AstNode astNodeTree;

    public EditorDocument (String name, @Nullable Path filePath) {
        this.name = name;
        this.filePath = filePath;


        if (filePath != null) {
            // TODO: fetch contents if file path
        }

        setContents("");
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

    public void setContents(String contents) {
        this.contents = contents;

        // TODO: when setting (new contents) calculate AST tree
    }
}
