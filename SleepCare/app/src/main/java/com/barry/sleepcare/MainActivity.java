package com.barry.sleepcare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
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
}
