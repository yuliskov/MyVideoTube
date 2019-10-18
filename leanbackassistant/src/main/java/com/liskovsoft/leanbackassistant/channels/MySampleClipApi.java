package com.liskovsoft.leanbackassistant.channels;

import androidx.tvprovider.media.tv.TvContractCompat;
import com.liskovsoft.myvideotubeapi.Video;
import com.liskovsoft.youtubeapi.adapters.YouTubeVideoService;

import java.util.ArrayList;
import java.util.List;

public class MySampleClipApi {
    public static final int SUBSCRIPTIONS_ID = 1;

    public static List<Playlist> getDesiredPublishedChannelSet() {
        YouTubeVideoService service = new YouTubeVideoService();
        List<Video> subscriptions = service.getSubscriptions();

        List<Playlist> playlists = null;

        if (subscriptions != null) {
            playlists = new ArrayList<>();
            playlists.add(
                    new Playlist("Subscriptions", convertToClips(subscriptions), Integer.toString(SUBSCRIPTIONS_ID)));
        }

        return playlists;
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
}
