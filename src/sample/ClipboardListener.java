package sample;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class ClipboardListener extends Thread implements ClipboardOwner {

    interface EntryListener {
        void onCopy(String data);
    }

    private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private EntryListener entryListener;

    public void setEntryListener(EntryListener entryListener) {
        this.entryListener = entryListener;
    }

    @Override
    public void lostOwnership(Clipboard c, Transferable t) {
        try {
            sleep(200);
        } catch (Exception e) {
        }

        Transferable contents = clipboard.getContents(this);
        processContents(contents);
        repairOwnership(contents);
    }

    public void processContents(Transferable clipData) {
        try {
            String what = (String) (clipData.getTransferData(DataFlavor.stringFlavor));

            if (entryListener != null) {
                entryListener.onCopy(what);
            }
        } catch (Exception e) {}
    }

    public void repairOwnership(Transferable t) {
        clipboard.setContents(t, this);
    }

    public void run() {
        Transferable transferable = clipboard.getContents(this);
        repairOwnership(transferable);

        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
