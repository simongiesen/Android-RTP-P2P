package com.djw.rtptest.audio;

import com.djw.rtptest.LogUtil;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.djw.rtptest.Global;
import com.djw.rtptest.MainActivity;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.util.Enumeration;
import jlibrtp.*;

import static com.djw.rtptest.Global.T;
import static com.djw.rtptest.crash.CrashHandler.TAG;

public class RtpApp implements RTPAppIntf {

  public RTPSession rtpSession = null;
  public RTPSession rtpSession2 = null;

  private int nPacks = 0;

  private LocalServerSocket lss;
  public LocalSocket sender, receiver;

  private TextView txtInfo;

  private int nRecvPacks = 0;
  private int nRecvBytes = 0;
  private MainActivity activity;

  public RtpApp(MainActivity activity, final String ip, final int port) throws SocketException {
    this.activity = activity;

    //initLocalSocket();

    DatagramSocket rtpSocket = new DatagramSocket(port);
    DatagramSocket rtcpSocket = new DatagramSocket(port + 1);

    final DatagramSocket finalRtpSocket = rtpSocket;
    final DatagramSocket finalRtcpSocket = rtcpSocket;

    new Thread(new Runnable() {
      @Override public void run() {

        rtpSession = new RTPSession(finalRtpSocket, finalRtcpSocket);
        rtpSession.RTPSessionRegister(RtpApp.this, callback, null);

        Enumeration<Participant> e = rtpSession.getParticipants();
        while (e.hasMoreElements()) {
          LogUtil.e("先删除之前的所有参与者...");
          rtpSession.removeParticipant(e.nextElement());
        }

        Participant p;
        p = new Participant(ip, port, port + 1);
        rtpSession.addParticipant(p);
      }
    }).start();
  }

  private RTCPAppIntf callback = new RTCPAppIntf() {

    @Override
    public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, long rtpTimestamp,
        long packetCount, long octetCount, long[] reporteeSsrc, int[] lossFraction,
        int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter,
        long[] lastSRTimeStamp, long[] delayLastSR) {
      LogUtil.e("-=---------------");
    }

    @Override public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, int[] lossFraction,
        int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter,
        long[] lastSRTimeStamp, long[] delayLastSR) {
      LogUtil.e("-=---------------");
    }

    @Override public void SDESPktReceived(Participant[] relevantParticipants) {
      LogUtil.e("-=---------------");
    }

    @Override public void BYEPktReceived(Participant[] relevantParticipants, String reason) {
      LogUtil.e("-=---------------");
    }

    @Override public void APPPktReceived(Participant part, int subtype, byte[] name, byte[] data) {
      LogUtil.e("-=---------------");
    }
  };

  public void releaseSocket() throws IOException {
    rtpSession.endSession();
    //releaseLocalSocket();
  }

  public void receiveData(DataFrame frame, Participant p) {

    Log.e("receiveData", frame.rtpTimestamp() + "-------------");

    if (!Global.ok) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return;
    }
    nPacks++;
    byte[] data = frame.getConcatenatedData();
    nRecvPacks++;
    nRecvBytes += data.length;
    Message message = new Message();
    message.what = 2;
    message.arg1 = nRecvPacks;
    message.arg2 = nRecvBytes;
    activity.handler.sendMessage(message);

    message = new Message();
    message.what = 3;
    if (Looper.myLooper() == Looper.getMainLooper()) {
      message.arg1 = 1;
    } else {
      message.arg1 = 0;
    }
    activity.handler.sendMessage(message);

    try {


      sender.getOutputStream().write(data);


    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void userEvent(int type, Participant[] participant) {
    // Do nothing
  }

  public int frameSize(int payloadType) {
    return 1;
  }

  private void releaseLocalSocket() throws IOException {
    if (sender != null) {
      sender.close();
    }
    if (receiver != null) {
      receiver.close();
    }
    if (lss != null) {
      lss.close();
    }
    sender = null;
    receiver = null;
    lss = null;
  }

  private boolean initLocalSocket() {
    boolean ret = true;
    try {
      releaseLocalSocket();

      String serverName = "rtpApp";
      final int bufSize = 1024;

      lss = new LocalServerSocket(serverName);

      receiver = new LocalSocket();
      receiver.connect(new LocalSocketAddress(serverName));
      receiver.setReceiveBufferSize(bufSize);
      receiver.setSendBufferSize(bufSize);

      sender = lss.accept();
      sender.setReceiveBufferSize(bufSize);
      sender.setSendBufferSize(bufSize);
    } catch (IOException e) {

      ret = false;
    }
    return ret;
  }
}