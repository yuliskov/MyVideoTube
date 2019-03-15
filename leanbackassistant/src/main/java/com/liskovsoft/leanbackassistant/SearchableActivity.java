package com.liskovsoft.leanbackassistant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.liskovsoft.sharedutils.mylogger.Log;

import static android.support.v4.content.IntentCompat.EXTRA_START_PLAYBACK;

public class SearchableActivity extends Activity {
    private static final String TAG = "SearchableActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Search data " + getIntent().getData());

        if (getIntent() != null && getIntent().getData() != null) {
            Uri uri = getIntent().getData();

            boolean startPlayback = getIntent().getBooleanExtra(EXTRA_START_PLAYBACK, false);
            Log.d(TAG, "Should start playback? " + (startPlayback ? "yes" : "no"));

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

        finish();
    }
}
