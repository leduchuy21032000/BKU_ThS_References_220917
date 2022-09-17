package com.numbers;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private DrawingView drawingView;
    DigitsClassifier digitClassifier;
    private Button clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!(hasPermission())) {
            requestPermission();
        }

        try {
            digitClassifier = DigitsClassifier.create(getAssets(), getApplicationContext());
            Log.i(this.getClass().getName(), "Model Initiated successfully.");
            Toast.makeText(getApplicationContext(), "DigitsClassifier created", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "DigitsClassifier could not be created", Toast.LENGTH_SHORT).show();
            finish();
        }

        clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener((view) -> {
            drawingView.clear();
        });

        drawingView = findViewById(R.id.drawView);
        drawingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    classifyDigit();
                    return true;
                }
                return false;
            }
        });
    }

    protected void classifyDigit() {
        Bitmap image = Bitmap.createBitmap(drawingView.getWidth(), drawingView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(Color.BLACK);
        drawingView.draw(canvas);

        String digit = digitClassifier.classifyDigits(image);
        Toast.makeText(getApplicationContext(), "Detected Digit: " + digit, Toast.LENGTH_LONG).show();
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onDestroy() {
        digitClassifier.close();
        super.onDestroy();
    }
}
