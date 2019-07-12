package com.qicode.kakaxicm.audio.handler;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.qicode.kakaxicm.audio.format.FormatInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/***
 * pcm编码成aac文件
 */
public class RealPcmToAacHandler {

    private static final String TAG = "PcmToAacHandler";

    private File aacFile;//输出
    private FormatInfo info;//aac
    private OutputStream aacOutput = null;
    private MediaCodec audioEncoder;
    private ByteBuffer[] audioInputBuffers;
    private ByteBuffer[] audioOutputBuffers;

    public RealPcmToAacHandler(File aacFile, FormatInfo info) {
        this.aacFile = aacFile;
        this.info = info;
        aacOutput = null;
    }

    public RealPcmToAacHandler(OutputStream outputStream, FormatInfo info) {
        this.aacOutput = outputStream;
        this.info = info;
        aacFile = null;
    }

    /**
     * 开启编码器
     *
     * @return
     */
    public boolean start() {
        try {
            if (aacFile != null) {
                aacOutput = new FileOutputStream(aacFile);
            }
            audioEncoder = createACCAudioDecoder();
            audioEncoder.start();
            audioInputBuffers = audioEncoder.getInputBuffers();
            audioOutputBuffers = audioEncoder.getOutputBuffers();
        } catch (Exception e) {
            Log.e("RealPcmToAacHandler", e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * 编码
     *
     * @param data
     * @return
     */
    public boolean encode(byte[] data) {
        try {
            encodeToAac(data);
            return true;
        } catch (Throwable t) {
            Log.e("RealPcmToAacHandler", t.getLocalizedMessage());
            return false;
        }
    }

    private void encodeToAac(byte[] data) throws IOException {
        MediaCodec.BufferInfo outBufferInfo = new MediaCodec.BufferInfo();
        int readDataCount = data == null ? 0 : data.length;
        int inputBufIndex, outputBufIndex;
        //取缓冲队列
        inputBufIndex = audioEncoder.dequeueInputBuffer(10000);
        //TODO 编码
        if (inputBufIndex >= 0) {
            ByteBuffer inputBuffer = audioInputBuffers[inputBufIndex];
            inputBuffer.clear();//清除旧数据
            if (readDataCount <= 0) {
                audioEncoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                //填充输入缓冲区数据
                inputBuffer.put(data, 0, readDataCount);
                audioEncoder.queueInputBuffer(inputBufIndex, 0, readDataCount, 0, 0);
            }
        }
        //读输出缓冲区，写aac文件
        while (true) {
            outputBufIndex = audioEncoder.dequeueOutputBuffer(outBufferInfo, 0);
            if (outputBufIndex > 0) {
                // Simply ignore codec config buffers.
                if ((outBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    audioEncoder.releaseOutputBuffer(outputBufIndex, false);
                } else if (outBufferInfo.size != 0) {
                    ByteBuffer outBuffer = audioOutputBuffers[outputBufIndex];
                    outBuffer.position(outBufferInfo.offset);
                    outBuffer.limit(outBufferInfo.offset + outBufferInfo.size);

                    int outBufSize = outBufferInfo.size;
                    int outPacketSize = outBufSize + 7;
                    //TODO 多余?
                    outBuffer.position(outBufferInfo.offset);
                    outBuffer.limit(outBufferInfo.offset + outBufSize);

                    byte[] outData = new byte[outBufSize + 7];
                    addADTSToPacket(outData, outPacketSize);
                    //读输出数据
                    outBuffer.get(outData, 7, outBufSize);
                    //写aac
                    aacOutput.write(outData, 0, outData.length);
                }
                audioEncoder.releaseOutputBuffer(outputBufIndex, false);

            } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                audioOutputBuffers = audioEncoder.getOutputBuffers();
                break;
            } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat audioFormat = audioEncoder.getOutputFormat();
                Log.i(TAG, "format change : " + audioFormat);
                break;
            } else {
                break;
            }
        }
    }

    private MediaCodec createACCAudioDecoder() throws IOException {
        MediaCodec codec = MediaCodec.createDecoderByType(info.mime);
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, info.mime);
        format.setInteger(MediaFormat.KEY_BIT_RATE, info.bitRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, info.channelCount);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, info.sampleRate);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, info.maxInputSize);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return codec;
    }

    /**
     * Add ADTS header at the beginning of each and every AAC packet.
     * This is needed as MediaCodec encoder generates a packet of raw
     * AAC data.
     * <p>
     * Note the packetLen must count in the ADTS header itself.
     **/
    private void addADTSToPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = info.freqIndex();  //44.1KHz
        int chanCfg = info.channelCount;  //CPE
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
