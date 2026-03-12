package nl.pallett.jsoneditor;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

public class RawAppleEventListener {

    public interface Carbon extends Library {
        Carbon INSTANCE = Native.load("Carbon", Carbon.class);

        int AEInstallEventHandler(int eventClass, int eventID, AEEventHandlerProc handler, Pointer userData, boolean onMainThread);
    }

    public interface AEEventHandlerProc extends Callback {
        int callback(Pointer nextHandler, Pointer theEvent, Pointer userData);
    }

    public static final int kCoreEventClass = 0x636F7265; // 'core'
    public static final int kAEOpenDocuments = 0x6F646F63; // 'odoc'

    public static void install() {
        AEEventHandlerProc handler = new AEEventHandlerProc() {
            @Override
            public int callback(Pointer nextHandler, Pointer theEvent, Pointer userData) {
                System.out.println("Received Apple Event: odoc");
                // You can parse theEvent for file paths using more Carbon APIs
                return 0; // noErr
            }
        };

        int err = Carbon.INSTANCE.AEInstallEventHandler(kCoreEventClass, kAEOpenDocuments, handler, null, true);
        if (err != 0) {
            System.err.println("Failed to install Apple Event handler: " + err);
        } else {
            System.out.println("Raw Apple Event handler installed");
        }
    }
}
