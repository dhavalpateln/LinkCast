package com.dhavalpateln.linkcast.explorer.tasks;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.anilist.AnilistDB;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.explorer.listeners.AlMalMetaDataListener;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;

import java.util.List;

public class ExtractMALMetaData extends RunnableTask {

    private LinkWithAllData animeData;
    private AlMalMetaDataListener listener;
    private boolean uiCallback;

    public ExtractMALMetaData(LinkWithAllData animeData, AlMalMetaDataListener listener) {
        this(animeData, listener, true);
    }

    public ExtractMALMetaData(LinkWithAllData animeData, AlMalMetaDataListener listener, boolean uiCallback) {
        super();
        this.animeData = animeData;
        this.listener = listener;
        this.uiCallback = uiCallback;
    }

    private String getMalID() {
        if(this.animeData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID) != null) {
            return this.animeData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID);
        }
        String formattedTitle = this.animeData.getTitle().trim();
        List<MyAnimelistAnimeData> myAnimelistSearchResult = MyAnimelistSearch.search(formattedTitle, this.animeData.isAnime());
        for (MyAnimelistAnimeData myAnimelistAnimeData : myAnimelistSearchResult) {
            if (myAnimelistAnimeData.getTitle().equalsIgnoreCase(formattedTitle)) {
                //this.animeData.updateData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID, String.valueOf(myAnimelistAnimeData.getId()), true, this.animeData.isAnime());
                return String.valueOf(myAnimelistAnimeData.getId());
            }
        }
        if(myAnimelistSearchResult.size() > 1) {
            return String.valueOf(myAnimelistSearchResult.get(0).getId());
            /*if (myAnimelistSearchResult.get(0).getSearchScore() - myAnimelistSearchResult.get(1).getSearchScore() > 10) {
                //this.animeData.updateData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID, String.valueOf(myAnimelistSearchResult.get(0).getId()), true, this.animeData.isAnime());
                return String.valueOf(myAnimelistSearchResult.get(0).getId());
            }*/
        }
        return null;
    }

    @Override
    public void run() {
        String malId = getMalID();
        if(malId != null) {
            if(!malId.equals(this.animeData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID))) {
                this.animeData.updateData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID, malId);
            }

            AlMalMetaData metaData = AnilistDB.getInstance().getAlMalMetaData(malId, this.animeData.isAnime());
            String bannerImage = metaData.getImageURL();
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
