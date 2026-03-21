package nl.pallett.jsoneditor.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import nl.pallett.jsoneditor.model.DocumentType;
import org.jspecify.annotations.Nullable;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.api.lowlevel.Present;
import org.snakeyaml.engine.v2.api.lowlevel.Serialize;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.nodes.Node;

public class StringUtil {
    private StringUtil() {
        /* This utility class should not be instantiated */
    }

    public static String getLeadingWhitespace(String line) {
        int i = 0;
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        return line.substring(0, i);
    }

    public static String formatCode(DocumentType documentType, String content) throws JsonProcessingException {
        if (content == null || content.isBlank()) {
            return content;
        }

        if (documentType == DocumentType.YAML) {
            return formatYaml(content);
        } else {
            return formatJson(content);
        }
    }

    public static String formatJson(String jsonString) throws JsonProcessingException {
        var objectMapper = ObjectMapperUtil.getJsonInstance();
        Object object = objectMapper.readValue(jsonString, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(object);
    }

    public static String formatYaml(String yamlString) {

        // 1️⃣ Parse to Node tree with comments
        LoadSettings loadSettings = LoadSettings.builder()
                .setParseComments(true)
                .build();

        Compose compose = new Compose(loadSettings);
        Optional<Node> node = compose.composeReader(new StringReader(yamlString));

        // 2️⃣ Dump Node while preserving comments
        DumpSettings dumpSettings = DumpSettings.builder()
                .setDumpComments(true)
                .setIndent(2)
                .build();

        Serialize serialize = new Serialize(dumpSettings);
        Present present = new Present(dumpSettings);

        List<Event> events = serialize.serializeOne(node.get());
        return present.emitToString(events.iterator());
    }

    public static @Nullable String convertOjectTreeToString(@Nullable Object objectTree, DocumentType documentType
    ) throws JsonProcessingException {
        if (objectTree == null) {
            return null;
        }

        ObjectMapper objectMapper = ObjectMapperUtil.getInstance(documentType);
        return objectMapper.writeValueAsString(objectTree);
    }

    public static @Nullable DocumentType detectFormat(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        String trimmed = content.trim();

        // Quick Check: JSON usually starts with { or [
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                ObjectMapperUtil.getJsonInstance().readTree(trimmed);
                return DocumentType.JSON;
            } catch (IOException e) {
                // Not valid JSON, fall through to YAML check
            }
        }

        // Try YAML
        try {
            ObjectMapperUtil.getYamlInstance().readTree(trimmed);
            // Since YAML is so permissive, we check if it's more than just a plain string
            return (trimmed.contains(":") || trimmed.startsWith("-")) ? DocumentType.YAML : null;
        } catch (IOException e) {
            return null;
        }
    }
}
