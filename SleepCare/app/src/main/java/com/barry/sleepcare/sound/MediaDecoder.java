package com.barry.sleepcare.sound;

/* MediaDecoder

   Author: Andrew Stubbs (based on some examples from the docs)

   This class opens a file, reads the first audio channel it finds, and returns raw audio data.

   Usage:
      MediaDecoder decoder = new MediaDecoder("myfile.m4a");
      short[] data;
      while ((data = decoder.readShortData()) != null) {
         // process data here
      }
  */

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.barry.sleepcare.account.AccountActivity.TAG;

public class MediaDecoder {
    private MediaExtractor extractor = new MediaExtractor();
    private MediaCodec decoder;

    private MediaFormat inputFormat;

    private ByteBuffer[] inputBuffers;
    private boolean end_of_input_file;

    private ByteBuffer[] outputBuffers;
    private int outputBufferIndex = -1;

    public MediaDecoder(String inputFilename) {
        try {
            Log.d("MediaRecord", "Get track from: " + inputFilename);
            extractor.setDataSource(inputFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Select the first audio track we find.
        int numTracks = extractor.getTrackCount();
        Log.d("MediaRecord", "Get track num: " + numTracks);
        for (int i = 0; i < numTracks; ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                extractor.selectTrack(i);
                try {
                    decoder = MediaCodec.createDecoderByType(mime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                decoder.configure(format, null, null, 0);
                inputFormat = format;
                break;
            }
        }

        if (decoder == null) {
            throw new IllegalArgumentException("No decoder for file format");
        }

        decoder.start();
        inputBuffers = decoder.getInputBuffers();
        outputBuffers = decoder.getOutputBuffers();
        end_of_input_file = false;
    }

    // Read the raw data from MediaCodec.
    // The caller should copy the data out of the ByteBuffer before calling this again
    // or else it may get overwritten.
    private ByteBuffer readData(BufferInfo infoAudio) {

        boolean isEOS = false;
        long startMs = System.currentTimeMillis();
        long lasAudioStartMs = System.currentTimeMillis();
        while (!Thread.interrupted()) {

            if (!isEOS) {
                int inIndex = -1;
                try {
                    inIndex = decoder.dequeueInputBuffer(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {

                        decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        buffer.clear();
                        isEOS = true;
                    } else {
                        decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                        buffer.clear();
                        extractor.advance();
                    }
                }
            }

            int outIndex = -1;
            try {
                outIndex = decoder.dequeueOutputBuffer(infoAudio, 10000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = decoder.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG, "New format " + decoder.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d(TAG, "dequeueOutputBuffer timed out!");
                    break;
                default:
                    if (outIndex >= 0) {
                        ByteBuffer buffer = outputBuffers[outIndex];
                        byte[] chunk = new byte[infoAudio.size];
                        buffer.get(chunk);

                        return buffer;
                    }
                    break;
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((infoAudio.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }
        return null;
    }

    // Return the Audio sample rate, in samples/sec.
    public int getSampleRate() {
        return inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
    }

    // Read the raw audio data in 16-bit format
    // Returns null on EOF
    public short[] readShortData() {
        BufferInfo info = new BufferInfo();
        ByteBuffer data = readData(info);

        if (data == null)
            return null;

        int samplesRead = info.size / 2;
        short[] returnData = new short[samplesRead];

        // Converting the ByteBuffer to an array doesn't actually make a copy
        // so we must do so or it will be overwritten later.
       // data.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(returnData);
        System.arraycopy(data.asShortBuffer().array(), 0, returnData, 0, samplesRead);
        return returnData;
    }
}