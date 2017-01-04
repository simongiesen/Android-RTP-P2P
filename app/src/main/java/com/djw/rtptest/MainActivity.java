package com.djw.rtptest;

import java.io.IOException;
import java.net.SocketException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.djw.rtptest.R;
import com.djw.rtptest.audio.AudioWrapper;
import com.djw.rtptest.audio.RtpApp;

public class MainActivity extends Activity {

	public TextView txtIP, txtPort, txtInfo, txtSend, txtRecv;
	private Button startEncodeButton, stopEncodeButton;

	public RtpApp rtpApp = null;
	String LOG = "MainActivity";
	private com.djw.rtptest.audio.AudioWrapper audioWrapper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Global.mainActivity = this;
		audioWrapper = AudioWrapper.getInstance();
		initControls();
	}

	private void initControls() {
		txtIP = (TextView) findViewById(R.id.txtIP);
		txtPort = (TextView) findViewById(R.id.txtPort);
		txtInfo = (TextView) findViewById(R.id.txtInfo);
		txtSend = (TextView) findViewById(R.id.txtSend);
		txtRecv = (TextView) findViewById(R.id.txtRecv);
		startEncodeButton = (Button) findViewById(R.id.startEncode);
		startEncodeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startEncodeButton.setEnabled(false);
				Global.ok = false;
				if (rtpApp != null) {
					try {
						rtpApp.releaseSocket();
					} catch (IOException e) {
						Toast.makeText(getApplicationContext(), "释放RTP资源时异常: " + e.getMessage(),
								Toast.LENGTH_SHORT).show();
						startEncodeButton.setEnabled(true);
						return;
					}
					rtpApp = null;
					Toast.makeText(getApplicationContext(), "成功释放RTP资源 ",
							Toast.LENGTH_SHORT).show();
				}
				try {
					int port = 6000;
					if (!txtPort.getText().toString().trim().equals(""))
						port = Integer.parseInt(txtPort.getText().toString()
								.trim());
					rtpApp = new RtpApp((MainActivity) v.getContext(), txtIP
							.getText().toString().trim(), port);
				} catch (SocketException e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), e.getMessage(),
							Toast.LENGTH_SHORT).show();
					startEncodeButton.setEnabled(true);
					return;
				}
				audioWrapper.startRecord();
				audioWrapper.startListen();
				stopEncodeButton.setEnabled(true);
				Global.ok = true;
			}
		});

		stopEncodeButton = (Button) findViewById(R.id.stopEncode);
		stopEncodeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopEncodeButton.setEnabled(false);
				Global.ok = false;
				// stopEncodeAudio();
				// stopPlayAudio();
				audioWrapper.stopRecord();
				audioWrapper.stopListen();
				if (rtpApp != null) {
					try {
						rtpApp.releaseSocket();
					} catch (IOException e) {
						Toast.makeText(getApplicationContext(), "释放RTP资源时异常: " + e.getMessage(),
								Toast.LENGTH_SHORT).show();
						startEncodeButton.setEnabled(true);
						return;
					}
					rtpApp = null;
					Toast.makeText(getApplicationContext(), "成功释放RTP资源 ",
							Toast.LENGTH_SHORT).show();
				}
				startEncodeButton.setEnabled(true);
			}
		});
		stopEncodeButton.setEnabled(false);
	}

	@Override
	protected void onDestroy() {
		// try {
		// if (amrEncoder != null) {
		// amrEncoder.stop();
		// }
		// if (audioPlayer != null) {
		// audioPlayer.stop();
		// }
		// } catch (Exception e) {
		// }
		super.onDestroy();
	}

	public final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				txtSend.setText("已发送" + msg.arg1 + "帧数据，共" + msg.arg2 + "字节");
				break;
			case 2:
				txtRecv.setText("已收到" + msg.arg1 + "帧数据，共" + msg.arg2 + "字节");
				break;
			case 3:
				if (msg.arg1 == 1) 
					txtInfo.setText("接收数据是在主线程中处理");
				else
					txtInfo.setText("接收数据不是在主线程中处理");
				break;
			}
			super.handleMessage(msg);
		}
	};

}