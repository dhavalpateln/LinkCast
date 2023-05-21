package com.dhavalpateln.linkcast.database.room;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaDataDao;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkDataDao;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaDataDao;

import java.util.HashMap;
import java.util.List;

public class LinkCastRoomRepository {

    private LinkDataDao linkDataDao;
    private AlMalMetaDataDao alMalMetaDataDao;
    private LinkMetaDataDao linkMetaDataDao;

    public LinkCastRoomRepository(Context application) {
        LinkCastRoomDatabase db = LinkCastRoomDatabase.getDatabase(application);
        linkDataDao = db.linkDataDao();
        alMalMetaDataDao = db.malMetaDataDao();
        linkMetaDataDao = db.linkMetaDataDao();
    }

    public void insert(LinkData linkData) {
        LinkCastRoomDatabase.databaseWriteExecutor.execute(() -> linkDataDao.insertAll(linkData));
    }
    public void insert(AlMalMetaData alMalMetaData) {
        LinkCastRoomDatabase.databaseWriteExecutor.execute(() -> alMalMetaDataDao.insertAll(alMalMetaData));
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

    public AlMalMetaData getMalMetaData(String id) {
        return this.alMalMetaDataDao.findByID(id);
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
