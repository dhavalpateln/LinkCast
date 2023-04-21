package com.dhavalpateln.linkcast.database.room;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;

import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkDataDao;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaDataDao;
import com.dhavalpateln.linkcast.database.room.maldata.MALMetaData;
import com.dhavalpateln.linkcast.database.room.maldata.MALMetaDataDao;

import java.util.List;

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
}
