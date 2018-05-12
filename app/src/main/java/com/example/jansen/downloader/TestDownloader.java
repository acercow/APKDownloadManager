package com.example.jansen.downloader;

import android.widget.Toast;

public class TestDownloader extends BaseAPKDownloader {
    @Override
    protected String subDir() {
        return null;
    }

    @Override
    protected void onDownloadDuplicate() {
        Toast.makeText(getApplicationContext(), "onDownloadDuplicate", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDownloadStart() {
        Toast.makeText(getApplicationContext(), "onDownloadStart", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onInstallingFileNotExist(long downloadId) {
        Toast.makeText(getApplicationContext(), "onInstallingFileNotExist", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onFileNotInQueue(long downloadId) {
        Toast.makeText(getApplicationContext(), "onFileNotInQueue", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDownloadComplete(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onDownloadComplete", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onClickNotification(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onClickNotification", Toast.LENGTH_SHORT).show();
    }
}
