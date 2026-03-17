package nl.pallett.jsoneditor.view.editor;

import nl.pallett.jsoneditor.ast.AstNode;
import org.jspecify.annotations.Nullable;

import java.util.EventListener;

@FunctionalInterface
public interface NodeSelectedListener extends EventListener {
    void onNodeSelected(@Nullable AstNode astNode);
}


