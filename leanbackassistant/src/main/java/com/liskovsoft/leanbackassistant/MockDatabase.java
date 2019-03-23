package com.liskovsoft.leanbackassistant;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.provider.BaseColumns;
import com.liskovsoft.myvideotubeapi.Video;

@TargetApi(21)
public class MockDatabase {
    // The columns we'll include in the video database table
    public static final String KEY_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_DESCRIPTION = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String KEY_ICON = SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE;
    public static final String KEY_DATA_TYPE = SearchManager.SUGGEST_COLUMN_CONTENT_TYPE;
    public static final String KEY_IS_LIVE = SearchManager.SUGGEST_COLUMN_IS_LIVE;
    public static final String KEY_VIDEO_WIDTH = SearchManager.SUGGEST_COLUMN_VIDEO_WIDTH;
    public static final String KEY_VIDEO_HEIGHT = SearchManager.SUGGEST_COLUMN_VIDEO_HEIGHT;
    public static final String KEY_AUDIO_CHANNEL_CONFIG = SearchManager.SUGGEST_COLUMN_AUDIO_CHANNEL_CONFIG;
    public static final String KEY_PURCHASE_PRICE = SearchManager.SUGGEST_COLUMN_PURCHASE_PRICE;
    public static final String KEY_RENTAL_PRICE = SearchManager.SUGGEST_COLUMN_RENTAL_PRICE;
    public static final String KEY_RATING_STYLE = SearchManager.SUGGEST_COLUMN_RATING_STYLE;
    public static final String KEY_RATING_SCORE = SearchManager.SUGGEST_COLUMN_RATING_SCORE;
    public static final String KEY_PRODUCTION_YEAR = SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR;
    public static final String KEY_COLUMN_DURATION = SearchManager.SUGGEST_COLUMN_DURATION;
    public static final String KEY_ACTION = SearchManager.SUGGEST_COLUMN_INTENT_ACTION;

    public final static String[] QUERY_PROJECTION =
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

    public static Object[] convertVideoIntoRow(Video movie) {
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
}
