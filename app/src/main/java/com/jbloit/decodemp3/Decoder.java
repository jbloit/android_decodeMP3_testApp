package com.jbloit.decodemp3;

// FROM:
// https://android.googlesource.com/platform/cts/+/jb-mr2-release/tests/tests/media/src/android/media/cts/DecoderTest.java

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.CRC32;



public class Decoder {
    private static final String TAG = "DecoderTest";
    private Resources mResources;
    short[] mMasterBuffer;

    protected void setUp() throws Exception {

        mResources = MyApplication.getAppContext().getResources();
        // read master file into memory

        AssetFileDescriptor masterFd = mResources.openRawResourceFd(R.raw.sinusoid);
        long masterLength = masterFd.getLength();
        mMasterBuffer = new short[(int) (masterLength / 2)];
        InputStream is = masterFd.createInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        for (int i = 0; i < mMasterBuffer.length; i++) {
            int lo = bis.read();
            int hi = bis.read();
            if (hi >= 128) {
                hi -= 256;
            }
            int sample = hi * 256 + lo;
            mMasterBuffer[i] = (short) sample;
        }
        bis.close();
        masterFd.close();
    }

    public float [] testDecodeMp3Lame() throws Exception {

        short [] decoded = decode(R.raw.sinusoid, 804.f, true);

        float[] transformed = new float[decoded.length];

        for (int j=0;j<decoded.length;j++) {
            transformed[j] = (float)decoded[j] / Short.MAX_VALUE;
        }
        return transformed;
    }

    private short [] decode(int testinput, float maxerror, boolean reconfigure) throws IOException {
        short [] decoded = decodeToMemory(testinput, false);
        return decoded;

        

//        assertEquals("wrong data size", mMasterBuffer.length, decoded.length);
//        long totalErrorSquared = 0;
//        for (int i = 0; i < decoded.length; i++) {
//            short sample = decoded[i];
//            short mastersample = mMasterBuffer[i];
//            int d = sample - mastersample;
//            totalErrorSquared += d * d;
//        }
//        long avgErrorSquared = (totalErrorSquared / decoded.length);
//        double rmse = Math.sqrt(avgErrorSquared);
//        assertTrue("decoding error too big: " + rmse, rmse <= maxerror);
//        if (reconfigure) {
//            short [] decoded2 = decodeToMemory(testinput, true);
//            assertEquals("count different with reconfigure", decoded.length, decoded2.length);
//            for (int i = 0; i < decoded.length; i++) {
//                assertEquals("samples don't match", decoded[i], decoded2[i]);
//            }
//        }
    }
    private short[] decodeToMemory(int testinput, boolean reconfigure) throws IOException {
        short [] decoded = new short[0];
        int decodedIdx = 0;
        AssetFileDescriptor testFd = mResources.openRawResourceFd(testinput);
        MediaExtractor extractor;
        MediaCodec codec;
        ByteBuffer[] codecInputBuffers;
        ByteBuffer[] codecOutputBuffers;
        extractor = new MediaExtractor();
        extractor.setDataSource(testFd.getFileDescriptor(), testFd.getStartOffset(),
                testFd.getLength());
        testFd.close();
//        assertEquals("wrong number of tracks", 1, extractor.getTrackCount());
        MediaFormat format = extractor.getTrackFormat(0);
        String mime = format.getString(MediaFormat.KEY_MIME);
//        assertTrue("not an audio file", mime.startsWith("audio/"));
        codec = MediaCodec.createDecoderByType(mime);
        codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
        codec.start();
        codecInputBuffers = codec.getInputBuffers();
        codecOutputBuffers = codec.getOutputBuffers();
        if (reconfigure) {
            codec.stop();
            codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
            codec.start();
            codecInputBuffers = codec.getInputBuffers();
            codecOutputBuffers = codec.getOutputBuffers();
        }
        extractor.selectTrack(0);
        // start decoding
        final long kTimeOutUs = 5000;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;
        int noOutputCounter = 0;
        while (!sawOutputEOS && noOutputCounter < 50) {
            noOutputCounter++;
            if (!sawInputEOS) {
                int inputBufIndex = codec.dequeueInputBuffer(kTimeOutUs);
                if (inputBufIndex >= 0) {
                    ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                    int sampleSize =
                            extractor.readSampleData(dstBuf, 0 /* offset */);
                    long presentationTimeUs = 0;
                    if (sampleSize < 0) {
                        Log.d(TAG, "saw input EOS.");
                        sawInputEOS = true;
                        sampleSize = 0;
                    } else {
                        presentationTimeUs = extractor.getSampleTime();
                    }
                    codec.queueInputBuffer(
                            inputBufIndex,
                            0 /* offset */,
                            sampleSize,
                            presentationTimeUs,
                            sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                    if (!sawInputEOS) {
                        extractor.advance();
                    }
                }
            }
            int res = codec.dequeueOutputBuffer(info, kTimeOutUs);
            if (res >= 0) {
                //Log.d(TAG, "got frame, size " + info.size + "/" + info.presentationTimeUs);
                if (info.size > 0) {
                    noOutputCounter = 0;
                }
                if (info.size > 0 && reconfigure) {
                    // once we've gotten some data out of the decoder, reconfigure it again
                    reconfigure = false;
                    extractor.seekTo(0, MediaExtractor.SEEK_TO_NEXT_SYNC);
                    sawInputEOS = false;
                    codec.stop();
                    codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
                    codec.start();
                    codecInputBuffers = codec.getInputBuffers();
                    codecOutputBuffers = codec.getOutputBuffers();
                    continue;
                }
                int outputBufIndex = res;
                ByteBuffer buf = codecOutputBuffers[outputBufIndex];
                if (decodedIdx + (info.size / 2) >= decoded.length) {
                    decoded = Arrays.copyOf(decoded, decodedIdx + (info.size / 2));
                }
                for (int i = 0; i < info.size; i += 2) {
                    decoded[decodedIdx++] = buf.getShort(i);
                }
                codec.releaseOutputBuffer(outputBufIndex, false /* render */);
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "saw output EOS.");
                    sawOutputEOS = true;
                }
            } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                codecOutputBuffers = codec.getOutputBuffers();
                Log.d(TAG, "output buffers have changed.");
            } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat oformat = codec.getOutputFormat();
                Log.d(TAG, "output format has changed to " + oformat);
            } else {
                Log.d(TAG, "dequeueOutputBuffer returned " + res);
            }
        }
        codec.stop();
        codec.release();
        return decoded;
    }
}