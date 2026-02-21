package nl.pallett.jsoneditor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class JsonService {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static String format(String json) throws IOException {
        Object jsonObj = mapper.readValue(json, Object.class);
        return mapper.writeValueAsString(jsonObj);
    }

    public static boolean isValid(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
