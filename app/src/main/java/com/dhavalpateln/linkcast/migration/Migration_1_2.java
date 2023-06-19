package com.dhavalpateln.linkcast.migration;

import android.content.Context;
import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.database.anilist.AnilistDB;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkDataContract;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.extractors.Extractor;
import com.dhavalpateln.linkcast.extractors.Providers;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Migration_1_2 extends MigrationTask {

    private LinkCastRoomRepository roomRepo;
    private Map<String, Extractor> extractorMap;
    private String TAG = "MIGRATION_1_2";

    public Migration_1_2(Context context, MigrationListener listener) {
        super(2, listener);
        this.roomRepo = new LinkCastRoomRepository(context);
        extractorMap = Providers.getExtractors();
    }

    private String getMalID(LinkWithAllData linkData) {
        if(linkData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID) != null) {
            return linkData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID);
        }
        String formattedTitle = linkData.getTitle().trim();
        List<MyAnimelistAnimeData> myAnimelistSearchResult = MyAnimelistSearch.search(formattedTitle, linkData.isAnime());
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

    private Extractor getExtractor(LinkWithAllData linkWithAllData) {
        String source = linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(extractorMap.containsKey(source)) {
            return extractorMap.get(source);
        }
        else {
            for (String extractorName : extractorMap.keySet()) {
                if (extractorMap.get(extractorName).isCorrectURL(linkWithAllData.linkData.getUrl())) {
                    return extractorMap.get(extractorName);
                }
            }
        }
        return null;
    }

    private void sync(LinkWithAllData linkData) {
        if(linkData.getMetaData(LinkDataContract.DATA_MYANIMELIST_ID) == null) {
            linkData.updateData(LinkDataContract.DATA_MYANIMELIST_ID, getMalID(linkData));
        }

        Extractor extractor = getExtractor(linkData);

        if(extractor != null) {
            AnimeLinkData animeData = AnimeLinkData.from(linkData.linkData);
            List<EpisodeNode> episodeNodes = extractor.extractData(animeData);
            Collections.sort(episodeNodes, (node1, node2) -> (int) (node2.getEpisodeNum() - node1.getEpisodeNum()));
            String availableEpisodes = episodeNodes.get(0).getEpisodeNumString();
            linkData.linkMetaData.setLastEpisodeNodesFetchCount((int) Double.parseDouble(availableEpisodes));
        }

        if(linkData.getMetaData(LinkDataContract.DATA_MYANIMELIST_ID) != null) {
            linkData.alMalMetaData = AnilistDB.getInstance().getAlMalMetaData(
                    linkData.getMetaData(LinkDataContract.DATA_MYANIMELIST_ID),
                    linkData.isAnime()
            );
        }

        roomRepo.insert(linkData.linkMetaData);
        roomRepo.insert(linkData.alMalMetaData);
    }

    @Override
    public void execute() {
        List<LinkWithAllData> linkWithAllData = this.roomRepo.getLinkWithData();
        List<Future> futures = new ArrayList<>();
        ExecutorService es = Executors.newFixedThreadPool(10);
        for(LinkWithAllData link: linkWithAllData) {
            futures.add(es.submit(() -> sync(link)));
        }
        for(int i = 0; i < futures.size(); i++) {
            try {
                futures.get(i).get();
            } catch (Exception e) {
                Log.d(TAG, "Migration exception");
            }
            updateProgress((i + 1) * 100 / futures.size());
        }
        notifySuccess();
    }
}
