package com.djw.rtptest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.djw.rtptest.audio.AudioWrapper;
import com.djw.rtptest.audio.RtpApp;
import java.io.IOException;
import java.net.SocketException;

public class MainActivity extends Activity {

  public TextView txtIP, txtInfo, txtSend, txtRecv;
  private Button startEncodeButton, stopEncodeButton;

  public RtpApp rtpApp = null;

  private com.djw.rtptest.audio.AudioWrapper audioWrapper;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Global.mainActivity = this;
    audioWrapper = AudioWrapper.getInstance();
    initControls();
  }

  private void initControls() {

    txtIP = (TextView) findViewById(R.id.txtIP);
    txtInfo = (TextView) findViewById(R.id.txtInfo);
    txtSend = (TextView) findViewById(R.id.txtSend);
    txtRecv = (TextView) findViewById(R.id.txtRecv);

    startEncodeButton = (Button) findViewById(R.id.startEncode);
    startEncodeButton.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        startEncodeButton.setEnabled(false);
        Global.ok = false;
        if (rtpApp != null) {
          destroyRTP();
        }
        enableRTP();
        Global.ok = true;
      }
    });

    stopEncodeButton = (Button) findViewById(R.id.stopEncode);
    stopEncodeButton.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        destroyRTP();
      }
    });
    stopEncodeButton.setEnabled(false);
  }

  void enableRTP() {
    rtpApp = new RtpApp(this, txtIP.getText().toString().trim());
    audioWrapper.startRecord();
    stopEncodeButton.setEnabled(true);
  }

  void destroyRTP() {
    stopEncodeButton.setEnabled(false);
    Global.ok = false;
    audioWrapper.stopRecord();
    audioWrapper.stopListen();
    if (rtpApp != null) {
      try {
        rtpApp.releaseSocket();
      } catch (IOException e) {
        showMsg("释放RTP资源时异常: " + e.getMessage());
        return;
      }
      rtpApp = null;
      showMsg("成功释放RTP资源");
    }
    startEncodeButton.setEnabled(true);
  }

  void showMsg(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  }

  public final Handler handler = new Handler() {
    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case 1:
          txtSend.setText("已发送" + msg.arg1 + "帧数据，共" + msg.arg2 + "字节");
          break;
        case 2:
          txtRecv.setText("已收到" + msg.arg1 + "帧数据，共" + msg.arg2 + "字节");
          break;
        case 3:
          if (msg.arg1 == 1) {
            txtInfo.setText("接收数据是在主线程中处理");
          } else {
            txtInfo.setText("接收数据不是在主线程中处理");
          }
          break;
      }
      super.handleMessage(msg);
    }
  };
}