package com.djw.rtptest.audio;

public class AudioCodec {

  static {
    System.loadLibrary("audiowrapper");
  }

  // initialize decoder and encoder
  public static native int audio_codec_init(int mode);

  // encode
  public static native int audio_encode(byte[] sample, int sampleOffset, int sampleLength,
      byte[] data, int dataOffset);

  // decode
  public static native int audio_decode(byte[] data, int dataOffset, int dataLength, byte[] sample,
      int sampleLength);
}
