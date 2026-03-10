package nl.pallett.jsoneditor.editor.parser;

import nl.pallett.jsoneditor.editor.ast.AstNode;

public interface FormatParser {
    AstNode parse(String text) throws Exception;
}
