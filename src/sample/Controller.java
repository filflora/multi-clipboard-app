package sample;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.jnativehook.GlobalScreen;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.awt.event.KeyEvent.*;


public class Controller implements ClipboardListener.EntryListener {

    @FXML
    public VBox vbox;

    @FXML
    public ListView listView;

    @FXML
    public TextField searchField;

    private double xOffset = 0;
    private double yOffset = 0;
    private int focusedItem = 0;
    private StorageService storageService;
    private static final Integer MAX_LENGTH = 50;
    public static ObservableList clips;
    private Stage primaryStage;
    private KeyboardListener keyboardListener;
    List<String> clipboardHistory;

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        boolean SetForegroundWindow(WinDef.HWND hWnd);

        WinDef.HWND SetFocus(WinDef.HWND hWnd);

        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);

    }


    public Controller() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
    }

    public void initialize() {
        ClipboardListener listener = new ClipboardListener();
        listener.setEntryListener(this);
        listener.start();

        VBox.setVgrow(listView, Priority.ALWAYS);

        clips = listView.getItems();

        this.storageService = new StorageService();
        clipboardHistory = this.storageService.load();
        Platform.runLater(() -> clips.setAll(clipboardHistory));

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            StringSelection strSel = new StringSelection((String) newValue);

            Platform.runLater(() -> {
                try {
                    if (strSel.getTransferData(DataFlavor.stringFlavor) != null) {

                        clipboard.setContents(strSel, null);
                        primaryStage.toBack();

                        User32.INSTANCE.ShowWindow(this.keyboardListener.getLastActiveWindow(), WinUser.SW_SHOW);
                        User32.INSTANCE.SetForegroundWindow(this.keyboardListener.getLastActiveWindow());
                        User32.INSTANCE.SetFocus(this.keyboardListener.getLastActiveWindow());

                        final Robot robot = new Robot();
                        robot.keyPress(VK_CONTROL);
                        robot.keyPress(VK_V);
                        robot.keyRelease(VK_V);
                        robot.keyRelease(VK_CONTROL);
                    }
                } catch (UnsupportedFlavorException | IOException | AWTException e) {
                    e.printStackTrace();
                }
            });
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterList(newValue));

        Platform.runLater(() -> {
            listView.getFocusModel().focus(focusedItem);
        });
    }

    @Override
    public synchronized void onCopy(String data) {
        clipboardHistory.remove(data);
        clipboardHistory.add(0, data);

        if (clipboardHistory.size() > MAX_LENGTH) {
            clipboardHistory.remove(clipboardHistory.size() - 1);
        }

        storageService.save(clipboardHistory);

        Platform.runLater(() -> clips.setAll(clipboardHistory));

    }

    public void onCloseClick() {
        Platform.exit();
    }

    private void filterList(String q) {
//        List<String> filteredList = clipboardHistory
//                .stream()
//                .filter(item -> item.toLowerCase().indexOf(q.toLowerCase()) > -1)
//                .collect(Collectors.toList());
        List<String> filteredList;

        if (q.isEmpty()) {
            filteredList = clipboardHistory;
        } else {
            List<ExtractedResult> results = FuzzySearch.extractSorted(q, clipboardHistory);
            filteredList = results
                    .stream()
                    .filter(result -> result.getScore() > 0)
                    .map(result -> result.getString())
                    .collect(Collectors.toList());
        }

        focusedItem = 0;
        listView.getFocusModel().focus(focusedItem);

        Platform.runLater(() -> clips.setAll(filteredList));
    }

    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) {
            focusedItem = focusedItem < listView.getItems().size() - 1 ? focusedItem + 1 : listView.getItems().size() - 1;
            listView.getFocusModel().focus(focusedItem);

            if (focusedItem > 20) {
                listView.scrollTo(focusedItem);
            }
        } else if (event.getCode() == KeyCode.UP) {
            focusedItem = focusedItem > 0 ? focusedItem - 1 : 0;
            listView.getFocusModel().focus(focusedItem);

            if (focusedItem > 20) {
                listView.scrollTo(focusedItem);
            }

        } else if (event.getCode() == KeyCode.ENTER) {
            listView.getSelectionModel().select(focusedItem);
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setKeyboardListener(KeyboardListener keyboardListener) {
        this.keyboardListener = keyboardListener;
    }

    public void resetUi() {
        this.focusedItem = 1;
        searchField.textProperty().setValue("");
    }

}
