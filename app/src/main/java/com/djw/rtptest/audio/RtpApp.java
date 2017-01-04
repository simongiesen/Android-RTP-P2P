package com.djw.rtptest.audio;

import com.djw.rtptest.LogUtil;
import com.djw.rtptest.audio.receiver.AudioDecoder;
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
import java.util.concurrent.RunnableFuture;
import jlibrtp.*;

import static com.djw.rtptest.Global.T;
import static com.djw.rtptest.crash.CrashHandler.TAG;

public class RtpApp {

  public RTPSession rtpSession = null;

  private AudioDecoder decoder;

  private int nRecvPacks = 0;
  private int nRecvBytes = 0;
  private MainActivity activity;

  private String ip;

  public RtpApp(MainActivity activity, String ip) {

    this.activity = activity;
    decoder = AudioDecoder.getInstance();
    decoder.startDecoding();
    this.ip = ip;
    new Thread(r).start();
  }

  private Runnable r = new Runnable() {
    @Override public void run() {
      try {
        int port = 6000;
        DatagramSocket rtpSocket = new DatagramSocket(port);
        DatagramSocket rtcpSocket = new DatagramSocket(port + 1);
        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession.RTPSessionRegister(rtpCallback, callback, null);
        Enumeration<Participant> e = rtpSession.getParticipants();
        while (e.hasMoreElements()) {
          rtpSession.removeParticipant(e.nextElement());
        }
        Participant p = new Participant(ip, port, port + 1);
        rtpSession.addParticipant(p);
      } catch (SocketException e) {
        e.printStackTrace();
      }
    }
  };

  public void releaseSocket() throws IOException {
    decoder.stopDecoding();
    rtpSession.endSession();
    rtpSession = null;
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

  private RTPAppIntf rtpCallback = new RTPAppIntf() {
    @Override public void receiveData(DataFrame frame, Participant participant) {
      Log.e("receiveData", frame.rtpTimestamp() + "-------------");
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
      decoder.addData(data, data.length);
    }

    @Override public void userEvent(int type, Participant[] participant) {
      LogUtil.e("userEvent(int type, Participant[] participant)");
    }

    @Override public int frameSize(int payloadType) {
      return 1;
    }
  };
}