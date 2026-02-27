package nl.pallett.jsoneditor.util;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtil {
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter()
                .withArrayIndenter(new DefaultIndenter("    ", "\n"))   // 4 spaces + newline
                .withObjectIndenter(new DefaultIndenter("    ", "\n"));
        objectMapper.setDefaultPrettyPrinter(printer);
    }

    public static ObjectMapper getInstance() {
        return objectMapper;
    }
}
