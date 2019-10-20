package com.liskovsoft.leanbackassistant.channels;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.liskovsoft.leanbackassistant.R;
import com.liskovsoft.leanbackassistant.utils.AppUtil;
import com.liskovsoft.sharedutils.mylogger.Log;

public class RecommendationReceiverActivity extends Activity implements SampleClipApi.GetClipByIdListener {
    private static final String TAG = RecommendationReceiverActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Channels data " + getIntent().getData());
        
        String videoId = SampleTvProvider.decodeVideoId(getIntent().getData());

        MySampleClipApi.getClipById(videoId, this);
    }

    @Override
    public void onGetClipById(Clip clip) {
        if (clip != null) {
            Log.d(TAG, "Opening the clip " + clip);

            Intent playVideo = AppUtil.getInstance(this).createVideoIntent(clip.getVideoUrl());

            startActivity(playVideo);
        } else {
            Log.d(TAG, "Oops clip is null");
            Toast.makeText(this, getResources().getString(R.string.cant_play_video),
                    Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SampleClipApi.cancelGetClipById(this);
    }
}
