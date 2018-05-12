package com.example.jansen.downloader;


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;

import tv.xiaoka.base.util.YZBLogUtil;
import tv.xiaoka.play.manager.YZBDownloadManager;

public class DownloadCompleteReceiver extends BroadcastReceiver {
    public static final String WEIBO_FILEPROVIDER_AUTHORITIES = "com.sina.weibo.fileprovider"; // see in res/AndroidManifest.xml
    private ICallback mListener;

    public void setCompleteListener(ICallback listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        YZBLogUtil.i("Receive DownloadManager broadcast: " + intent.getAction());
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        switch (intent.getAction()) {
            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                if (mListener != null) {
                    mListener.onComplete(id);
                }
                break;

            case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                if (mListener != null) {
                    mListener.onClickNotification(id);
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

    public static void invokeInstall(Context context) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), YZBDownloadManager.FILE_YIZHIBO_APK_NAME);
        if (path.exists()) {
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, WEIBO_FILEPROVIDER_AUTHORITIES, path);
            } else {
                uri = Uri.fromFile(path);
            }
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(installIntent);
        }
    }
}
