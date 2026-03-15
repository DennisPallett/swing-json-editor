package nl.pallett.jsoneditor.editor.parser;

import nl.pallett.jsoneditor.editor.ast.AstNode;

import java.io.IOException;

public interface FormatParser {
    AstNode parse(String text) throws IOException;
}
