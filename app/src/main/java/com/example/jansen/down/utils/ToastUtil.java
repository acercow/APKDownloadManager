package com.example.jansen.down.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by zhaosen on 18-5-16.
 */

public class ToastUtil {

    public static Toast makeToast(Context context, String content, int duration) {
        return Toast.makeText(context, content, duration);
    }
}
