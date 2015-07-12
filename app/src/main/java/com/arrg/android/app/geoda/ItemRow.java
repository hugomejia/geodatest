package com.arrg.android.app.geoda;

import java.io.Serializable;

public class ItemRow implements Serializable {

    private boolean isGroupHeader = false;

    private String nameOfFile;
    private String pathOfFile;

    public ItemRow(String title) {
        this(title, null);
        isGroupHeader = true;
    }

    public ItemRow(String nameOfFile, String pathOfFile) {
        this.nameOfFile = nameOfFile;
        this.pathOfFile = pathOfFile;
    }

    public String getNameOfFile() {
        return nameOfFile;
    }

    public void setNameOfFile(String nameOfFile) {
        this.nameOfFile = nameOfFile;
    }

    public String getPathOfFile() {
        return pathOfFile;
    }

    public void setPathOfFile(String pathOfFile) {
        this.pathOfFile = pathOfFile;
    }

    public boolean isGroupHeader() {
        return isGroupHeader;
    }

    public void setIsGroupHeader(boolean isGroupHeader) {
        this.isGroupHeader = isGroupHeader;
    }

    @Override
    public String toString() {
        return "ItemRow{" +
                "nameOfFile='" + nameOfFile + '\'' +
                ", pathOfFile='" + pathOfFile + '\'' +
                '}';
    }
}
