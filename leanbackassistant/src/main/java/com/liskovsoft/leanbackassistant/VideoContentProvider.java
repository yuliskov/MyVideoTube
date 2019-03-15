package com.liskovsoft.leanbackassistant;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.AppSchedulerProvider;
import com.liskovsoft.sharedutils.rx.SchedulerProvider;
import com.liskovsoft.myvideotubeapi.Video;
import com.liskovsoft.myvideotubeapi.VideoService;
import com.liskovsoft.youtubeapi.adapters.YouTubeVideoService;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import java.util.List;

/**
 * Provides global search on the app's movie service.<br/>
 * The assistant will query this provider for results.<br/>
 * Note: If you provide WatchAction feeds to Google, then you do not need this class.<br/>
 * You should still handle the playback intent and media controls in your fragment.<br/>
 * This class enables <a href="https://developer.android.com/training/tv/discovery/searchable.html">on-device search.</a>.
 */
public class VideoContentProvider extends ContentProvider {
    private static final String TAG = "VideoContentProvider";
    private static final String AUTHORITY = "com.example.android.assistantplayback";
    private VideoService mService;

    // UriMatcher constant for search suggestions
    private static final int SEARCH_SUGGEST = 1;

    private UriMatcher mUriMatcher;

    private final String[] queryProjection =
            new String[] {
                BaseColumns._ID,
                MockDatabase.KEY_NAME,
                MockDatabase.KEY_DESCRIPTION,
                MockDatabase.KEY_ICON,
                MockDatabase.KEY_DATA_TYPE,
                MockDatabase.KEY_IS_LIVE,
                MockDatabase.KEY_VIDEO_WIDTH,
                MockDatabase.KEY_VIDEO_HEIGHT,
                MockDatabase.KEY_AUDIO_CHANNEL_CONFIG,
                MockDatabase.KEY_PURCHASE_PRICE,
                MockDatabase.KEY_RENTAL_PRICE,
                MockDatabase.KEY_RATING_STYLE,
                MockDatabase.KEY_RATING_SCORE,
                MockDatabase.KEY_PRODUCTION_YEAR,
                MockDatabase.KEY_COLUMN_DURATION,
                MockDatabase.KEY_ACTION,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
            };
    private CompositeDisposable mDisposable;
    private SchedulerProvider mSchedulerProvider;

    @Override
    public boolean onCreate() {
        mService = new YouTubeVideoService();
        mUriMatcher = buildUriMatcher();
        return true;
    }

    private UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
                AUTHORITY, "/search/" + SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        uriMatcher.addURI(
                AUTHORITY,
                "/search/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
                SEARCH_SUGGEST);
        return uriMatcher;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {

        Log.d(TAG, uri.toString());

        if (mUriMatcher.match(uri) == SEARCH_SUGGEST) {
            Log.d(TAG, "Search suggestions requested.");

            return search(uri.getLastPathSegment());

        } else {
            Log.d(TAG, "Unknown uri to query: " + uri);
            throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private Cursor search(String query) {
        Observable<List<Video>> results = mService.findVideos(query);

        MatrixCursor matrixCursor = new MatrixCursor(queryProjection);

        getCompositeDisposable().add(results
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(videos -> {
                    Log.d(TAG, "Search result received: " + videos);

                    if (videos != null) {
                        for (Video video : videos) {
                            matrixCursor.addRow(convertVideoIntoRow(video));
                        }
                    }
        }));

        return matrixCursor;
    }

    private SchedulerProvider getSchedulerProvider() {
        if (mSchedulerProvider == null) {
            mSchedulerProvider = new AppSchedulerProvider();
        }

        return mSchedulerProvider;
    }

    private CompositeDisposable getCompositeDisposable() {
        if (mDisposable == null) {
            mDisposable = new CompositeDisposable();
        }

        return mDisposable;
    }

    private Object[] convertVideoIntoRow(Video movie) {
        return new Object[] {
            movie.getId(),
            movie.getTitle(),
            movie.getDescription(),
            movie.getCardImageUrl(),
            movie.getContentType(),
            movie.isLive(),
            movie.getWidth(),
            movie.getHeight(),
            movie.getAudioChannelConfig(),
            movie.getPurchasePrice(),
            movie.getRentalPrice(),
            movie.getRatingStyle(),
            movie.getRatingScore(),
            movie.getProductionDate(),
            movie.getDuration(),
            "GLOBALSEARCH",
            movie.getId()
        };
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        throw new UnsupportedOperationException("Insert is not implemented.");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException("Delete is not implemented.");
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues contentValues,
            @Nullable String s,
            @Nullable String[] strings) {
        throw new UnsupportedOperationException("Update is not implemented.");
    }
}
