package com.barry.sleepcare.sound;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.barry.sleepcare.NavActivity;
import com.barry.sleepcare.R;
import com.barry.sleepcare.event.SoundEvent;
import com.barry.sleepcare.record.RecordActivity;
import com.barry.sleepcare.utils.FileUtils;
import com.barry.sleepcare.view.WaveformView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;

public class SoundAnalysisActivity extends NavActivity {

    WaveformView mWaveForm;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_analysis);

        mWaveForm = (WaveformView) findViewById(R.id.waveform);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_choose) {
            showFileChooser();
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String fileName = FileUtils.getPath(this, uri);

                    mProgressDialog = new ProgressDialog(SoundAnalysisActivity.this, R.style.Theme_MyDialog);
                    mProgressDialog.setMessage("Analyzing...");
                    mProgressDialog.show();
                    decodeFile(fileName);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void decodeFile(String fileName) {
        final AudioCodec audioCodec = AudioCodec.newInstance(MediaFormat.MIMETYPE_AUDIO_AAC);
        audioCodec.setIOPath(fileName, null);
        audioCodec.startAsync();

        audioCodec.setOnCompleteListener(new AudioCodec.OnCompleteListener() {
            @Override
            public void completed() {
                audioCodec.release();
                short[] pcm = audioCodec.getDecodedShortArray();
                mWaveForm.updateAudioData(pcm);

                SoundAnalyzer analyzer = SoundAnalyzer.getInstance();
                analyzer.setOnAnalyzedListener(new SoundAnalyzer.OnAnalyzedListener() {
                    @Override
                    public void onAnalyzedFinish(ArrayList<SoundEvent> soundEvents) {
                        mProgressDialog.dismiss();
                    }
                });
                analyzer.asyncAnalyze(pcm);
            }
        });
    }
}
