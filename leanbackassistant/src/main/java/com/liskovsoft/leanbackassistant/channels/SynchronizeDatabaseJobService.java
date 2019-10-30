package com.liskovsoft.leanbackassistant.channels;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;

import java.util.concurrent.TimeUnit;

/**
 * JobScheduler task to synchronize the TV provider database with the desired list of channels and
 * programs. This sample app runs this once at install time to publish an initial set of channels
 * and programs, however in a real-world setting this might be run at other times to synchronize
 * a server's database with the TV provider database.
 * This code will ensure that the channels from "SampleClipApi.getDesiredPublishedChannelSet()"
 * appear in the TV provider database, and that these and all other programs are synchronized with
 * TV provider database.
 */

@TargetApi(21)
public class SynchronizeDatabaseJobService extends JobService {
    private SynchronizeDatabaseTask mSynchronizeDatabaseTask;
    private static final String TAG = SynchronizeDatabaseJobService.class.getSimpleName();
    private static boolean sInProgress;

    static void schedule(Context context) {
        if (VERSION.SDK_INT >= 23 && !sInProgress) {
            JobScheduler scheduler = context.getSystemService(JobScheduler.class);
            scheduler.schedule(
                    new JobInfo.Builder(0, new ComponentName(context, SynchronizeDatabaseJobService.class))
                            .setPeriodic(TimeUnit.MINUTES.toMillis(30))
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setRequiresDeviceIdle(false)
                            .setRequiresCharging(false)
                            .build());
            sInProgress = true;
        }
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mSynchronizeDatabaseTask = new SynchronizeDatabaseTask(this, jobParameters);
        // NOTE: fetching channels in background
        mSynchronizeDatabaseTask.execute();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mSynchronizeDatabaseTask != null) {
            mSynchronizeDatabaseTask.cancel(true);
            mSynchronizeDatabaseTask = null;
        }

        return true;
    }

    /**
     * Publish any default channels not already published.
     */
    private class SynchronizeDatabaseTask extends AsyncTask<Void, Void, Void> {
        private final GlobalPreferences mPrefs;
        private Context mContext;
        private JobParameters mJobParameters;

        SynchronizeDatabaseTask(Context context, JobParameters jobParameters) {
            mContext = context;
            mJobParameters = jobParameters;
            mPrefs = GlobalPreferences.instance(mContext);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "Syncing channels...");

            updateOrPublish(MySampleClipApi.getSubscriptionsPlaylist(mContext));
            updateOrPublish(MySampleClipApi.getRecommendedPlaylist(mContext));
            updateOrPublish(MySampleClipApi.getHistoryPlaylist(mContext));

            return null;
        }

        private void updateOrPublish(Playlist playlist) {
            if (playlist != null) {
                String channelKey = playlist.getChannelKey();
                String programsKey = playlist.getProgramsKey();

                if (channelKey != null && programsKey != null) {
                    if (getChannelId(channelKey) == -1 || getProgramsIds(programsKey) == null) {
                        Log.d(TAG, "Add channel: " + playlist.getName());
                        SampleTvProvider.addChannel(mContext, playlist);
                        setChannelId(channelKey, playlist.getChannelId());
                        setProgramsIds(programsKey, playlist.getPublishedClipsIds());
                    } else {
                        Log.d(TAG, "Updating " + playlist.getName() + "...");

                        playlist.setChannelPublishedId(getChannelId(channelKey));
                        playlist.restoreClipsIds(getProgramsIds(programsKey));

                        SampleTvProvider.updateChannel(mContext, playlist);
                        SampleTvProvider.updateProgramsClips(mContext, playlist.getClips()); // update clips that have program_id
                        SampleTvProvider.addClipsToChannel(mContext, playlist.getChannelId(), playlist.getClips()); // add more clips

                        // all clips now published
                        setProgramsIds(programsKey, playlist.getPublishedClipsIds());
                    }
                }
            }
        }

        private void setProgramsIds(String programsKey, String clipsIds) {
            mPrefs.putString(programsKey, clipsIds);
        }

        private void setChannelId(String programsKey, long channelId) {
            mPrefs.putLong(programsKey, channelId);
        }

        private String getProgramsIds(String programsKey) {
            return mPrefs.getString(programsKey, null);
        }

        private long getChannelId(String channelKey) {
            return mPrefs.getLong(channelKey, -1);
        }

        @Override
        protected void onPostExecute(Void result) {
            sInProgress = false;
            mSynchronizeDatabaseTask = null;
            jobFinished(mJobParameters, false);
        }
    }
}
