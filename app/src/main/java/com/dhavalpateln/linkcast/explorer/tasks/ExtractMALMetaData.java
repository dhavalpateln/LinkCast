package com.dhavalpateln.linkcast.explorer.tasks;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeMALMetaData;
import com.dhavalpateln.linkcast.database.JikanDatabase;
import com.dhavalpateln.linkcast.database.anilist.AnilistDB;
import com.dhavalpateln.linkcast.explorer.listeners.MALMetaDataListener;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;

import java.util.List;

public class ExtractMALMetaData extends RunnableTask {

    private AnimeLinkData animeData;
    private MALMetaDataListener listener;
    private boolean uiCallback;

    public ExtractMALMetaData(AnimeLinkData animeData, MALMetaDataListener listener) {
        this(animeData, listener, true);
    }

    public ExtractMALMetaData(AnimeLinkData animeData, MALMetaDataListener listener, boolean uiCallback) {
        super();
        this.animeData = animeData;
        this.listener = listener;
        this.uiCallback = uiCallback;
    }

    private String getMalID() {
        if(this.animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID) != null) {
            return this.animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID);
        }
        if(this.animeData.getMalMetaData() != null) {
            return this.animeData.getMalMetaData().getId();
        }
        String formattedTitle = this.animeData.getTitle().replaceAll("\\(.*\\)", "").trim();
        List<MyAnimelistAnimeData> myAnimelistSearchResult = MyAnimelistSearch.search(formattedTitle, this.animeData.isAnime());
        for (MyAnimelistAnimeData myAnimelistAnimeData : myAnimelistSearchResult) {
            if (myAnimelistAnimeData.getTitle().equalsIgnoreCase(formattedTitle)) {
                //this.animeData.updateData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID, String.valueOf(myAnimelistAnimeData.getId()), true, this.animeData.isAnime());
                return String.valueOf(myAnimelistAnimeData.getId());
            }
        }
        if(myAnimelistSearchResult.size() > 1) {
            if (myAnimelistSearchResult.get(0).getSearchScore() - myAnimelistSearchResult.get(1).getSearchScore() > 10) {
                //this.animeData.updateData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID, String.valueOf(myAnimelistSearchResult.get(0).getId()), true, this.animeData.isAnime());
                return String.valueOf(myAnimelistSearchResult.get(0).getId());
            }
        }
        return null;
    }

    @Override
    public void run() {
        String malId = getMalID();
        if(malId != null) {
            if(!malId.equals(this.animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID))) {
                this.animeData.updateData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID, malId, true, this.animeData.isAnime());
            }
            AnimeMALMetaData metaData = JikanDatabase.getInstance().getMalMetaData(malId, this.animeData.isAnime());
            String bannerImage = AnilistDB.getInstance().getBannerImage(malId, this.animeData.isAnime());
            if(this.getUIHandler() != null) {
                this.getUIHandler().post(() -> {
                    this.listener.onMALMetaData(metaData);
                    if(bannerImage != null) this.listener.onBannerImageFetched(bannerImage);
                });
            }
            else {
                this.listener.onMALMetaData(metaData);
            }
        }
    }

}
