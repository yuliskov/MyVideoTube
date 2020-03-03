package com.liskovsoft.leanbackassistant.recommendations;

import android.app.PendingIntent;

public class RecommendationsProvider {
    private static final String TAG = RecommendationsProvider.class.getSimpleName();
    private static final int MAX_RECOMMENDATIONS = 3;

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
