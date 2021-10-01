package com.dhavalpateln.linkcast.animesources;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;

import java.net.URLEncoder;

public class FourAnime extends AnimeSource {

    public FourAnime() {
        this.animeSource = "4anime.org";
    }

    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        String title = currentURL.split("4animes.org/")[1];
        if (includeEpisode) {
            title = title.split("\\?")[0];
        } else {
            if (title.contains("anime/")) {
                title = title.split("/")[1];
            } else if (title.contains("episode") && !includeEpisode) {
                title = title.split("-episode")[0];
            } else {
                title = searchTerm;
            }
        }
        return title;
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return "https://4animes.org/?s=" + URLEncoder.encode(searchTerm);
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.contains("4anime.org");
    }

    @Override
    public void updateBookmarkPage(String url, String id, String title) {
        if (url.contains("episode")) {
            String episodeNum = url.split("\\?")[0].split("episode-")[1];
            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                    .child(id)
                    .child("title")
                    .setValue(title.split(" - EP")[0] + " - EP" + episodeNum);
        }
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        if(!urlString.contains("4animes.org")) return true;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }
}
