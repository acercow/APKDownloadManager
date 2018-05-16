package com.example.jansen.down.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Created by acercow on 18-5-16.
 */

public class LogUtil {
    private static final CharSequence TAG_PREFIX = "TAG_PREFIX";

    public static void d(String str) {
        StackTraceElement stackTraceElement = getCallerStackTraceElement();
        Log.d(generateTag(stackTraceElement), str);
    }

    public static void v(String str) {
        StackTraceElement stackTraceElement = getCallerStackTraceElement();
        Log.v(generateTag(stackTraceElement), str);
    }

    public static void i(String str) {
        StackTraceElement stackTraceElement = getCallerStackTraceElement();
        Log.i(generateTag(stackTraceElement), str);
    }

    public static void w(String str) {
        StackTraceElement stackTraceElement = getCallerStackTraceElement();
        Log.w(generateTag(stackTraceElement), str);
    }


    public static void e(String str) {
        StackTraceElement stackTraceElement = getCallerStackTraceElement();
        Log.e(generateTag(stackTraceElement), str);
    }


    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "%s.%s(Line:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName
                .lastIndexOf(".") + 1);
        tag = String.format(Locale.CHINA, tag, callerClazzName, caller.getMethodName(),
                caller.getLineNumber());
        tag = TextUtils.isEmpty(TAG_PREFIX) ? tag : TAG_PREFIX + "|"
                + tag;
        return tag;
    }
}
