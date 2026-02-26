package nl.pallett.jsoneditor;

import java.util.ArrayList;
import java.util.List;

public record JsonPath(JsonPath parentPath, String key) {
    public List<String> toList() {
        List<String> returnList = new ArrayList<>();
        if (parentPath() != null) {
            returnList.addAll(parentPath().toList());
        }
        returnList.add(key());
        return returnList;
    }
}
