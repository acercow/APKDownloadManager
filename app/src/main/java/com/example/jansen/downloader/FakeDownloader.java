package com.example.jansen.downloader;

import android.widget.Toast;

public class FakeDownloader extends BaseAPKDownloader {
    @Override
    protected String subDir() {
        return "ACEDownloadFolder";
    }

    @Override
    protected void onDownloadDuplicate(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onDownloadDuplicate", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDownloadStart(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onDownloadStart", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onInstallingFileNotExist(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onInstallingFileNotExist", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onFileNotInQueue(long downloadId) {
        Toast.makeText(getApplicationContext(), "onFileNotInQueue", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDownloadCanceled(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onDownloadCanceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDownloadCompleted(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onDownloadComplete", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDownloadFailed(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onDownloadFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onClickNotification(FileData fileData) {
        Toast.makeText(getApplicationContext(), "onClickNotification", Toast.LENGTH_SHORT).show();
    }
}
