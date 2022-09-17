package com.example.digitrecognition;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private Button btnClassify;
    private ImageButton btnClear;
    Interpreter tflite;
    private HorizontalBarChart chart;
    ArrayList<BarEntry> values = new ArrayList<>();

    @Override
    protected void onCreate(Bundle InstanceState) {
        super.onCreate(InstanceState);
        setContentView(R.layout.activity_main);

        chart = findViewById(R.id.chart);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setMaxVisibleValueCount(11);

        XAxis xl = chart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawGridLines(false);
        xl.setCenterAxisLabels(false);
        xl.setTextSize(12);
        xl.setLabelCount(10);

        YAxis yl = chart.getAxisLeft();
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0f);

        YAxis yr = chart.getAxisRight();
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setAxisMinimum(0f);

        chart.setFitBars(true);

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setFormSize(8f);
        legend.setXEntrySpace(4f);

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        btnClassify = findViewById(R.id.btnClassify);
        btnClassify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    drawingView.setDrawingCacheEnabled(true);
                    drawingView.buildDrawingCache();
                    Bitmap bitmap = drawingView.getDrawingCache().copy(Bitmap.Config.RGB_565, false);
                    drawingView.setDrawingCacheEnabled(false);
                    Bitmap resized = Bitmap.createScaledBitmap(bitmap, 28, 28, true);
                    ByteBuffer buff = convertBitmapToByteBuffer(resized);
                    digitClassifier(buff);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clear();
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        drawingView = findViewById(R.id.paintView);
        drawingView.setLayoutParams(new ConstraintLayout.LayoutParams(width, width));
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        drawingView.init(metrics);
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("mnist_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @SuppressLint("DefaultLocale")
    private void digitClassifier(ByteBuffer byteBufferToClassify) {
        float[][] result = new float[1][10];
        tflite.run(byteBufferToClassify, result);

        int maxIndex = -1;
        float maxScore = 0;
        values = new ArrayList<>();
        for (int i = 0; i < result[0].length; i++) {
            values.add(new BarEntry(i, result[0][i]*100.f));
            if(result[0][i] > maxScore) {
                maxIndex = i;
                maxScore = result[0][i];
            }
        }

        BarDataSet set;
        set = new BarDataSet(values, "Results in percentages");

        ArrayList<IBarDataSet> dataSet = new ArrayList<>();
        dataSet.add(set);

        BarData data = new BarData(dataSet);
        data.setValueTextSize(12f);
        chart.setData(data);
        chart.animateY(400);

        Toast.makeText(getApplicationContext(), "Detected Digit: " + String.valueOf(maxIndex), Toast.LENGTH_LONG).show();
    }

    private float[] pixelToChannelValue(int pixel) {
        float[] singleChannelVal = new float[1];
        float rChannel = (pixel >> 16) & 0xFF;
        float gChannel = (pixel >> 8) & 0xFF;
        float bChannel = (pixel) & 0xFF;
        singleChannelVal[0] = (rChannel + gChannel + bChannel) / 3 / 255.f;
        return singleChannelVal;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        int SIZE = 28;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(SIZE * SIZE * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[SIZE * SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                int pixelVal = intValues[pixel++];
                for (float channelVal : pixelToChannelValue(pixelVal)) {
                    byteBuffer.putFloat(channelVal);
                }
            }
        }
        return byteBuffer;
    }

}