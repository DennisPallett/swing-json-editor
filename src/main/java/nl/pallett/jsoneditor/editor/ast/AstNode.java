package nl.pallett.jsoneditor.editor.ast;

import java.util.ArrayList;
import java.util.List;

public class AstNode {

    public enum Type {
        DOCUMENT,
        OBJECT,
        ARRAY,
        PROPERTY,
        VALUE,
        COMMENT,
        ALIAS
    }

    public enum ValueType {
        STRING,
        NUMBER,
        BOOLEAN,
        NULL,
        BLOCK
    }

    private Type type;
    private ValueType valueType;

    private String key;
    private String value;
    private String pointer;

    public int startOffset;
    public int endOffset;

    public int startLine;
    public int startColumn;

    public int endLine;
    public int endColumn;

    private AstNode parent;

    private String anchor;
    private String alias;

    private final List<AstNode> children = new ArrayList<>();

    public AstNode(Type type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public void addChild(AstNode child) {
        children.add(child);
        child.parent = this;
    }

    public List<AstNode> getChildren() {
        return children;
    }

    public Type getType() {
        return type;
    }

    public void setPointer(String pointer) {
        this.pointer = pointer;
    }

    public String getPointer() {
        return pointer;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public String getAnchor () {
        return anchor;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public int getStartOffset () {
        return startOffset;
    }

    public int getEndOffset () {
        return endOffset;
    }

    public boolean contains(int pos) {
        return pos >= startOffset && pos <= endOffset;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void prettyPrint(String indent) {
        System.out.println(indent + key + value);
        children.forEach(child -> child.prettyPrint(indent + "  "));
    }
}