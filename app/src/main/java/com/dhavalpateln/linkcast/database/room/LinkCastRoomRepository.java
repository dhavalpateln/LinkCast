package com.dhavalpateln.linkcast.database.room;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkDataDao;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaDataDao;
import com.dhavalpateln.linkcast.database.room.maldata.MALMetaData;
import com.dhavalpateln.linkcast.database.room.maldata.MALMetaDataDao;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class LinkCastRoomRepository {

    private LinkDataDao linkDataDao;
    private MALMetaDataDao malMetaDataDao;
    private LinkMetaDataDao linkMetaDataDao;

    public LinkCastRoomRepository(Context application) {
        LinkCastRoomDatabase db = LinkCastRoomDatabase.getDatabase(application);
        linkDataDao = db.linkDataDao();
        malMetaDataDao = db.malMetaDataDao();
        linkMetaDataDao = db.linkMetaDataDao();
    }

    public void insert(LinkData linkData) {
        LinkCastRoomDatabase.databaseWriteExecutor.execute(() -> linkDataDao.insertAll(linkData));
    }
    public void insert(MALMetaData malMetaData) {
        LinkCastRoomDatabase.databaseWriteExecutor.execute(() -> malMetaDataDao.insertAll(malMetaData));
    }
    public void insert(LinkMetaData linkMetaData) {
        LinkCastRoomDatabase.databaseWriteExecutor.execute(() -> linkMetaDataDao.insertAll(linkMetaData));
    }

    public void deleteLinkData(String id) {
        LinkCastRoomDatabase.databaseWriteExecutor.execute(() -> linkDataDao.delete(id));
    }

    public void deleteAnimeLinkData() {
        linkDataDao.deleteAnimeLinks();
    }

    public LiveData<List<LinkWithAllData>> getAnimeLinks() { return linkDataDao.getAnimeLinks(); }
    public LiveData<List<LinkWithAllData>> getMangaLinks() { return linkDataDao.getMangaLinks(); }
    public List<LinkData> getAllAnimeLinks() { return linkDataDao.getAll(); }

    public MALMetaData getMalMetaData(String id) {
        return this.malMetaDataDao.findByID(id);
    }

    public void clearLinkData() {
        LinkCastRoomDatabase.databaseWriteExecutor.execute(() -> linkDataDao.deleteAll());
    }

    public List<LinkWithAllData> getLinkWithData() {
        return linkDataDao.getAllData();
    }

    public LinkMetaData createDefaultLinkMetaData(String id) {
        LinkMetaData linkMetaData = new LinkMetaData();
        linkMetaData.setId(id);
        linkMetaData.setLastEpisodeNodesFetchCount(-2);
        linkMetaData.setMiscData(new HashMap<>());
        return linkMetaData;
    }

    public void updateLastFetchedEpisode(LinkWithAllData linkWithAllData, int episodes) {
        if(linkWithAllData.getId() != null) {
            if(linkWithAllData.linkMetaData == null) {
                linkWithAllData.linkMetaData = createDefaultLinkMetaData(linkWithAllData.getId());
            }
            linkWithAllData.linkMetaData.setLastEpisodeNodesFetchCount(episodes);
            insert(linkWithAllData.linkMetaData);
        }
    }

    public List<String> getAllAnimeIDs() {
        return this.linkDataDao.getAllAnimeIDs();
    }

    public List<String> getAllMangaIDs() {
        return this.linkDataDao.getAllMangaIDs();
    }
}
