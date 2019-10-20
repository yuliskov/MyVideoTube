package com.liskovsoft.leanbackassistant.channels;

import android.content.Context;
import androidx.tvprovider.media.tv.TvContractCompat;
import com.liskovsoft.leanbackassistant.R;
import com.liskovsoft.leanbackassistant.channels.SampleClipApi.GetClipByIdListener;
import com.liskovsoft.myvideotubeapi.Video;
import com.liskovsoft.youtubeapi.adapters.YouTubeVideoService;

import java.util.ArrayList;
import java.util.List;

public class MySampleClipApi {
    private static List<Clip> sCachedVideos;
    private static final int SUBSCRIPTIONS_ID = 1;
    private static final int HISTORY_ID = 2;
    private static final int RECOMMENDED_ID = 3;
    private static final String SUBS_CHANNEL_ID = "subs_channel_id";
    private static final String SUBS_PROGRAMS_IDS = "subs_clips_ids";
    private static final String RECOMMENDED_CHANNEL_ID = "recommended_channel_id";
    private static final String RECOMMENDED_PROGRAMS_IDS = "recommended_programs_ids";
    private static final String HISTORY_CHANNEL_ID = "history_channel_id";
    private static final String HISTORY_PROGRAMS_IDS = "history_programs_ids";

    public static Playlist getSubscriptionsPlaylist(Context context) {
        YouTubeVideoService service = YouTubeVideoService.instance();
        List<Video> subscriptions = service.getSubscriptions();

        Playlist playlist = null;

        if (subscriptions != null) {
            if (subscriptions.size() < 20) {
                subscriptions.addAll(service.getNextSubscriptions());
                subscriptions.addAll(service.getNextSubscriptions());
                subscriptions.addAll(service.getNextSubscriptions());
            }

            List<Clip> clips = convertToClips(subscriptions);
            playlist = new Playlist(context.getResources().getString(R.string.subscriptions_playlist_name), clips, Integer.toString(SUBSCRIPTIONS_ID));
            playlist.setChannelKey(SUBS_CHANNEL_ID);
            playlist.setProgramsKey(SUBS_PROGRAMS_IDS);
        }

        return playlist;
    }

    public static Playlist getHistoryPlaylist(Context context) {
        YouTubeVideoService service = YouTubeVideoService.instance();
        List<Video> history = service.getHistory();

        Playlist playlist = null;

        if (history != null) {
            if (history.size() < 20) {
                history.addAll(service.getNextHistory());
                history.addAll(service.getNextHistory());
                history.addAll(service.getNextHistory());
            }

            List<Clip> clips = convertToClips(history);
            playlist = new Playlist(context.getResources().getString(R.string.history_playlist_name), clips, Integer.toString(HISTORY_ID));
            playlist.setChannelKey(HISTORY_CHANNEL_ID);
            playlist.setProgramsKey(HISTORY_PROGRAMS_IDS);
        }

        return playlist;
    }

    public static Playlist getRecommendedPlaylist(Context context) {
        YouTubeVideoService service = YouTubeVideoService.instance();
        List<Video> recommended = service.getRecommended();

        Playlist playlist = null;

        if (recommended != null) {
            if (recommended.size() < 20) {
                recommended.addAll(service.getNextRecommended());
                recommended.addAll(service.getNextRecommended());
                recommended.addAll(service.getNextRecommended());
            }

            List<Clip> clips = convertToClips(recommended);
            playlist = new Playlist(context.getResources().getString(R.string.recommended_playlist_name), clips, Integer.toString(RECOMMENDED_ID));
            playlist.setChannelKey(RECOMMENDED_CHANNEL_ID);
            playlist.setProgramsKey(RECOMMENDED_PROGRAMS_IDS);
        }

        return playlist;
    }

    private static List<Clip> convertToClips(List<Video> videos) {
        if (videos != null) {
            List<Clip> clips = new ArrayList<>();

            for (Video v : videos) {
                clips.add(new Clip(
                        v.getTitle(),
                        v.getDescription(),
                        v.getBackgroundImageUrl(),
                        v.getCardImageUrl(),
                        v.getVideoUrl(),
                        null,
                        false,
                        null,
                        Integer.toString(v.getId()),
                        null,
                        TvContractCompat.PreviewProgramColumns.ASPECT_RATIO_16_9));
            }

            return clips;
        }

        return null;
    }

    public static void getClipById(String clipId, GetClipByIdListener listener) {
        Clip clip = null;

        if (sCachedVideos != null) {
            for (Clip v : sCachedVideos) {
                if (clipId.equals(v.getClipId())) {
                    clip = v;
                    break;
                }
            }
        }

        listener.onGetClipById(clip);
    }
}
