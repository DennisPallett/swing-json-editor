package nl.pallett.jsoneditor.editor.document.tree;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.IndexRange;
import nl.pallett.jsoneditor.editor.document.JsonPath;

public class JsonTreeNode {

    public enum Type {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL
    }

    private final String key;
    private final String value;
    private final Type type;
    private final JsonPath path;
    private final JsonNode jsonNode;
    private final String pointer;
    private IndexRange range;

    public JsonTreeNode(String key, String value, Type type, JsonPath path, JsonNode jsonNode) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.path = path;
        this.jsonNode = jsonNode;
        this.pointer = null;
    }

    public JsonTreeNode(String displayName, String pointer) {
        this.key = displayName;
        this.pointer = pointer;
        this.value = null;
        this.type = Type.OBJECT;
        this.path = null;
        this.jsonNode = null;
    }

    public JsonTreeNode(String key, String value, String pointer, Type type) {
        this.key = key;
        this.pointer = pointer;
        this.value = value;
        this.type = type;
        this.path = null;
        this.jsonNode = null;
    }

    public String getPointer() { return pointer; }

    public IndexRange getRange() { return range; }

    public void setRange(IndexRange range) {
        this.range = range;
    }


    public String getKey() { return key; }
    public String getValue() { return value; }
    public Type getType() { return type; }
    public JsonPath getPath() { return path; }
    public JsonNode getJsonNode() { return jsonNode; }
}
