package nl.pallett.jsoneditor;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonTreeNode {

    public enum Type {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL
    }

    private final String key;
    private final String value;
    private final Type type;
    private final JsonPath path;
    private final JsonNode jsonNode;

    public JsonTreeNode(String key, String value, Type type, JsonPath path, JsonNode jsonNode) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.path = path;
        this.jsonNode = jsonNode;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public Type getType() { return type; }
    public JsonPath getPath() { return path; }
    public JsonNode getJsonNode() { return jsonNode; }
}
