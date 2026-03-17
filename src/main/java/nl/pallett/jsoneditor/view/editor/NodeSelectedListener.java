package nl.pallett.jsoneditor.view.editor;

import java.util.EventListener;
import nl.pallett.jsoneditor.editor.ast.AstNode;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface NodeSelectedListener extends EventListener {
    void onNodeSelected(@Nullable AstNode astNode);
}


