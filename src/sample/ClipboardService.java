package sample;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardService {

    static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static boolean setClipBoard(String newValue){
        try {
            StringSelection selection = new StringSelection(newValue);
            clipboard.setContents(selection, selection);
            return true;
        } catch (HeadlessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getClipBoard(){
        try {
            return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
