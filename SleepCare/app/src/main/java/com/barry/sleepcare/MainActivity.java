package com.barry.sleepcare;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.barry.sleepcare.account.LoginActivity;
import com.barry.sleepcare.record.RecordActivity;
import com.barry.sleepcare.view.WaveLoadingView;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends DrawerActivity {

    static final String TAG = "MainActivity";
    static final int REQUEST_CODE = 11111;

    private StorageReference mStorageRef;

    ImageView mImage;
    WaveLoadingView mWaveLoadingView;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImage = (ImageView) findViewById(R.id.image);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://sleepcare-85365.appspot.com");
        mWaveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        mWaveLoadingView.setBottomTitle("Sleep Qulity");
        mWaveLoadingView.setBottomTitleColor(Color.WHITE);
        mWaveLoadingView.setBottomTitleSize(12f);
        mWaveLoadingView.setWaveShiftRatio(80);

        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                       startRecord();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                        return;
                    }
                }
                startRecord();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            // TODO: Update UI
        }
        mWaveLoadingView.setProgressValue(90);
        mWaveLoadingView.setAmplitudeRatio(60);
        mWaveLoadingView.setCenterTitle("90");
    }

    @Override
    protected void onResume() {
        super.onResume();

        getImage();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getImage() {
        StorageReference spaceRef = mStorageRef.child("Sample01.jpg");
        Log.w(TAG, "Path: " + spaceRef.getName());

        Glide.with(this /* context */)
                .using(new FirebaseImageLoader())
                .load(spaceRef)
                .into(mImage);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            startRecord();
        }
    }

    private void startRecord() {
        Intent intent = new Intent(MainActivity.this, RecordActivity.class);
        startActivity(intent);
    }
}
