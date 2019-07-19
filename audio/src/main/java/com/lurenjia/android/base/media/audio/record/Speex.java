package com.lurenjia.android.base.media.audio.record;

public class Speex {

	static {
		System.loadLibrary("zmSpeex");
	}

	public Speex() {
	}

	public native int speexInit(int frame_size, int sample_rate);

	public native int speexProcess(byte[] inbuffer, int channels, int length);

	public native int speexDestroy();

	/**
	 * 重采样，并且将双声道改为单声道
	 *
	 * @param inFilePath 源pcm文件
	 * @param outFilePath 输出pcm文件
	 * @param channels 声道数，如果是2，则输出文件会为单声道
	 * @param inRate 输入hz
	 * @param outRate 输出hz
	 */
	public native int resampleFile(String inFilePath, String outFilePath, int channels, int inRate, int outRate);

	/**
	 * 目前仅支持单声道，如果不是单声道，请自行处理成单声道
	 *
	 * @param inRate 输入频率，hz，如44100
	 * @param outRate 输出频率，hz，如16000
	 */
	public native int resampleInit(int inRate, int outRate);

	/**
	 * 处理每一帧数据.如果inRate比outRate大，则inArray的长度不能小于outArray的长度,反之亦然
	 *
	 * @param inChannels 输入声道数
	 * @param inArray 输入数据
	 * @param outArray 输出数据
	 * @param inByteArrayLength 输入字节数组数据长度
	 * @param outByteArrayLength 输出字节数组的长度
	 * @return 应该需要写入的字节大小，即outputStream.write(byte[],offset,count)中的count
	 */
	public native int resampleFrame(int inChannels, byte[] inArray, byte[] outArray, int inByteArrayLength, int
			outByteArrayLength);

	/**
	 * 销毁重采样器
	 */
	public native int resampleDestroy();
}