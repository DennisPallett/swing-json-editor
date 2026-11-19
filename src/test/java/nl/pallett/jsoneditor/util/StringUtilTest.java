package nl.pallett.jsoneditor.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class StringUtilTest {

    @Test
    void formatJsonKeepsDuplicateTopLevelKeys() throws JsonProcessingException {
        String formatted = StringUtil.formatJson("{\"a\":1,\"b\":2,\"a\":3}");

        assertEquals("""
                {
                  "a" : 1,
                  "b" : 2,
                  "a" : 3
                }""", formatted);
    }

    @Test
    void formatJsonKeepsDuplicateNestedKeys() throws JsonProcessingException {
        String formatted = StringUtil.formatJson("{\"o\":{\"x\":1,\"x\":2},\"l\":[{\"y\":true,\"y\":false}]}");

        assertEquals("""
                {
                  "o" : {
                    "x" : 1,
                    "x" : 2
                  },
                  "l" : [
                    {
                      "y" : true,
                      "y" : false
                    }
                  ]
                }""", formatted);
    }

    @Test
    void formatJsonKeepsNumberLiterals() throws JsonProcessingException {
        String formatted = StringUtil.formatJson("[1.0,12345678901234567890]");

        assertEquals("""
                [
                  1.0,
                  12345678901234567890
                ]""", formatted);
    }

    @Test
    void formatJsonThrowsOnInvalidJson() {
        assertThrows(JsonProcessingException.class, () -> StringUtil.formatJson("{\"a\":"));
    }
}
