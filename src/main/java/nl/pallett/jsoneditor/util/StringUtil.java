package nl.pallett.jsoneditor.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import nl.pallett.jsoneditor.editor.EditorMode;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.api.lowlevel.Present;
import org.snakeyaml.engine.v2.api.lowlevel.Serialize;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.nodes.Node;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

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

    public static String formatCode(EditorMode editorMode, JsonNode node) throws JsonProcessingException {
        String prettyCode = ObjectMapperUtil.getInstance(editorMode)
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(node);

        if (editorMode == EditorMode.YAML) {
            return formatYaml(prettyCode);
        } else {
            return prettyCode;
        }
    }

    public static String formatYaml(String input) {

        // 1️⃣ Parse to Node tree with comments
        LoadSettings loadSettings = LoadSettings.builder()
                .setParseComments(true)
                .build();

        Compose compose = new Compose(loadSettings);
        Optional<Node> node = compose.composeReader(new StringReader(input));

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
}
