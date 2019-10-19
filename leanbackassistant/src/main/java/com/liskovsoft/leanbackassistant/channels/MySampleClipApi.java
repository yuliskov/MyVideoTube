package com.liskovsoft.leanbackassistant.channels;

import androidx.tvprovider.media.tv.TvContractCompat;
import com.liskovsoft.leanbackassistant.channels.SampleClipApi.GetClipByIdListener;
import com.liskovsoft.myvideotubeapi.Video;
import com.liskovsoft.youtubeapi.adapters.YouTubeVideoService;

import java.util.ArrayList;
import java.util.List;

public class MySampleClipApi {
    public static final int SUBSCRIPTIONS_ID = 1;
    private static List<Clip> sCachedVideos;

    public static Playlist getSubscriptionsPlaylist() {
        YouTubeVideoService service = new YouTubeVideoService();
        List<Video> subscriptions = service.getSubscriptions();

        sCachedVideos = new ArrayList<>();

        Playlist playlist = null;

        if (subscriptions != null) {
            List<Clip> clips = convertToClips(subscriptions);
            playlist = new Playlist("Subscriptions", clips, Integer.toString(SUBSCRIPTIONS_ID));

            sCachedVideos.addAll(clips);
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
                    //listener.onGetClipById(v);
                    clip = v;
                    break;
                }
            }
        }

        listener.onGetClipById(clip);
    }
}
