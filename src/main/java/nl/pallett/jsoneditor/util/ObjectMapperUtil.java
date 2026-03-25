package nl.pallett.jsoneditor.util;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import nl.pallett.jsoneditor.model.DocumentType;

public class ObjectMapperUtil {
    private static ObjectMapper objectMapperJson;

    static {
        objectMapperJson = new ObjectMapper();
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter()
                .withArrayIndenter(new DefaultIndenter("  ", "\n"))   // 4 spaces + newline
                .withObjectIndenter(new DefaultIndenter("  ", "\n"));
        objectMapperJson.setDefaultPrettyPrinter(printer);
    }

    private static ObjectMapper objectMapperYaml;
    static {
        objectMapperYaml = new ObjectMapper(new YAMLFactory());
    }

    public static ObjectMapper getJsonInstance() {
        return objectMapperJson;
    }

    public static ObjectMapper getYamlInstance() {
        return objectMapperYaml;
    }

    public static ObjectMapper getInstance(DocumentType editorMode) {
        return switch(editorMode) {
            case JSON -> getJsonInstance();
            case YAML -> getYamlInstance();
        };
    }
}
