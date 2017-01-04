package com.djw.rtptest.audio;

import com.djw.rtptest.audio.sender.AudioRecorder;


public class AudioWrapper {

	private AudioRecorder audioRecorder;

	private static AudioWrapper instanceAudioWrapper;

	private AudioWrapper() {
	}

	public static AudioWrapper getInstance() {
		if (null == instanceAudioWrapper) {
			instanceAudioWrapper = new AudioWrapper();
		}
		return instanceAudioWrapper;
	}

	public void startRecord() {
		if (null == audioRecorder) {
			audioRecorder = new AudioRecorder();
		}
		audioRecorder.startRecording();
	}

	public void stopRecord() {
		if (audioRecorder != null)
			audioRecorder.stopRecording();
	}

	public void stopListen() {
		if (audioRecorder != null)
			audioRecorder.stopRecording();
	}
}
