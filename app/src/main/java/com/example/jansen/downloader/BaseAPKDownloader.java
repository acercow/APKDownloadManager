package com.example.jansen.downloader;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tv.xiaoka.base.util.YZBLogUtil;

/**
 *
 */
public abstract class BaseAPKDownloader extends Service implements DownloadCompleteReceiver.ICallback {

    private List<FileData> mFileLists;
    private DownloadManager mDownloadManager;
    private DownloadCompleteReceiver mCompleteReceiver;
    private boolean isRegistered;

    @Override
    public void onCreate() {
        YZBLogUtil.v("APKDownload Service -> onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        YZBLogUtil.v("APKDownload Service -> onStartCommand");
        if (!isRegistered) {
            registerReceiver();
        }
        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        }
        if (mFileLists == null) {
            mFileLists = new ArrayList<>();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        YZBLogUtil.v("APKDownload Service -> onDestroy");
        if (isRegistered) {
            unRegisterReceiver();
        }
        mFileLists.clear();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        YZBLogUtil.v("APKDownload Service -> onBind");
        return null;
    }

    /**
     * Main method to invoke file download
     * @param fileData
     */
    private void download(FileData fileData) {
        makeDownloadDir();
        if (duplicateCheck(fileData)) {
            long downloadId = mDownloadManager.enqueue(new DownloadManager.Request(fileData.getUri())
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                    .setAllowedOverRoaming(true)
                    .setTitle(fileData.getTitle())
                    .setDescription(fileData.getDescription())
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileData.getFileName()));
            fileData.setDownloadId(downloadId);
            add(fileData);
            YZBLogUtil.i("fileData: " + fileData);
            downloadStart();
        } else {
            downloadDuplicate();
        }
    }

    private void makeDownloadDir() {
        String subDir = subDir();
        File destDir;
        if (TextUtils.isEmpty(subDir)) {
            destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            destDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), subDir());
        }
        if (!destDir.exists()) {
            try {
                boolean isCreated = destDir.mkdir();
                YZBLogUtil.i("mkdir: " + destDir + " [result: " + isCreated + "]" );
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generate sub-dir in DIRECTORY_DOWNLOADS
     * @return Sub-directory name, if do not need sub-dir, just return null or empty string
     */
    protected abstract String subDir();

    /**
     * Check if the file with same uri which is disallowed multiple download is downloading in progress
     *
     * @param fileData
     * @return <tt>true</tt> if is allowed to download
     */
    private boolean duplicateCheck(FileData fileData) {
        return !mFileLists.contains(fileData) || fileData.isAllowDuplicated();
    }

    private void add(FileData fileData) {
       mFileLists.add(fileData);
    }

    private FileData get(long downloadId) {
        for (FileData fileData : mFileLists) {
            if (fileData.getDownloadId() == downloadId) {
                return fileData;
            }
        }
        throw new IllegalStateException("No matched download id in queue!");
    }

    private void remove(long downloadId) {
        Iterator<FileData> iterator = mFileLists.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getDownloadId() == downloadId) {
                iterator.remove();
            }
        }
    }

    /**
     * To deal with DownloadManager's action broadcast
     * See{@link DownloadManager#ACTION_DOWNLOAD_COMPLETE} etc.
     */
    private void registerReceiver() {
        YZBLogUtil.v("BroadcastReceiver -> registered");
        isRegistered = true;
        if (mCompleteReceiver == null) {
            mCompleteReceiver = new DownloadCompleteReceiver();
        }
        mCompleteReceiver.setCompleteListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        intentFilter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        getApplicationContext().registerReceiver(mCompleteReceiver, intentFilter);
    }

    private void unRegisterReceiver() {
        YZBLogUtil.v("BroadcastReceiver -> unRegistered");
        isRegistered = false;
        if (mCompleteReceiver != null) {
            getApplicationContext().unregisterReceiver(mCompleteReceiver);
        }
    }

    @Override
    public void onClickNotification(long id) {
        clickNotification(get(id));
    }

    @Override
    public void onComplete(long id) {
        FileData fileData = get(id);
        downloadComplete(fileData);
        if (fileData.isEvokeInstall()) {
            DownloadCompleteReceiver.invokeInstall(getApplicationContext());
        }
        remove(id);
        if (mFileLists.size() < 1) {
            stopSelf();
        }
    }

    protected abstract void downloadDuplicate();
    protected abstract void downloadStart();


    protected abstract void downloadComplete(FileData fileData);
    protected abstract void clickNotification(FileData fileData);
}
