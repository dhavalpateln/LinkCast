package com.dhavalpateln.linkcast.explorer.tasks;

import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.database.JikanDatabase;
import com.dhavalpateln.linkcast.database.kitsu.KitsuDB;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.explorer.listeners.EpisodeNodeListListener;
import com.dhavalpateln.linkcast.extractors.Extractor;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExtractEpisodeNodes extends RunnableTask {
    private Extractor extractor;
    private EpisodeNodeListListener listener;
    private LinkWithAllData linkData;
    private String TAG = "ExtractAnimeEpisodes";

    public ExtractEpisodeNodes(Extractor extractor, LinkWithAllData linkData, EpisodeNodeListListener listener) {
        super();
        this.extractor = extractor;
        this.listener = listener;
        this.linkData = linkData;
    }

    private String getMalID() {
        if(this.linkData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID) != null) {
            return this.linkData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID);
        }
        String formattedTitle = this.linkData.getTitle().trim();
        List<MyAnimelistAnimeData> myAnimelistSearchResult = MyAnimelistSearch.search(formattedTitle, this.linkData.isAnime());
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

    private class ExtractEpisodes implements Callable<List<EpisodeNode>> {

        @Override
        public List<EpisodeNode> call() {
            if(extractor.requiresInit()) {
                extractor.init();
            }
            AnimeLinkData animeData = AnimeLinkData.from(linkData.linkData);
            List<EpisodeNode> episodes = extractor.extractData(animeData);
            return episodes;
        }
    }

    @Override
    public void run() {
        try {

            ExecutorService es = Executors.newFixedThreadPool(3);
            List<Future> metaFuturesList = new ArrayList<>();
            Future<List<EpisodeNode>> episodeFuture = es.submit(new ExtractEpisodes());

            Future<Map<String, EpisodeNode>> malAnimeMetaFuture = null;
            Map<String, EpisodeNode> malAnimeMeta = null;

            /*if(this.extractor.requiresInit()) {
                this.extractor.init();
            }
            AnimeLinkData animeData = AnimeLinkData.from(linkData.linkData);
            List<EpisodeNode> episodes = this.extractor.extractData(animeData);*/

            String malId = getMalID();

            if(malId != null) {

                metaFuturesList.add(es.submit(() -> KitsuDB.getInstance().fetchData(
                        malId,
                        this.linkData.isAnime()
                )));
                /*KitsuDB.getInstance().fetchData(
                        this.linkData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID),
                        this.linkData.isAnime()
                );
                KitsuDB.getInstance().updateEpisodeNodes(
                        episodes,
                        this.linkData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID),
                        this.linkData.isAnime()
                );*/

                if(this.linkData.isAnime()) {
                    malAnimeMetaFuture = es.submit(() -> JikanDatabase.getInstance().getAllEpisodeData(malId));
                    metaFuturesList.add(malAnimeMetaFuture);
                }
            }

            List<EpisodeNode> episodeNodes = episodeFuture.get();

            for(Future future: metaFuturesList) future.get();

            if(this.linkData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID) != null) {
                KitsuDB.getInstance().updateEpisodeNodes(
                        episodeNodes,
                        this.linkData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID),
                        this.linkData.isAnime()
                );
                if(malAnimeMetaFuture != null) {
                    malAnimeMeta = malAnimeMetaFuture.get();
                }
            }

            for(EpisodeNode node: episodeNodes) {
                if(node.getThumbnail() == null) node.setThumbnail(this.linkData.getMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL));
                if(malAnimeMeta != null && malAnimeMeta.containsKey(node.getEpisodeNumString())) {
                    EpisodeNode malEpisodeNode = malAnimeMeta.get(node.getEpisodeNumString());
                    if(node.getTitle() == null) node.setTitle(malEpisodeNode.getTitle());
                    node.setFiller(malEpisodeNode.isFiller());
                }
            }

            this.getUIHandler().post(() -> this.listener.onEpisodeNodesFetched(episodeNodes));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception while running extractor: " + this.extractor.getDisplayName());
        }
    }
}
