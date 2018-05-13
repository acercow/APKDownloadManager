package com.example.jansen.downloader;


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.example.jansen.down.BuildConfig;

import java.io.File;

/**
 * Created by zhaosen, 2018-05-13
 *
 * A BroadcastReceiver to notify APKDownloader that download status changed, the broadcast comes from
 * system's DownloadManager progress.
 */
public class DownloadCompleteReceiver extends BroadcastReceiver {
    public static final String APK_FILEPROVIDER_AUTHORITIES = BuildConfig.APPLICATION_ID + ".fileprovider"; // see in res/AndroidManifest.xml
    private static final String TAG = DownloadCompleteReceiver.class.getSimpleName();

    private ICallback mListener;

    public void setCompleteListener(ICallback listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receive DownloadManager broadcast: " + intent.getAction());
        if (TextUtils.isEmpty(intent.getAction())) {
            return;
        }
        switch (intent.getAction()) {
            // Important!!! 不仅仅是下载成功，下载失败、取消下载也会收到此广播
            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                long completeId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (completeId == -1) {
                    return; // may not invoked by this downloader
                }
                if (mListener != null) {
                    mListener.onComplete(completeId);
                }
                break;

            case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
                if (mListener != null) {
                    for (long clickedId : ids) {
                        mListener.onClickNotification(clickedId);
                    }
                }
                break;
            default:
                break;
        }

    }

    public interface ICallback {
        void onClickNotification(long id);
        void onComplete(long id);
    }

    public static void invokeInstall(Context context, File apkPath) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.setDataAndType(generateUri(context, apkPath), "application/vnd.android.package-archive");
        context.startActivity(installIntent);
    }

    private static Uri generateUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, APK_FILEPROVIDER_AUTHORITIES, file);
        } else {
            uri = Uri.fromFile(file);
        }
        Log.i(TAG, "uri: " + uri);
        return uri;
    }
}
