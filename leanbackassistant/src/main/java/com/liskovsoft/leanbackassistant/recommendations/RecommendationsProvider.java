package com.liskovsoft.leanbackassistant.recommendations;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.liskovsoft.leanbackassistant.R;
import com.liskovsoft.leanbackassistant.media.Clip;
import com.liskovsoft.leanbackassistant.media.Playlist;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.okhttp.OkHttpHelpers;
import okhttp3.Response;

@TargetApi(21)
public class RecommendationsProvider {
    private static final String TAG = RecommendationsProvider.class.getSimpleName();
    private static final int MAX_RECOMMENDATIONS = 3;

    public static void createOrUpdateRecommendations(Context context, Playlist playlist) {
        if (playlist != null) {
            int clipCounter = 0;
            for (Clip clip : playlist.getClips()) {
                if (clipCounter++ > MAX_RECOMMENDATIONS) {
                    break;
                }

                Response response = OkHttpHelpers.doGetOkHttpRequest(clip.getCardImageUrl());

                Bitmap image = null;

                if (response.body() != null) {
                    image = BitmapFactory.decodeStream(response.body().byteStream());
                }

                new RecommendationBuilder()
                        .setContext(context)
                        .setDescription(clip.getDescription())
                        .setImage(image)
                        .setTitle(clip.getTitle())
                        .setSmallIcon(R.drawable.app_icon)
                        .build();

                Log.d(TAG, "Posting recommendation: " + clip.getTitle());
            }
        }
    }

    //private PendingIntent buildPendingIntent(Movie movie) {
    //    Intent detailsIntent = new Intent(this, DetailsActivity.class);
    //    detailsIntent.putExtra("Movie", movie);
    //
    //    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    //    stackBuilder.addParentStack(DetailsActivity.class);
    //    stackBuilder.addNextIntent(detailsIntent);
    //    // Ensure a unique PendingIntents, otherwise all
    //    // recommendations end up with the same PendingIntent
    //    detailsIntent.setAction(Long.toString(movie.getId()));
    //
    //    PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    //    return intent;
    //}
}
