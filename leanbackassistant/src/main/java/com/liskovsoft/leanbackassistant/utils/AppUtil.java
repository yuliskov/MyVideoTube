package com.liskovsoft.leanbackassistant.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.liskovsoft.sharedutils.configparser.AssetPropertyParser2;
import com.liskovsoft.sharedutils.configparser.ConfigParser;

public class AppUtil {
    private final Context mContext;
    private ConfigParser mParser;
    private static AppUtil sInstance;

    public AppUtil(Context context) {
        mContext = context;
    }

    public static AppUtil getInstanse(Context context) {
        if (sInstance == null) {
            sInstance = new AppUtil(context);
        }

        return sInstance;
    }

    public String getBootstrapClassName() {
        ConfigParser parser = getParser();

        return parser.get("app_bootsrap_class_name");
    }

    public String getAppPackageName() {
        ConfigParser parser = getParser();

        return parser.get("app_package_name");
    }

    private ConfigParser getParser() {
        if (mParser == null) {
            mParser = new AssetPropertyParser2(mContext, "common.properties");
        }

        return mParser;
    }

    public Intent createVideoIntent(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setClassName(getAppPackageName(), getBootstrapClassName());

        return intent;
    }
}
