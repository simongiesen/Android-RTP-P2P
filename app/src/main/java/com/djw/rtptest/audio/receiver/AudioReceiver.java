package com.djw.rtptest.audio.receiver;

import android.util.Log;
import com.djw.rtptest.Global;
import java.io.IOException;
import java.io.InputStream;

import static com.djw.rtptest.crash.CrashHandler.TAG;

public class AudioReceiver implements Runnable {

  String LOG = "AudioReceiver";
  boolean isRunning = false;

  private byte[] packetBuf = new byte[1024];
  private int packetSize = 1024;

  /*
   * 开始接收数据
   */
  public void startRecieving() {
    //new Thread(this).start();
  }

  /*
   * 停止接收数据
   */
  public void stopRecieving() {
    isRunning = false;
  }

  /*
   * 释放资源
   */
  private void release() {
  }

  public void run() {

    Log.e(TAG, "开始收取码流");

    // 在接收前，要先启动解码器
    AudioDecoder decoder = AudioDecoder.getInstance();
    decoder.startDecoding();

    isRunning = true;
    try {
      InputStream inputStream = Global.mainActivity.rtpApp.receiver.getInputStream();
      byte[] buffer = new byte[100 * 1024];
      while (isRunning) {
        int numOfRead = inputStream.read(buffer);
        if (numOfRead <= 0) {
          break;
        }
        decoder.addData(buffer, numOfRead);
        Log.e(LOG, "收到一个包..." + numOfRead);
      }
    } catch (IOException e) {
      Log.e(LOG, "RECIEVE ERROR!");
    }
    // 接收完成，停止解码器，释放资源
    decoder.stopDecoding();
    release();
    Log.e(LOG, "stop recieving");
  }
}
