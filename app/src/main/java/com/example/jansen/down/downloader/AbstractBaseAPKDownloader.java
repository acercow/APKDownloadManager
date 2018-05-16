package com.example.jansen.down.downloader;

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

import com.example.jansen.down.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Created by zhaosen 2018-05-12
 * <p>
 * A service of apk downloading, which maintains a downloading queue to control duplicate
 * downloading(if disallow duplicate), status changing, auto installing, path management,
 * lifecycle management, dealing with unconventional operation(exp. deleting file while downloading).
 * <p>
 * If Android would restrict implicit service invocation in future, you should implicit invoke an empty
 * activity, which contains only an explicit intent to start this download service.
 */
public abstract class AbstractBaseAPKDownloader extends Service implements APKDownloaderReceiver.IReceiveCallback {
    private static final String TAG = AbstractBaseAPKDownloader.class.getSimpleName();

    /*--------------------Query parameters of Scheme --------------------------*/
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String FILE_NAME = "fileName";
    public static final String AUTO_INSTALL = "autoInstall";
    public static final String DOWNLOAD_OVER_MOBILE = "downloadOverMobile";
    public static final String ALLOW_DUPLICATE = "allowDuplicate";
    public static final String URL = "url";
    public static final String SCHEME_APK_DOWNLOAD_MANAGER = "arczues://startdownload";

    private List<RepositoryData> mFileLists;
    private DownloadManager mDownloadManager;
    private APKDownloaderReceiver mCompleteReceiver;
    private boolean isRegistered;
    private File mDestDir;

    @Override
    public void onCreate() {
        LogUtil.v("===== APKDownload Service -> onCreate =====");
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
        LogUtil.v("===== APKDownload Service -> onStartCommand =====");
        RepositoryData repositoryData = parseIntent(intent);
        if (repositoryData != null) {
            download(repositoryData);
        } else {
            onDeliveredSchemeError(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogUtil.v("===== APKDownload Service -> onDestroy =====");
        if (isRegistered) {
            unRegisterReceiver();
        }
        mFileLists.clear();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.v("===== APKDownload Service -> onBind =====");
        return null;
    }

    /**
     * Main method to invoke file download
     *
     * @param repositoryData
     */
    private void download(RepositoryData repositoryData) {
        makeDownloadDir(); // check each time when download start to ensure dir's validation
        if (passDuplicateCheck(repositoryData)) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(repositoryData.getUrl()));

            request.setAllowedNetworkTypes((repositoryData.isDownloadOverMobile() ? DownloadManager.Request.NETWORK_MOBILE : 0) | DownloadManager.Request.NETWORK_WIFI);
            request.setAllowedOverRoaming(true);

            // uri解析过来可能为"null"字符串
            if (!TextUtils.isEmpty(repositoryData.getNotificationTitle()) && !"null".equals(repositoryData.getNotificationTitle())) {
                request.setTitle(repositoryData.getNotificationTitle());
            }
            if (!TextUtils.isEmpty(repositoryData.getNotificationDescription()) && !"null".equals(repositoryData.getNotificationDescription())) {
                request.setDescription(repositoryData.getNotificationDescription());
            }
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            String subPath = TextUtils.isEmpty(subDir()) ? repositoryData.getLocalFileName() : subDir() + File.separator + repositoryData.getLocalFileName();
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,  subPath);

            long downloadId = mDownloadManager.enqueue(request);
            repositoryData.setDownloadId(downloadId);
            add(repositoryData);
            LogUtil.i("repositoryData: " + repositoryData);
            onDownloadStart(repositoryData);
        } else {
            onDownloadDuplicate(repositoryData);
        }
    }

    private RepositoryData parseIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            LogUtil.e("Could not get uri from intent!");
            return null;
        }
        LogUtil.i(uri.toString());
        String repositoryUrl = uri.getQueryParameter(URL);
        if (TextUtils.isEmpty(repositoryUrl)) {
            LogUtil.e("Download url should not be empty!");
            return null;
        }

        if (!repositoryUrl.startsWith("http")) { // If not checked this, an exception would be thrown by system
            LogUtil.e("Download url should not be empty!");
            return null;
        }

        if (!repositoryUrl.startsWith("https")) { // For mobile security
            LogUtil.w("Download url should use https instead of http protocol!");
        }

        String notificationTitle = uri.getQueryParameter(TITLE);
        String notificationDescription = uri.getQueryParameter(DESCRIPTION);
        String localFileName = uri.getQueryParameter(FILE_NAME);
        boolean autoInstall = uri.getBooleanQueryParameter(AUTO_INSTALL, true);
        boolean downloadOverMobile = uri.getBooleanQueryParameter(DOWNLOAD_OVER_MOBILE, true);
        boolean allowDuplicated = uri.getBooleanQueryParameter(ALLOW_DUPLICATE, false);

        RepositoryData repositoryData = new RepositoryData(repositoryUrl, autoInstall, allowDuplicated, downloadOverMobile, notificationTitle, notificationDescription, localFileName);

        return repositoryData;
    }


    /**
     * Support sub-dir
     */
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
                LogUtil.i("mkdir: " + mDestDir + " [result: " + isCreated + "]");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private File getDestFile(long id) {
        RepositoryData repositoryData = get(id);
        if (repositoryData == null) {
            return null;
        }
        File destApk = new File(mDestDir, repositoryData.getLocalFileName());
        if (!destApk.exists()) {
            onInstallingFileNotExist(repositoryData);
            return null;
        }
        LogUtil.i("Dest apk: " + destApk);
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
     * @param repositoryData
     * @return <tt>true</tt> if is allowed to download
     */
    private boolean passDuplicateCheck(RepositoryData repositoryData) {
        // If is duplicate and disallow duplicate
        boolean isDuplicate = mFileLists.contains(repositoryData) && !repositoryData.isAllowDuplicated();
//        if (isDuplicate) {
//            RepositoryData oldFileData = mFileLists.get(mFileLists.indexOf(repositoryData));
//            int status = checkStatus(oldFileData.getDownloadId());
//            return  (status == -1 || status == DownloadManager.STATUS_FAILED || status == DownloadManager.STATUS_SUCCESSFUL);
//        } else {
//            return true;
//        }
        if (isDuplicate) {
            RepositoryData oldRepositoryData = mFileLists.get(mFileLists.indexOf(repositoryData));
            File file = new File(mDestDir, oldRepositoryData.getLocalFileName());
            // 在多数"下载"应用中删除文件，有系统广播，但是在文件管理器中或如MIUI系统的"下载"中删除，无广播
            // 所以此时通过路径判断
            if (file.exists()) {
                return false;
            } else {
                // 下载同一文件发现被删除，先取消下载器中任务，注意会触发COMPLETE广播，status为cancel
                // 收到广播后会删除队列，检查是否需要停止服务和广播等
                mDownloadManager.remove(oldRepositoryData.getDownloadId());
                return true;
            }
        } else {
            return true;
        }

    }

    private void add(RepositoryData repositoryData) {
        mFileLists.add(repositoryData);
    }

    private RepositoryData get(long downloadId) {
        for (RepositoryData repositoryData : mFileLists) {
            if (repositoryData.getDownloadId() == downloadId) {
                return repositoryData;
            }
        }
        onFileNotInQueue(downloadId);
        return null;
    }

    private void remove(long downloadId) {
        Iterator<RepositoryData> iterator = mFileLists.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getDownloadId() == downloadId) {
                iterator.remove();
                LogUtil.i("removed item: " + downloadId);

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
        LogUtil.v("----- BroadcastReceiver -> registered -----");
        isRegistered = true;
        if (mCompleteReceiver == null) {
            mCompleteReceiver = new APKDownloaderReceiver();
        }
        mCompleteReceiver.setOnReceiveListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getApplicationContext().registerReceiver(mCompleteReceiver, intentFilter);
    }

    private void unRegisterReceiver() {
        LogUtil.v("----- BroadcastReceiver -> unRegistered -----");
        isRegistered = false;
        if (mCompleteReceiver != null) {
            getApplicationContext().unregisterReceiver(mCompleteReceiver);
        }
    }

    @Override
    public void onClickNotification(long id) {
        LogUtil.i("BroadcastReceiver -> onClickNotification: " + id);
        RepositoryData repositoryData = get(id);
        if (repositoryData == null) {
            return;
        }
        onClickNotification(repositoryData);
    }

    /**
     * 注意COMPLETE广播不仅仅是下载完成收到，下载失败、取消都是这个广播
     *
     * @param id
     */
    @Override
    public void onComplete(long id) {
        LogUtil.i("BroadcastReceiver -> onComplete: " + id);
        RepositoryData repositoryData = get(id);
        if (repositoryData == null) {
            return;
        }
        // User canceling download progress also receives COMPLETE broadcast
        checkStatus(repositoryData);
    }

    /**
     * To check status as soon as receiving COMPLETE broadcast
     *
     * @param repositoryData
     * @return Status code, See in {@link DownloadManager#STATUS_FAILED} etc.
     */
    public int checkStatus(RepositoryData repositoryData) {
        long downloadId = repositoryData.getDownloadId();
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = mDownloadManager.query(query);
        try {
            if (c != null && c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                LogUtil.i("Status: " + status);
                switch (status) {
                    case DownloadManager.STATUS_PENDING:
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        if (repositoryData.isAutoInstall()) {
                            File destApk = getDestFile(downloadId);
                            if (destApk == null) {
                                return -1;
                            }
                            APKDownloaderReceiver.invokeInstall(getApplicationContext(), destApk);
                        }
                        onDownloadCompleted(repositoryData);
                        remove(downloadId);
                        break;
                    case DownloadManager.STATUS_FAILED:
                        onDownloadFailed(repositoryData);
                        remove(downloadId);
                        break;
                }
                return status;
            } else { // Self difined status type: download canceled
                int selfDefinedStatus = 1 << 7;
                LogUtil.i("Status: " + selfDefinedStatus + " (means canceled, not defined in api)");
                onDownloadCanceled(repositoryData);
                remove(downloadId); // Cancel downloading status
                return selfDefinedStatus;
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

    public List<RepositoryData> getCurrentDownloads() {
        return mFileLists;
    }

    /**
     * Start downloading
     * <p>
     * Do not use directly in other modules（exp. extcard) in case interdependence, implicit intent invoking
     * makes life safer :)
     *
     * @param context
     * @param url      download url,
     * @param fileName
     */
    public static void startDownload(Context context, String url, String fileName) {
        startDownload(context, url, true, false, true, null, null, fileName);
    }


    public static void startDownload(Context context, String url, boolean autoInstall, boolean allowDuplicated, boolean downloadOverMobile, String title, String description, String fileName) {
        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(SCHEME_APK_DOWNLOAD_MANAGER)
                .buildUpon()
                .appendQueryParameter(URL, url)
                .appendQueryParameter(AUTO_INSTALL, String.valueOf(autoInstall))
                .appendQueryParameter(ALLOW_DUPLICATE, String.valueOf(allowDuplicated))
                .appendQueryParameter(DOWNLOAD_OVER_MOBILE, String.valueOf(downloadOverMobile))
                .appendQueryParameter(TITLE, title)
                .appendQueryParameter(DESCRIPTION, description)
                .appendQueryParameter(FILE_NAME, fileName)
                .build();

        LogUtil.i(uri.toString());
        intent.setData(uri);
        intent.setPackage(context.getPackageName()); // to support implicit intent on service
        context.startService(intent);
    }


    public void cancelDownloads(RepositoryData... repositoryDatas) {
        if (repositoryDatas == null || repositoryDatas.length < 1) {
            return;
        }
        for (RepositoryData repositoryData : repositoryDatas) {
            mDownloadManager.remove(repositoryData.getDownloadId());
        }
        LogUtil.i("Cancel downloading: " + Arrays.toString(repositoryDatas));
    }

    public void showDownloadsView() {
        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    /**
     * Called when downloading file which is not allowed duplicate
     */
    protected abstract void onDownloadDuplicate(RepositoryData repositoryData);

    /**
     * Called when a new download task starting
     */
    protected abstract void onDownloadStart(RepositoryData repositoryData);

    /**
     * Called when invoking installing apk whose path is missing
     */
    protected abstract void onInstallingFileNotExist(RepositoryData repositoryData);

    /**
     * Called when invoking installing apk which is not in the array list
     */
    protected abstract void onFileNotInQueue(long downloadId);

    /**
     * Called when receiving system broadcast of downloading canceled
     *
     * @param repositoryData
     */
    protected abstract void onDownloadCanceled(RepositoryData repositoryData);

    /**
     * Called when receiving system broadcast of downloading completed
     *
     * @param repositoryData
     */
    protected abstract void onDownloadCompleted(RepositoryData repositoryData);

    /**
     * Called when receiving system broadcast of downloading failed
     *
     * @param repositoryData
     */
    protected abstract void onDownloadFailed(RepositoryData repositoryData);

    /**
     * Called when receiving system broadcast of clicking on notification by user
     *
     * @param repositoryData
     */
    protected abstract void onClickNotification(RepositoryData repositoryData);

    /**
     * Called when the conveyed scheme contains incorrect information, see{@link AbstractBaseAPKDownloader#parseIntent(Intent)}
     *
     * @param intent
     */
    protected abstract void onDeliveredSchemeError(Intent intent);
}
