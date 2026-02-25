package nl.pallett.jsoneditor.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.prefs.Preferences;

public class Settings {

    private static final Preferences prefs =
            Preferences.userNodeForPackage(Settings.class);

    public static BooleanProperty prefBoolean(String key, boolean defaultValue) {
        BooleanProperty property =
                new SimpleBooleanProperty(prefs.getBoolean(key, defaultValue));

        property.addListener((obs, oldVal, newVal) ->
                prefs.putBoolean(key, newVal));

        return property;
    }
}