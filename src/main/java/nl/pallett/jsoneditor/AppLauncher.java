package nl.pallett.jsoneditor;

/**
 * Custom launcher Main class to be able to launch without using javafx:run command
 */
public class AppLauncher {
    public static void main(String[] args) {
        System.out.println("applauncher");
        SwingJsonEditorApp.main(args);
    }
}
