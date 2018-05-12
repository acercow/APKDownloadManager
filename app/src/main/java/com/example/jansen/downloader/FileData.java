package com.example.jansen.downloader;


import android.net.Uri;
import android.text.TextUtils;

public class FileData {
    private static final String DEFAULT_FILE_APK_NAME = "weibo_download";

    private Uri uri;
    private boolean isInvokeInstall;
    private boolean isAllowDuplicated;

    private String title;
    private String description;
    private String fileName;

    private long downloadId;

    public FileData(String uri, String title, String fileName) {
        this(uri, true, false, title, "", fileName);
    }

    public FileData(String uri, boolean isEvokeInstall, boolean isAllowDuplicated, String title, String description, String fileName) {
        if (TextUtils.isEmpty(uri) || !uri.startsWith("http")) { // 下载只能以http/https开头
            return;
        }
        this.uri = Uri.parse(uri);
        this.isInvokeInstall = isEvokeInstall;
        this.isAllowDuplicated = isAllowDuplicated;
        this.title = title;
        this.description = description;
        if (TextUtils.isEmpty(fileName)) {
            fileName = DEFAULT_FILE_APK_NAME;
        }
        this.fileName = fileName;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isInvokeInstall() {
        return isInvokeInstall;
    }

    public void setInvokeInstall(boolean invokeInstall) {
        isInvokeInstall = invokeInstall;
    }

    public boolean isAllowDuplicated() {
        return isAllowDuplicated;
    }

    public void setAllowDuplicated(boolean allowDuplicated) {
        isAllowDuplicated = allowDuplicated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileData fileData = (FileData) o;

        return uri.equals(fileData.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return "FileData{" +
                "uri=" + uri +
                ", isInvokeInstall=" + isInvokeInstall +
                ", isAllowDuplicated=" + isAllowDuplicated +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
