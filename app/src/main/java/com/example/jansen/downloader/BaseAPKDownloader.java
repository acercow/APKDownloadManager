package com.example.jansen.downloader;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhaosen 2018-05-12
 */
public abstract class BaseAPKDownloader extends Service implements DownloadCompleteReceiver.ICallback {
    private static final String TAG = BaseAPKDownloader.class.getSimpleName();
    public static final String EXTRA_DOWNLOAD_FILE_DATA = "com.acercow.extra.filedata";
    private List<FileData> mFileLists;
    private DownloadManager mDownloadManager;
    private DownloadCompleteReceiver mCompleteReceiver;
    private boolean isRegistered;
    private File mDestDir;

    @Override
    public void onCreate() {
        Log.v(TAG, "APKDownload Service -> onCreate");
        super.onCreate();

        if (!isRegistered) {
            registerReceiver();
        }
        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        }
        if (mFileLists == null) {
            mFileLists = new ArrayList<>();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "APKDownload Service -> onStartCommand");
        FileData fileData = intent.getParcelableExtra(EXTRA_DOWNLOAD_FILE_DATA);
        if (fileData != null) {
            if (!TextUtils.isEmpty(fileData.getUri()) && fileData.getUri().startsWith("http")) { // 下载只能以http/https开头
                download(fileData);
            } else {
                Log.i(TAG, "The download uri must start with http or https!");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "APKDownload Service -> onDestroy");
        if (isRegistered) {
            unRegisterReceiver();
        }
        mFileLists.clear();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "APKDownload Service -> onBind");
        return null;
    }

    /**
     * Main method to invoke file download
     *
     * @param fileData
     */
    private void download(FileData fileData) {
        makeDownloadDir(); // check each time when download start to ensure dir's validation
        if (passDuplicateCheck(fileData)) {
            long downloadId = mDownloadManager.enqueue(new DownloadManager.Request(Uri.parse(fileData.getUri()))
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                    .setAllowedOverRoaming(true)
                    .setTitle(fileData.getTitle())
                    .setDescription(fileData.getDescription())
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileData.getFileName()));
            fileData.setDownloadId(downloadId);
            add(fileData);
            Log.i(TAG, "fileData: " + fileData);
            onDownloadStart(fileData);
        } else {
            onDownloadDuplicate(fileData);
        }
    }

    private void makeDownloadDir() {
        if (mDestDir == null) {
            if (TextUtils.isEmpty(subDir())) {
                mDestDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            } else {
                mDestDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), subDir());
            }
        }
        if (!mDestDir.exists()) {
            try {
                boolean isCreated = mDestDir.mkdir();
                Log.i(TAG, "mkdir: " + mDestDir + " [result: " + isCreated + "]");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private File getDestFile(long id) {
        FileData fileData = get(id);
        if (fileData == null) {
            return null;
        }
        File destApk = new File(mDestDir, fileData.getFileName());
        if (!destApk.exists()) {
            onInstallingFileNotExist(fileData);
            return null;
        }
        Log.i(TAG, "Dest apk: " + destApk);
        return destApk;
    }

    /**
     * Generate sub-dir in DIRECTORY_DOWNLOADS
     *
     * @return Sub-directory name, if do not need sub-dir, just return null or empty string
     */
    protected abstract String subDir();

    /**
     * Check if the file with same uri which is disallowed multiple download is downloading in progress
     *
     * @param fileData
     * @return <tt>true</tt> if is allowed to download
     */
    private boolean passDuplicateCheck(FileData fileData) {
        // If is duplicate and disallow duplicate
        boolean isDuplicate = mFileLists.contains(fileData) && !fileData.isAllowDuplicated();
        if (isDuplicate) {
            FileData oldFileData = mFileLists.get(mFileLists.indexOf(fileData));
            int status = checkStatus(oldFileData.getDownloadId());
            return  (status == -1 || status == DownloadManager.STATUS_FAILED || status == DownloadManager.STATUS_SUCCESSFUL);
        } else {
            return true;
        }

        // if download is interrupt or finish
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
        onFileNotInQueue(downloadId);
        return null;
    }

    private void remove(long downloadId) {
        Iterator<FileData> iterator = mFileLists.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getDownloadId() == downloadId) {
                iterator.remove();
                Log.i(TAG, "removed item: " + downloadId);

            }
        }
        if (mFileLists.size() < 1) {
            stopSelf();
        }
    }

    /**
     * To deal with DownloadManager's action broadcast
     * See{@link DownloadManager#ACTION_DOWNLOAD_COMPLETE} etc.
     */
    private void registerReceiver() {
        Log.v(TAG, "BroadcastReceiver -> registered");
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
        Log.v(TAG, "BroadcastReceiver -> unRegistered");
        isRegistered = false;
        if (mCompleteReceiver != null) {
            getApplicationContext().unregisterReceiver(mCompleteReceiver);
        }
    }

    @Override
    public void onClickNotification(long id) {
        Log.i(TAG, "BroadcastReceiver -> onClickNotification: " + id);
        FileData fileData = get(id);
        if (fileData == null) {
            return;
        }
        onClickNotification(fileData);
    }

    @Override
    public void onComplete(long id) {
        Log.i(TAG, "BroadcastReceiver -> onComplete: " + id);
        FileData fileData = get(id);
        if (fileData == null) {
            return;
        }
        // User canceling download progress also receives COMPLETE broaddcast
        if (checkStatus(id) == -1) {
            remove(id);
            return;
        }
        onDownloadComplete(fileData);
        if (fileData.isInvokeInstall()) {
            File destApk = getDestFile(id);
            if (destApk == null) {
                return;
            }
            DownloadCompleteReceiver.invokeInstall(getApplicationContext(), destApk);
        }
        remove(id);
    }

    private int checkStatus(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = mDownloadManager.query(query);
        try {
            if (c != null && c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                Log.i(TAG, "Status: " + status);
                switch (status) {
                    case DownloadManager.STATUS_PENDING:
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        break;
                    case DownloadManager.STATUS_FAILED:
                        remove(downloadId);
                        break;
                }
                return status;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return -1;
    }

    /**
     * Called when downloading file which is not allowed duplicate
     */
    protected abstract void onDownloadDuplicate(FileData fileData);

    /**
     * Called when a new download task starting
     */
    protected abstract void onDownloadStart(FileData fileData);

    /**
     * Called when invoking installing apk whose path is missing
     */
    protected abstract void onInstallingFileNotExist(FileData fileData);

    /**
     * Called when invoking installing apk which is not in the array list
     */
    protected abstract void onFileNotInQueue(long downloadId);


    /**
     * Called when receiving system broadcast of downloading complete
     *
     * @param fileData
     */
    protected abstract void onDownloadComplete(FileData fileData);

    /**
     * Called when receiving system broadcast of clicking on notification by user
     *
     * @param fileData
     */
    protected abstract void onClickNotification(FileData fileData);
}
