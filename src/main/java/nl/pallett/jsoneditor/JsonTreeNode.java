package nl.pallett.jsoneditor;

public class JsonTreeNode {

    public enum Type {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL
    }

    private final String key;
    private final String value;
    private final Type type;
    private final JsonPath path;

    public JsonTreeNode(String key, String value, Type type, JsonPath path) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.path = path;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public Type getType() { return type; }
    public JsonPath getPath() { return path; }
}
