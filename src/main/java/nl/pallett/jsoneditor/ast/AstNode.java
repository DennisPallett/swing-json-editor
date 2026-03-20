package nl.pallett.jsoneditor.ast;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

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
        INTEGER,
        FLOAT,
        BOOLEAN,
        NULL,
        BLOCK,
        TIMESTAMP
    }

    public enum CommentType {
        /**
         * empty line
         */
        BLANK_LINE,
        /**
         * comment which start with #
         */
        BLOCK,
        /**
         * ending the line
         */
        IN_LINE
    }

    private Type type;
    private ValueType valueType;

    private String key;
    private String value;
    private List<PointerType> pointer;

    public int startOffset;
    public int endOffset;

    public int startLine;
    public int startColumn;

    public int endLine;
    public int endColumn;

    private AstNode parent;

    private String anchor;
    private String alias;

    private @Nullable CommentType commentType;

    private final List<AstNode> children = new ArrayList<>();

    private @Nullable Integer arrayIndex = null;

    private @Nullable Integer arraySize = null;

    public static AstNode copyOf(AstNode original) {
        AstNode copy = new AstNode(original.getType(), original.getKey(), original.getValue());
        copy.setValueType(original.getValueType());
        copy.startOffset = original.startOffset;
        copy.startLine = original.startLine;
        copy.startColumn = original.startColumn;
        copy.endOffset = original.endOffset;
        copy.endLine = original.endLine;
        copy.endColumn = original.endColumn;
        copy.setAlias(original.getAlias());
        copy.setAnchor(original.getAnchor());
        copy.setPointer(original.getPointer());
        copy.setArrayIndex(original.getArrayIndex());
        copy.setArraySize(original.getArraySize());
        original.getChildren().forEach(copy::addChild);
        return copy;
    }

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

    public void setPointer(List<PointerType> pointerList) {
        this.pointer = pointerList;
    }

    public List<PointerType> getPointer() {
        return pointer;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public ValueType getValueType () {
        return this.valueType;
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

    public void setArrayIndex (@Nullable Integer arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public @Nullable Integer getArrayIndex () {
        return arrayIndex;
    }

    public boolean isArrayItem () {
        return arrayIndex != null;
    }

    public void setArraySize(@Nullable Integer size) {
        this.arraySize = size;
    }

    public @Nullable Integer getArraySize () {
        return arraySize;
    }

    public void setCommentType (CommentType commentType) {
        this.commentType = commentType;
    }

    public @Nullable CommentType getCommentType () {
        return commentType;
    }

    public boolean hasPointer() {
        return this.pointer != null && !this.pointer.isEmpty();
    }

    public String getPointerAsJsonPath() {
        List<PointerType> filtered = this.pointer.stream()
            .filter(p -> !(p instanceof NullPointer))
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append(((FieldPointer)filtered.getFirst()).fieldName()); // root node

        for(int i=1; i < filtered.size(); i++) {
            PointerType currPointer = filtered.get(i);

            if (currPointer instanceof ArrayIndexPointer(int index)) {
                sb.append("[").append(index).append("]");
            } else if (currPointer instanceof FieldPointer(String fieldName)) {
                sb.append(".").append(fieldName);
            }
        }

        return sb.toString();
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();

        // node type
        sb.append(getType());

        // key for property or value with key
        if (getKey() != null) sb.append(" key=").append(getKey());

        if (isArrayItem()) sb.append(" array-index=").append(getArrayIndex());

        if (getArraySize() != null) sb.append(" array-size=").append(getArraySize());

        // value if scalar
        if (getValue() != null) {
            sb.append(" value=").append(getValue());

        }

        if (getValueType() != null) {
            sb.append(" (").append(getValueType()).append(")");
        }

        if (getCommentType() != null) {
            sb.append(" (").append(getCommentType()).append(")");
        }


        if (getPointer() != null) {
            sb.append(" pointer=").append(getPointerAsJsonPath());
        }

        // YAML-specific info
        if (getAnchor() != null) sb.append(" anchor=").append(getAnchor());
        if (getAlias() != null) sb.append(" alias=").append(getAlias());

        // optional: offsets
        sb.append(" [").append(startOffset).append(", ").append(endOffset).append("]");

        // print the line
        return sb.toString();
    }
}