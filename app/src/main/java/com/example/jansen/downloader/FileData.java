package com.example.jansen.downloader;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileData implements Parcelable {
    private static final String DEFAULT_FILE_APK_NAME = "weibo_download";

    private String uri;
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
        this.uri = uri;
        this.isInvokeInstall = isEvokeInstall;
        this.isAllowDuplicated = isAllowDuplicated;
        this.title = title;
        this.description = description;
        if (TextUtils.isEmpty(fileName)) {
            fileName = DEFAULT_FILE_APK_NAME;
        }
        if (fileName.endsWith(".apk")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        this.fileName = fileName + "-" + suffix(fileName) + ".apk";
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isInvokeInstall() {
        return isInvokeInstall;
    }

    public boolean isAllowDuplicated() {
        return isAllowDuplicated;
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

    public String getFileName() {
        return fileName;
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

    private String suffix(String fileName) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
       return dateFormat.format(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uri);
        dest.writeByte(this.isInvokeInstall ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isAllowDuplicated ? (byte) 1 : (byte) 0);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.fileName);
        dest.writeLong(this.downloadId);
    }

    protected FileData(Parcel in) {
        this.uri = in.readString();
        this.isInvokeInstall = in.readByte() != 0;
        this.isAllowDuplicated = in.readByte() != 0;
        this.title = in.readString();
        this.description = in.readString();
        this.fileName = in.readString();
        this.downloadId = in.readLong();
    }

    public static final Creator<FileData> CREATOR = new Creator<FileData>() {
        @Override
        public FileData createFromParcel(Parcel source) {
            return new FileData(source);
        }

        @Override
        public FileData[] newArray(int size) {
            return new FileData[size];
        }
    };
}
