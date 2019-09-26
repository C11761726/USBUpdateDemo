package com.example.usbupdaelibrary;

public class FileInfoStruct {
    private String fileName;
    private String md5;
    private String fromPath;
    private String destPath;
    private boolean isHasSame = false;

    public boolean isHasSame() {
        return isHasSame;
    }

    public void setHasSame(boolean hasSame) {
        isHasSame = hasSame;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }

    public String getDestPath() {
        return destPath;
    }

    public void setDestPath(String destPath) {
        this.destPath = destPath;
    }
}
