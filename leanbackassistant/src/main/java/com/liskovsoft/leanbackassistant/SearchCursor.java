package com.liskovsoft.leanbackassistant;

import android.database.MatrixCursor;
import com.liskovsoft.myvideotubeapi.Video;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.youtubeapi.adapters.YouTubeVideoService;

import java.util.ArrayList;
import java.util.List;

public class SearchCursor extends MatrixCursor {
    private static final String TAG = SearchCursor.class.getSimpleName();
    private static final int MIN_ROWS = 10;
    private static final int VIRTUAL_ROW_COUNT = 99;
    private final YouTubeVideoService mService;
    private List<Video> mCachedVideos;
    private int mIdx = 0;

    public SearchCursor(String searchTerm) {
        super(MockDatabase.QUERY_PROJECTION);

        mService = new YouTubeVideoService();
        initSearch(searchTerm);
    }

    public Video findVideoWithId(int id) {
        if (mCachedVideos != null) {
            for (Video video : mCachedVideos) {
                if (video.getId() == id) {
                    return video;
                }
            }
        }

        return null;
    }

    private void initSearch(String query) {
        List<Video> videos = mService.findVideos2(query);

        Log.d(TAG, "Search result received: " + videos);

        mCachedVideos = new ArrayList<>();

        apply(videos);
    }

    private void apply(List<Video> videos) {
        if (videos != null) {
            for (Video video : videos) {
                addRow(MockDatabase.convertVideoIntoRow(video));
                mIdx++;
            }

            mCachedVideos.addAll(videos);

            if (mIdx < MIN_ROWS) {
                nextSearch();
            } else {
                mIdx = 0;
            }
        }
    }

    private void nextSearch() {
        List<Video> videos = mService.getNextSearchPage();

        Log.d(TAG, "Next search result received: " + videos);

        apply(videos);
    }

    private void ensureSize(int size) {
        if (mCachedVideos.size() < size) {
            nextSearch();
        }
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        ensureSize(newPosition + 1);

        return super.onMove(oldPosition, newPosition);
    }

    @Override
    public int getCount() {
        return VIRTUAL_ROW_COUNT;
    }
}
