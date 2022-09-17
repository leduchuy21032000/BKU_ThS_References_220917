package com.numbers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

public class DigitsClassifier {
    private static final String MODEL_FILENAME = "mnist_model.tflite";
    private static final String LABEL_FILENAME = "labelmap.txt";
    private static final int INPUT_SIZE = 28;
    private static final int NUM_BYTES_PER_CHANNEL = 4;
    private static final int NUM_DETECTIONS = 10;
    private static final String LOGGING_TAG = DigitsClassifier.class.getName();

    private Interpreter tfLite;
    private Context ctx;

    private float[][] outputClasses;
    private Vector<String> labels = new Vector<String>();

    private DigitsClassifier(final AssetManager assetManager, final Context ctx) throws IOException {
        init(assetManager, ctx);
    }

    private void init(final AssetManager assetManager, final Context ctx) throws IOException {
        this.ctx = ctx;
        outputClasses = new float[1][NUM_DETECTIONS];

        InputStream labelsInput = assetManager.open(LABEL_FILENAME);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }
        br.close();

        try {
            tfLite = new Interpreter(loadModelFile(assetManager));
            Log.i(LOGGING_TAG, "Input tensor shapes:");
            for (int i=0; i<tfLite.getInputTensorCount(); i++) {
                int[] shape = tfLite.getInputTensor(i).shape();
                String stringShape = "";
                for(int j = 0; j < shape.length; j++) {
                    stringShape = stringShape + ", " + shape[j];
                }
                Log.i(LOGGING_TAG, "Shape of input tensor " + i + ": " + stringShape);
            }
            Log.i(LOGGING_TAG, "Output tensor shapes:");
            for (int i=0; i<tfLite.getOutputTensorCount(); i++) {
                int[] shape = tfLite.getOutputTensor(i).shape();
                String stringShape = "";
                for(int j = 0; j < shape.length; j++) {
                    stringShape = stringShape + ", " + shape[j];
                }
                Log.i(LOGGING_TAG, "Shape of output tensor " + i + ": " + tfLite.getOutputTensor(i).name() + " " + stringShape);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public static DigitsClassifier create(final AssetManager assetManager, final Context ctx) throws IOException {
        return new DigitsClassifier(assetManager, ctx);
    }

    private static MappedByteBuffer loadModelFile(AssetManager assets)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(MODEL_FILENAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void close() {
        tfLite.close();
    }

    public String classifyDigits(final Bitmap bitmap) {
        Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(resizedImage);

        tfLite.run(byteBuffer, outputClasses);

        int maxIndex = -1;
        float maxScore = 0;
        for (int i = 0; i < NUM_DETECTIONS; ++i) {
            Log.i(LOGGING_TAG, "Output Label " + i + ": " + outputClasses[0][i]);
            if(outputClasses[0][i] > maxScore) {
                maxIndex = i;
                maxScore = outputClasses[0][i];
            }
        }
        if(maxIndex != -1) {
            Log.i(LOGGING_TAG, "detected label: " + labels.get(maxIndex));
            return labels.get(maxIndex);
        } else {
            return "None";
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer imgData = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * NUM_BYTES_PER_CHANNEL);
        imgData.order(ByteOrder.nativeOrder());
        imgData.rewind();

        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixelValue: intValues) {
            float red = ((pixelValue >> 16) & 0xFF);
            float green = ((pixelValue >> 8) & 0xFF);
            float blue = (pixelValue & 0xFF);
            float normalizedPixelValue = (red + green + blue) / 3.0f;
            imgData.putFloat(normalizedPixelValue);
        }
        return imgData;
    }
}
