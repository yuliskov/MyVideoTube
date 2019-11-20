package com.liskovsoft.leanbackassistant.channels;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.tv.TvContract;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.annotation.DrawableRes;
import androidx.annotation.WorkerThread;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Channel.Builder;
import androidx.tvprovider.media.tv.ChannelLogoUtils;
import androidx.tvprovider.media.tv.PreviewProgram;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.TvContractCompat.Channels;
import androidx.tvprovider.media.tv.WatchNextProgram;
import android.text.TextUtils;

import com.liskovsoft.leanbackassistant.channels.scheduler.ClipData;
import com.liskovsoft.leanbackassistant.utils.AppUtil;
import com.liskovsoft.sharedutils.mylogger.Log;

import java.util.List;

public class SampleTvProvider {
    private static final String TAG = SampleTvProvider.class.getSimpleName();
    private static final String SCHEME = "tvhomescreenchannels";
    private static final String APPS_LAUNCH_HOST = "com.google.android.tvhomescreenchannels";
    private static final String PLAY_VIDEO_ACTION_PATH = "playvideo";
    /**
     * Index into "WATCH_NEXT_MAP_PROJECTION" and if that changes, this should change too.
     */
    private static final int COLUMN_WATCH_NEXT_ID_INDEX = 0;
    private static final int COLUMN_WATCH_NEXT_INTERNAL_PROVIDER_ID_INDEX = 1;
    private static final int COLUMN_WATCH_NEXT_COLUMN_BROWSABLE_INDEX = 2;

    private static final String[] WATCH_NEXT_MAP_PROJECTION =
            {BaseColumns._ID, TvContractCompat.WatchNextPrograms.COLUMN_INTERNAL_PROVIDER_ID,
                    TvContractCompat.WatchNextPrograms.COLUMN_BROWSABLE};

    private static final Uri PREVIEW_PROGRAMS_CONTENT_URI =
            Uri.parse("content://android.media.tv/preview_program");

    @TargetApi(21)
    private static String[] CHANNELS_PROJECTION = {
            TvContractCompat.Channels._ID,
            TvContract.Channels.COLUMN_DISPLAY_NAME,
            TvContractCompat.Channels.COLUMN_BROWSABLE};

    private SampleTvProvider() {
    }

    static private String createInputId(Context context) {
        // TODO: tv input service component name
        ComponentName cName = new ComponentName(context, SampleTvProvider.class.getName());
        return TvContractCompat.buildInputId(cName);
    }

    /**
     * Writes a drawable as the channel logo.
     *
     * @param channelId  identifies the channel to write the logo.
     * @param drawableId resource to write as the channel logo. This must be a bitmap and not, say
     *                   a vector drawable.
     */
    @WorkerThread
    static private void writeChannelLogo(Context context, long channelId, @DrawableRes int drawableId) {
        if (channelId != -1 && drawableId != -1) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);
            ChannelLogoUtils.storeChannelLogo(context, channelId, bitmap);
        }
    }

    @WorkerThread
    public static void addWatchNextContinue(Context context, ClipData clipData) {
        final String clipId = clipData.getClipId();
        final String contentId = clipData.getContentId();

        // Check if program "key" has already been added.
        boolean isProgramPresent = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    TvContractCompat.WatchNextPrograms.CONTENT_URI, WATCH_NEXT_MAP_PROJECTION, null,
                    null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (!cursor.isNull(COLUMN_WATCH_NEXT_INTERNAL_PROVIDER_ID_INDEX)
                            && TextUtils.equals(clipId, cursor.getString(
                            COLUMN_WATCH_NEXT_INTERNAL_PROVIDER_ID_INDEX))) {
                        // Found a row that contains an equal COLUMN_INTERNAL_PROVIDER_ID.
                        long watchNextProgramId = cursor.getLong(COLUMN_WATCH_NEXT_ID_INDEX);
                        // If the clip exists in watch next programs, there are 2 cases:
                        // 1. The program was not removed by the user (browsable == 1) and we
                        // only need to update the existing info for that program
                        // 2. The program was removed by the user from watch next
                        // (browsable== 0), in which case we will first remove it from watch
                        // next database and then treat it as a new watch next program to be
                        // inserted.
                        if (cursor.getInt(COLUMN_WATCH_NEXT_COLUMN_BROWSABLE_INDEX) == 0) {
                            int rowsDeleted = context.getContentResolver().delete(
                                    TvContractCompat.buildWatchNextProgramUri(
                                            watchNextProgramId), null,
                                    null);
                            if (rowsDeleted < 1) {
                                Log.e(TAG, "Delete program failed");
                            }
                        } else {
                            WatchNextProgram existingProgram = WatchNextProgram.fromCursor(
                                    cursor);
                            // Updating the following columns since when a program is added
                            // manually through the launcher interface to the WatchNext row:
                            // 1. watchNextType is set to WATCH_NEXT_TYPE_WATCHLIST which
                            // should be changed to WATCH_NEXT_TYPE_CONTINUE when at least 1
                            // minute of the video is played.
                            // 2. The duration may not have been set for the programs in a
                            // channel row since the video wasn't processed then to set this
                            // column. Also setting lastPlaybackPosition to maintain the
                            // correct progressBar upon returning to the launcher.
                            WatchNextProgram.Builder builder = new WatchNextProgram.Builder(
                                    existingProgram)
                                    .setWatchNextType(TvContractCompat.WatchNextPrograms
                                            .WATCH_NEXT_TYPE_CONTINUE)
                                    .setLastPlaybackPositionMillis((int) clipData.getProgress())
                                    .setDurationMillis((int) clipData.getDuration());
                            ContentValues contentValues = builder.build().toContentValues();
                            Uri watchNextProgramUri = TvContractCompat.buildWatchNextProgramUri(
                                    watchNextProgramId);
                            int rowsUpdated = context.getContentResolver().update(
                                    watchNextProgramUri,
                                    contentValues, null, null);
                            if (rowsUpdated < 1) {
                                Log.e(TAG, "Update program failed");
                            }
                            isProgramPresent = true;
                        }
                    }
                }
            }
            if (!isProgramPresent) {
                WatchNextProgram.Builder builder = new WatchNextProgram.Builder();
                builder.setType(TvContractCompat.WatchNextPrograms.TYPE_CLIP)
                        .setWatchNextType(
                                TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE)
                        .setLastEngagementTimeUtcMillis(System.currentTimeMillis())
                        .setTitle(clipData.getTitle())
                        .setDescription(clipData.getDescription())
                        .setPosterArtUri(Uri.parse(clipData.getCardImageUrl()))
                        .setIntentUri(Uri.parse(SCHEME + "://" + APPS_LAUNCH_HOST
                                + "/" + PLAY_VIDEO_ACTION_PATH + "/" + clipId))
                        .setInternalProviderId(clipId)
                        // Setting the contentId to avoid having duplicate programs with the same
                        // content added to the watch next row (The launcher will use the contentId
                        // to detect duplicates). Note that, programs of different channels can
                        // still point to the same content i.e. their contentId can be the same.
                        .setContentId(contentId)
                        .setLastPlaybackPositionMillis((int) clipData.getProgress())
                        .setDurationMillis((int) clipData.getDuration());
                ContentValues contentValues = builder.build().toContentValues();
                Uri programUri = context.getContentResolver().insert(
                        TvContractCompat.WatchNextPrograms.CONTENT_URI, contentValues);
                if (programUri == null || programUri.equals(Uri.EMPTY)) {
                    Log.e(TAG, "Insert watch next program failed");
                }
            }

            // TODO: update api
            //SampleContentDb.getInstance(context).updateClipProgress(clipId, clipData.getProgress());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @WorkerThread
    public static void deleteWatchNextContinue(Context context, String clipId) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    TvContractCompat.WatchNextPrograms.CONTENT_URI, WATCH_NEXT_MAP_PROJECTION, null,
                    null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (!cursor.isNull(COLUMN_WATCH_NEXT_INTERNAL_PROVIDER_ID_INDEX)
                            && TextUtils.equals(clipId, cursor.getString(
                            COLUMN_WATCH_NEXT_INTERNAL_PROVIDER_ID_INDEX))) {
                        long watchNextProgramId = cursor.getLong(COLUMN_WATCH_NEXT_ID_INDEX);
                        int rowsDeleted = context.getContentResolver().delete(
                                TvContractCompat.buildWatchNextProgramUri(watchNextProgramId), null,
                                null);
                        if (rowsDeleted < 1) {
                            Log.e(TAG, "Delete program failed");
                        }

                        // TODO: delete api
                        //SampleContentDb.getInstance(context).deleteClipProgress(clipId);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @WorkerThread
    public static long createOrUpdateChannel(Context context, Playlist playlist) {
        long oldChannelId = playlist.getChannelId();

        if (oldChannelId != -1) {
            Log.d(TAG, "Oops: channel already published. Doing update instead... " + oldChannelId);
            updateChannel(context, playlist);
            addClipsToChannel(context, oldChannelId, playlist.getClips());
            return oldChannelId;
        }

        long foundId = findChannel(context, playlist.getName());

        if (foundId != -1) {
            Log.d(TAG, "Oops: channel already published but not memorized by the app. Doing update instead... " + foundId);
            playlist.setChannelPublishedId(foundId);
            updateChannel(context, playlist);
            addClipsToChannel(context, foundId, playlist.getClips());
            return foundId;
        }

        long channelId = createChannel(context, playlist);

        addClipsToChannel(context, channelId, playlist.getClips());

        return channelId;
    }

    private static long createChannel(Context context, Playlist playlist) {
        Channel.Builder builder = createChannelBuilder(context, playlist);

        Uri channelUri = null;

        try {
            channelUri = context.getContentResolver().insert(
                    Channels.CONTENT_URI,
                    builder.build().toContentValues());
        } catch (Exception e) { // channels not supported
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        if (channelUri == null || channelUri.equals(Uri.EMPTY)) {
            Log.e(TAG, "Insert channel failed");
            return -1;
        }

        long channelId = ContentUris.parseId(channelUri);

        playlist.setChannelPublishedId(channelId);

        writeChannelLogo(context, channelId, playlist.getLogoResId());

        return channelId;
    }

    @WorkerThread
    private static void addClipsToChannel(Context context, long channelId, List<Clip> clips) {
        if (channelId == -1) {
            Log.d(TAG, "Cant add clips: channelId == -1");
            return;
        }

        if (clips.size() == 0) {
            Log.d(TAG, "Cant add clips: clips.size() == 0");
            return;
        }

        cleanupChannel(context, channelId);

        int weight = clips.size();
        for (int i = 0; i < clips.size(); ++i, --weight) {
            Clip clip = clips.get(i);

            publishProgram(context, clip, channelId, weight);
        }
    }

    private static void cleanupChannel(Context context, long channelId) {
        context.getContentResolver().delete(TvContractCompat.buildPreviewProgramsUriForChannel(channelId), null, null);
    }

    @WorkerThread
    private static void updateChannel(Context context, Playlist playlist) {
        long channelId = playlist.getChannelId();

        if (channelId == -1) {
            Log.d(TAG, "Error: channel not published yet: " + channelId);
            return;
        }

        writeChannelLogo(context, channelId, playlist.getLogoResId());

        Builder builder = createChannelBuilder(context, playlist);

        int rowsUpdated = context.getContentResolver().update(
                TvContractCompat.buildChannelUri(channelId), builder.build().toContentValues(), null, null);

        if (rowsUpdated < 1) {
            Log.e(TAG, "Update channel failed");
        } else {
            Log.d(TAG, "Channel updated " + playlist.getName());
        }
    }

    @WorkerThread
    static void deleteChannel(Context context, long channelId) {
        if (channelId == -1) {
            Log.d(TAG, "Invalid channel id " + channelId);
            return;
        }

        int rowsDeleted = context.getContentResolver().delete(
                TvContractCompat.buildChannelUri(channelId), null, null);
        if (rowsDeleted < 1) {
            Log.e(TAG, "Delete channel failed");
        }
    }

    @WorkerThread
    public static void deleteProgram(Context context, Clip clip) {
        deleteProgram(context, clip.getProgramId());
    }

    @WorkerThread
    private static void deleteProgram(Context context, long programId) {
        int rowsDeleted = context.getContentResolver().delete(
                TvContractCompat.buildPreviewProgramUri(programId), null, null);
        if (rowsDeleted < 1) {
            Log.e(TAG, "Delete program failed");
        }
    }

    private static void publishProgram(Context context, Clip clip, long channelId, int weight) {
        if (clip.getProgramId() != -1) {
            Log.e(TAG, "Clip already published. Exiting...");
            return;
        }

        PreviewProgram.Builder builder =
                createProgramBuilder(context, clip)
                        .setWeight(weight)
                        .setChannelId(channelId);

        Uri programUri = null;

        try {
            programUri = context.getContentResolver().insert(PREVIEW_PROGRAMS_CONTENT_URI, builder.build().toContentValues());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        if (programUri == null || programUri.equals(Uri.EMPTY)) {
            Log.e(TAG, "Insert program failed");
            return;
        }

        clip.setProgramId(ContentUris.parseId(programUri));
    }

    static String decodeVideoId(Uri uri) {
        List<String> paths = uri.getPathSegments();
        if (paths.size() == 2 && TextUtils.equals(paths.get(0), PLAY_VIDEO_ACTION_PATH)) {
            return paths.get(1);
        }

        return new String();
    }

    @WorkerThread
    static void setProgramViewCount(Context context, long programId, int numberOfViews) {
        Uri programUri = TvContractCompat.buildPreviewProgramUri(programId);
        try (Cursor cursor = context.getContentResolver().query(programUri, null, null, null,
                null)) {
            if (!cursor.moveToFirst()) {
                return;
            }
            PreviewProgram existingProgram = PreviewProgram.fromCursor(cursor);
            PreviewProgram.Builder builder = new PreviewProgram.Builder(existingProgram)
                    .setInteractionCount(numberOfViews)
                    .setInteractionType(TvContractCompat.PreviewProgramColumns
                            .INTERACTION_TYPE_VIEWS);
            int rowsUpdated = context.getContentResolver().update(
                    TvContractCompat.buildPreviewProgramUri(programId),
                    builder.build().toContentValues(), null, null);
            if (rowsUpdated != 1) {
                Log.e(TAG, "Update program failed");
            }
        }
    }

    private static PreviewProgram.Builder createProgramBuilder(Context context, Clip clip) {
        return createProgramBuilder(new PreviewProgram.Builder(), context, clip);
    }

    private static PreviewProgram.Builder createProgramBuilder(PreviewProgram.Builder baseBuilder, Context context, Clip clip) {
        Uri previewUri = clip.getPreviewVideoUrl() == null ? null : Uri.parse(clip.getPreviewVideoUrl());
        Uri cardUri = clip.getCardImageUrl() == null ? null : Uri.parse(clip.getCardImageUrl());

        baseBuilder
            .setTitle(clip.getTitle())
            .setDescription(clip.getDescription())
            .setPosterArtUri(cardUri)
            .setIntent(AppUtil.getInstance(context).createAppIntent(clip.getVideoUrl()))
            .setPreviewVideoUri(previewUri)
            .setInternalProviderId(clip.getClipId())
            .setContentId(clip.getContentId())
            .setPosterArtAspectRatio(clip.getAspectRatio())
            .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE);

        return baseBuilder;
    }

    private static Channel.Builder createChannelBuilder(Context context, Playlist playlist) {
        Channel.Builder builder = new Channel.Builder()
                .setDisplayName(playlist.getName())
                .setDescription(playlist.getDescription())
                .setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setInputId(createInputId(context))
                .setAppLinkIntent(AppUtil.getInstance(context).createAppIntent(playlist.getPlaylistUrl()))
                .setInternalProviderId(playlist.getPlaylistId());

        return builder;
    }

    private static long findChannel(Context context, String name) {
        Cursor cursor = context.getContentResolver().query(
                TvContractCompat.Channels.CONTENT_URI,
                CHANNELS_PROJECTION,
                null,
                null,
                null
        );

        long channelId = -1;

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        Channel channel = Channel.fromCursor(cursor);
                        if (name.equals(channel.getDisplayName())) {
                            if (channelId == -1) {
                                Log.d(TAG, "Channel found: " + name);
                                channelId = channel.getId();
                            } else {
                                Log.d(TAG, "Duplicate channel deleted: " + name);
                                deleteChannel(context, channel.getId());
                            }
                        }

                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }

        }

        return channelId;
    }

    //@WorkerThread
    //static void updateChannelOld(Context context, Playlist playlist) {
    //    long channelId = playlist.getChannelId();
    //
    //    if (channelId == -1) {
    //        Log.d(TAG, "Error: channel not published yet: " + channelId);
    //        return;
    //    }
    //
    //    writeChannelLogo(context, channelId, playlist.getLogoResId());
    //
    //    Builder builder = createChannelBuilder(context, playlist);
    //
    //    int rowsUpdated = context.getContentResolver().update(
    //            TvContractCompat.buildChannelUri(channelId), builder.build().toContentValues(), null, null);
    //
    //    if (rowsUpdated < 1) {
    //        Log.e(TAG, "Update channel failed");
    //    } else {
    //        Log.d(TAG, "Channel updated " + playlist.getName());
    //    }
    //}

    //@WorkerThread
    //static long addChannel(Context context, Playlist playlist) {
    //    long oldChannelId = playlist.getChannelId();
    //
    //    if (oldChannelId != -1) {
    //        Log.d(TAG, "Error: channel already published: " + oldChannelId);
    //        return oldChannelId;
    //    }
    //
    //    long foundId = findChannel(context, playlist.getName());
    //
    //    if (foundId != -1) {
    //        Log.d(TAG, "Error: channel already published but not memorized by the app: " + foundId);
    //        playlist.setChannelPublishedId(foundId);
    //        addClipsToChannel(context, foundId, playlist.getClips());
    //        return foundId;
    //    }
    //
    //    Channel.Builder builder = createChannelBuilder(context, playlist);
    //
    //    Uri channelUri = null;
    //
    //    try {
    //        channelUri = context.getContentResolver().insert(Channels.CONTENT_URI,
    //                builder.build().toContentValues());
    //    } catch (Exception e) { // channels not supported
    //        Log.e(TAG, e.getMessage());
    //        e.printStackTrace();
    //    }
    //
    //    if (channelUri == null || channelUri.equals(Uri.EMPTY)) {
    //        Log.e(TAG, "Insert channel failed");
    //        return 0;
    //    }
    //
    //    long channelId = ContentUris.parseId(channelUri);
    //
    //    playlist.setChannelPublishedId(channelId);
    //
    //    writeChannelLogo(context, channelId, playlist.getLogoResId());
    //
    //    addClipsToChannel(context, channelId, playlist.getClips());
    //
    //    return channelId;
    //}

    //@WorkerThread
    //static void updateProgramsClips(Context context, List<Clip> wantClipsProgramsUpdate) {
    //    for (Clip clip : wantClipsProgramsUpdate) {
    //        SampleTvProvider.updateProgramClip(context, clip);
    //    }
    //}
    //
    //@WorkerThread
    //static void updateProgramClipSimple(Context context, Clip clip) {
    //    long programId = clip.getProgramId();
    //
    //    if (programId == -1) {
    //        Log.e(TAG, "Oops. Clip not published yet. Exiting...");
    //        return;
    //    }
    //
    //    Uri programUri = TvContractCompat.buildPreviewProgramUri(programId);
    //
    //    Log.d(TAG, "Updating clip " + programUri);
    //
    //    PreviewProgram.Builder builder = createProgramBuilder(context, clip);
    //
    //    int rowsUpdated = context.getContentResolver().update(programUri, builder.build().toContentValues(), null, null);
    //
    //    if (rowsUpdated < 1) {
    //        Log.e(TAG, "Update program failed");
    //    } else {
    //        Log.d(TAG, "Program clip updated " + clip.getTitle());
    //    }
    //}

    //@WorkerThread
    //static void updateProgramClip(Context context, Clip clip) {
    //    long programId = clip.getProgramId();
    //
    //    if (programId == -1) {
    //        Log.e(TAG, "Oops. Clip not published yet. Exiting...");
    //        return;
    //    }
    //
    //    Uri programUri = TvContractCompat.buildPreviewProgramUri(programId);
    //
    //    Log.d(TAG, "Updating clip " + programUri);
    //
    //    try (Cursor cursor = context.getContentResolver().query(programUri, null, null, null, null)) {
    //
    //        int rowsUpdated = 0;
    //
    //        if (cursor != null) {
    //            if (!cursor.moveToFirst()) {
    //                Log.e(TAG, "Update program failed");
    //            }
    //
    //            PreviewProgram program = PreviewProgram.fromCursor(cursor);
    //
    //            PreviewProgram.Builder builder = createProgramBuilder(new PreviewProgram.Builder(program), context, clip);
    //
    //            rowsUpdated = context.getContentResolver().update(programUri, builder.build().toContentValues(), null, null);
    //        }
    //
    //        if (rowsUpdated < 1) {
    //            Log.e(TAG, "Update program failed");
    //        } else {
    //            Log.d(TAG, "Program clip updated " + clip.getTitle());
    //        }
    //    } catch (Exception ex) {
    //        Log.e(TAG, ex.getMessage());
    //        ex.printStackTrace();
    //    }
    //}
}
