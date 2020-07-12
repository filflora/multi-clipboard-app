package sample;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyboardListener implements NativeKeyListener, NativeMouseInputListener {
    private WinDef.HWND lastActiveWindow;

//    public interface User32 extends W32APIOptions {
//
//
//        User32 instance = (User32) Native.load("user32", User32.class,
//                DEFAULT_OPTIONS);
//
//
//        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);
//
//        boolean SetForegroundWindow(WinDef.HWND hWnd);
//        WinDef.HWND GetForegroundWindow();
//
//        WinDef.HWND FindWindow(String winClass, String title);
//
//        int SW_SHOW = 1;
//
//    }

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.load("user32", User32.class);

        WinDef.HWND GetForegroundWindow();  // add this

        boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);

        boolean SetForegroundWindow(WinDef.HWND hWnd);

        WinDef.HWND SetFocus(WinDef.HWND hWnd);

        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);

        int GetWindowTextA(PointerType hWnd, byte[] lpString, int nMaxCount);
    }

    Set<Integer> pressedKeys = new HashSet<>();
    Stage primaryStage;
    Long lastOpenTriggered = new Date().getTime();
    Logger logger;

    KeyboardListener(Stage primaryStage) {
        logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        this.primaryStage = primaryStage;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
//        System.out.println(e.getKeyCode());
        pressedKeys.remove(e.getKeyCode());
        pressedKeys.add(e.getKeyCode());

        // Use Shift + Alt + V to show history
        if (pressedKeys.contains(NativeKeyEvent.VC_SHIFT) &&
                pressedKeys.contains(NativeKeyEvent.VC_ALT) &&
                pressedKeys.contains(NativeKeyEvent.VC_V) &&
                lastOpenTriggered + 1000 < new Date().getTime()) {

            Platform.runLater(() -> {
                lastOpenTriggered = new Date().getTime();


                byte[] windowText = new byte[512];
                this.lastActiveWindow = User32.INSTANCE.GetForegroundWindow(); // then you can call it!
                User32.INSTANCE.GetWindowTextA(this.lastActiveWindow, windowText, 512);

//                primaryStage.setAlwaysOnTop(true);

                final User32 user32 = User32.INSTANCE;
                user32.EnumWindows((hWnd, arg1) -> {
                    // byte[] windowText = new byte[512];
                    user32.GetWindowTextA(hWnd, windowText, 512);
                    String wText = Native.toString(windowText);

                    // get rid of this if block if you want all windows regardless of whether
                    // or not they have text
                    if (wText.isEmpty()) {
                        return true;
                    }

//                    System.out.println("wText: " + wText);
                    if (wText.contains("Clipboard history")) {
                        System.out.println("Set foreground window: " + wText);

                        // I know I know, this is a hack but it's working...
                        // https://stackoverflow.com/questions/54821655/jna-user32-showwindow-with-java-util-scanner-doesnt-work
                        user32.ShowWindow(hWnd, WinUser.SW_SHOWMINIMIZED);
                        user32.ShowWindow(hWnd, WinUser.SW_RESTORE);
                        user32.SetFocus(hWnd);
                        return false;
                    }

                    return true;
                }, null);

                TextField searchField = (TextField) primaryStage.getScene().lookup("#searchField");
                searchField.requestFocus();
            });
        }

        // ESC
        if (pressedKeys.contains(NativeKeyEvent.VC_ESCAPE)) {
            Platform.runLater(() -> {
//                primaryStage.setAlwaysOnTop(false);
                primaryStage.toBack();
            });
        }


    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        if (primaryStage.isFocused() == false) {
            Platform.runLater(() -> {
//                primaryStage.setAlwaysOnTop(false);
                primaryStage.toBack();
            });
        }
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }

    public WinDef.HWND getLastActiveWindow() {
        return lastActiveWindow;
    }
}
