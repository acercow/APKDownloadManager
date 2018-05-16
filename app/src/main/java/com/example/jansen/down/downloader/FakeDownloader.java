package com.example.jansen.down.downloader;

import android.content.Intent;
import android.widget.Toast;

import com.example.jansen.down.utils.ToastUtil;


public class FakeDownloader extends AbstractBaseAPKDownloader {
    @Override
    protected String subDir() {
        return "sdf";
    }

    @Override
    protected void onDownloadDuplicate(RepositoryData repositoryData) {
        ToastUtil.makeToast(getApplicationContext(), "onDownloadDuplicate", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDownloadStart(RepositoryData repositoryData) {
        ToastUtil.makeToast(getApplicationContext(), "onDownloadStart", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onInstallingFileNotExist(RepositoryData repositoryData) {
        ToastUtil.makeToast(getApplicationContext(), "onInstallingFileNotExist", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onFileNotInQueue(long downloadId) {
        ToastUtil.makeToast(getApplicationContext(), "onFileNotInQueue", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDownloadCanceled(RepositoryData repositoryData) {
        ToastUtil.makeToast(getApplicationContext(), "onDownloadCanceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDownloadCompleted(RepositoryData repositoryData) {
        ToastUtil.makeToast(getApplicationContext(), "onDownloadComplete", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDownloadFailed(RepositoryData repositoryData) {
        ToastUtil.makeToast(getApplicationContext(), "onDownloadFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onClickNotification(RepositoryData repositoryData) {
        ToastUtil.makeToast(getApplicationContext(), "onClickNotification", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDeliveredSchemeError(Intent intent) {
        ToastUtil.makeToast(getApplicationContext(), "onDeliveredSchemeError", Toast.LENGTH_SHORT).show();
    }
}

