package com.barry.sleepcare.record;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.barry.sleepcare.R;
import com.barry.sleepcare.view.SlideToUnlock;

public class RecordActivity extends AppCompatActivity {

    SlideToUnlock mSwipeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mSwipeBtn = (SlideToUnlock) findViewById(R.id.swipe_button);


    }
}
