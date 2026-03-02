package nl.pallett.jsoneditor;

import org.jspecify.annotations.NullMarked;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;

import static nl.pallett.jsoneditor.SwingJsonEditorApp.APP_ID;

@NullMarked
public class SingleInstanceManager {

    private static final int PORT = computePort(APP_ID);

    private SingleInstanceManager() {
        /* This utility class should not be instantiated */
    }

    public static boolean startServer(Consumer<List<String>> onFilesReceived) {
        try {
            ServerSocket server = new ServerSocket(PORT);

            Thread thread = new Thread(() -> {
                while (true) {
                    try (Socket socket = server.accept();
                         BufferedReader in = new BufferedReader(
                                 new InputStreamReader(socket.getInputStream()))) {

                        List<String> files = in.lines().toList();
                        onFilesReceived.accept(files);

                    } catch (IOException ignored) {}
                }
            });

            thread.setDaemon(true);
            thread.start();

            return true; // We are the primary instance

        } catch (IOException e) {
            return false; // Another instance is already running
        }
    }

    public static void sendToRunningInstance(List<String> files) {
        try (Socket socket = new Socket("localhost", PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            for (String file : files) {
                out.println(file);
            }

        } catch (IOException ignored) {}
    }

    private static int computePort(String appId) {
        int hash = Math.abs(appId.hashCode());

        int minPort = 20000;
        int maxPort = 40000;
        int range = maxPort - minPort;

        return minPort + (hash % range);
    }
}
