package com.djw.rtptest;

import android.util.Log;

public class LogUtil {

  private LogUtil() {

  }

  public static void e(String msg) {
    Log.e(getFileLineMethod(), msg);
  }

  private static String getFileLineMethod() {
    StackTraceElement localStackTraceElement = new Exception().getStackTrace()[2];
    return "("
        + localStackTraceElement.getFileName()
        + ":"
        + localStackTraceElement.getLineNumber()
        + ")[Func:"
        + localStackTraceElement.getMethodName()
        + "]";
  }
}