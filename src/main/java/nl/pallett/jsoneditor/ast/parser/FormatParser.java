package nl.pallett.jsoneditor.ast.parser;

import nl.pallett.jsoneditor.ast.AstNode;

import java.io.IOException;

public interface FormatParser {
    AstNode parse(String text) throws IOException;
}
