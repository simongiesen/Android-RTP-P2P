package com.djw.rtptest.audio.sender;

import android.os.Message;
import com.djw.rtptest.Global;
import com.djw.rtptest.audio.AudioData;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AudioSender implements Runnable {
  String LOG = "AudioSender ";

  private boolean isSendering = false;
  private List<AudioData> dataList;

  private int nSendPacks = 0;
  private int nSendBytes = 0;

  public AudioSender() {
    dataList = Collections.synchronizedList(new LinkedList<AudioData>());
  }

  public void addData(byte[] data, int size) {
    AudioData encodedData = new AudioData();
    encodedData.setSize(size);
    byte[] tempData = new byte[size];
    System.arraycopy(data, 0, tempData, 0, size);
    encodedData.setRealData(tempData);
    dataList.add(encodedData);
  }

  private void sendData(byte[] data, int size) {
    if (!Global.ok) {
      return;
    }
    byte[] buf = new byte[size];
    System.arraycopy(data, 0, buf, 0, size);
    try {
      Global.mainActivity.rtpApp.rtpSession.sendData(buf);
    } catch (NullPointerException e) {
      return;
    }
    nSendPacks++;
    nSendBytes += size;
    Message message = new Message();
    message.what = 1;
    message.arg1 = nSendPacks;
    message.arg2 = nSendBytes;
    Global.mainActivity.handler.sendMessage(message);
  }

  /*
   * start sending data
   */
  public void startSending() {
    new Thread(this).start();
  }

  /*
   * stop sending data
   */
  public void stopSending() {
    this.isSendering = false;
  }

  // run
  public void run() {
    this.isSendering = true;
    System.out.println(LOG + "start....");
    while (isSendering) {
      if (dataList.size() > 0) {
        AudioData encodedData = dataList.remove(0);
        sendData(encodedData.getRealData(), encodedData.getSize());
      }
    }
    System.out.println(LOG + "stop!!!!");
  }
}