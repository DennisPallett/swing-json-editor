module nl.pallett.jsoneditor {
    requires java.desktop;
    requires java.prefs;
    requires java.base;

    requires javafx.controls;

    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires org.controlsfx.controls;
    requires reactfx;

    requires org.jspecify;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    requires org.slf4j;

    requires org.snakeyaml.engine.v2;
    requires org.yaml.snakeyaml;
    requires javafx.graphics;
    requires com.sun.jna;

    exports nl.pallett.jsoneditor;
}