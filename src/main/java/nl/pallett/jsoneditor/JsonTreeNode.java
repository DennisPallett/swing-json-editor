package nl.pallett.jsoneditor;

public class JsonTreeNode {

    public enum Type {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL
    }

    private final String key;
    private final String value;
    private final Type type;

    public JsonTreeNode(String key, String value, Type type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public Type getType() { return type; }
}
