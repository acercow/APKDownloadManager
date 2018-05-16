package com.example.jansen.down.downloader;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by zhaosen 2018-05-16
 *
 * Apk Download model, to control repository url, title in notification, auto installation, saved filename etc.
 *
 */
public class RepositoryData implements Parcelable {
    private static final String DEFAULT_FILE_APK_NAME = "weibo_download";

    private String url;
    private boolean autoInstall;
    private boolean allowDuplicated;
    private boolean downloadOverMobile;

    private String notificationTitle;
    private String notificationDescription;
    private String localFileName;

    private long downloadId; // Generated id by system

    public RepositoryData(String url, String localFileName) {
        this(url, true, false, true, "", "", localFileName);
    }

    public RepositoryData(String url, boolean autoInstall, boolean allowDuplicated, boolean downloadOverMobile, String notificationTitle, String notificationDescription, String localFileName) {
        this.url = url;
        this.autoInstall = autoInstall;
        this.allowDuplicated = allowDuplicated;
        this.downloadOverMobile = downloadOverMobile;
        this.notificationTitle = notificationTitle;
        this.notificationDescription = notificationDescription;

        if (TextUtils.isEmpty(localFileName) || "null".equals(localFileName)) { // uri解析过来可能为"null"字符串
            localFileName = DEFAULT_FILE_APK_NAME;
        }
        if (localFileName.endsWith(".apk")) {
            localFileName = localFileName.substring(0, localFileName.length() - 4);
        }
        this.localFileName = localFileName + "-" + suffix() + ".apk";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAutoInstall() {
        return autoInstall;
    }

    public void setAutoInstall(boolean autoInstall) {
        this.autoInstall = autoInstall;
    }

    public boolean isAllowDuplicated() {
        return allowDuplicated;
    }

    public void setAllowDuplicated(boolean allowDuplicated) {
        this.allowDuplicated = allowDuplicated;
    }

    public boolean isDownloadOverMobile() {
        return downloadOverMobile;
    }

    public void setDownloadOverMobile(boolean downloadOverMobile) {
        this.downloadOverMobile = downloadOverMobile;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationDescription() {
        return notificationDescription;
    }

    public void setNotificationDescription(String notificationDescription) {
        this.notificationDescription = notificationDescription;
    }

    public String getLocalFileName() {
        return localFileName;
    }

    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
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

        RepositoryData that = (RepositoryData) o;

        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    private String suffix() {
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
        dest.writeString(this.url);
        dest.writeByte(this.autoInstall ? (byte) 1 : (byte) 0);
        dest.writeByte(this.allowDuplicated ? (byte) 1 : (byte) 0);
        dest.writeByte(this.downloadOverMobile ? (byte) 1 : (byte) 0);
        dest.writeString(this.notificationTitle);
        dest.writeString(this.notificationDescription);
        dest.writeString(this.localFileName);
        dest.writeLong(this.downloadId);
    }

    protected RepositoryData(Parcel in) {
        this.url = in.readString();
        this.autoInstall = in.readByte() != 0;
        this.allowDuplicated = in.readByte() != 0;
        this.downloadOverMobile = in.readByte() != 0;
        this.notificationTitle = in.readString();
        this.notificationDescription = in.readString();
        this.localFileName = in.readString();
        this.downloadId = in.readLong();
    }

    public static final Creator<RepositoryData> CREATOR = new Creator<RepositoryData>() {
        @Override
        public RepositoryData createFromParcel(Parcel source) {
            return new RepositoryData(source);
        }

        @Override
        public RepositoryData[] newArray(int size) {
            return new RepositoryData[size];
        }
    };
}
