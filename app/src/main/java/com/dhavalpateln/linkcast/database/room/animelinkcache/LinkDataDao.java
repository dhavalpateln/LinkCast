package com.dhavalpateln.linkcast.database.room.animelinkcache;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface LinkDataDao {

    @Query("SELECT * FROM LinkData")
    List<LinkData> getAll();

    @Transaction
    @Query("SELECT * FROM LinkData")
    List<LinkWithAllData> getAllData();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(LinkData... linkData);

    @Delete
    void delete(LinkData linkData);

    @Query("DELETE FROM LinkData WHERE id=:id")
    void delete(String id);

    @Query("DELETE FROM LinkData")
    void deleteAll();

    @Transaction
    @Query("SELECT * FROM LinkData WHERE type='anime'")
    LiveData<List<LinkWithAllData>> getAnimeLinks();

    @Transaction
    @Query("SELECT * FROM LinkData WHERE type='manga'")
    LiveData<List<LinkWithAllData>> getMangaLinks();

    @Query("SELECT * FROM LinkData WHERE type='anime'")
    List<LinkData> getAllAnimeLinks();

}
