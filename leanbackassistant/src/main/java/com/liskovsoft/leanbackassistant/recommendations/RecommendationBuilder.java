package com.liskovsoft.leanbackassistant.recommendations;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recommendation.app.ContentRecommendation;
import com.liskovsoft.leanbackassistant.R;

@RequiresApi(21)
public class RecommendationBuilder {
    private String mTitle;
    private String mDescription;
    private Context mContext;
    private int mSmallIcon;
    private Intent mIntent;
    private Bitmap mImage;

    public RecommendationBuilder setTitle(String title) {
        mTitle = title;
        return this;
    }

    public RecommendationBuilder setDescription(String description) {
        mDescription = description;
        return this;
    }

    public RecommendationBuilder setImage(Bitmap image) {
        mImage = image;
        return this;
    }

    public RecommendationBuilder setContext(Context context) {
        mContext = context;
        return this;
    }

    public RecommendationBuilder setSmallIcon(int smallIcon) {
        mSmallIcon = smallIcon;
        return this;
    }

    public RecommendationBuilder setIntent(Intent intent) {
        mIntent = intent;
        return this;
    }

    public ContentRecommendation build() {
        ContentRecommendation rec = new ContentRecommendation.Builder()
                .setTitle(mTitle)
                .setText(mDescription)
                .setColor(ContextCompat.getColor(mContext, R.color.fastlane_background))
                .setContentImage(mImage)
                .setBadgeIcon(mSmallIcon)
                .setContentIntentData(ContentRecommendation.INTENT_TYPE_ACTIVITY, mIntent, 0, null)
                .build();

        return rec;
    }
}
