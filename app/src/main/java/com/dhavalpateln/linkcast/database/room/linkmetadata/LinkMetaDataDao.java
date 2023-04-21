package com.dhavalpateln.linkcast.database.room.linkmetadata;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LinkMetaDataDao {

    @Query("SELECT * FROM LinkMetaData")
    List<LinkMetaData> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(LinkMetaData... linkData);

    @Delete
    void delete(LinkMetaData linkData);

    @Query("DELETE FROM LinkMetaData WHERE id=:id")
    void delete(String id);

    @Query("DELETE FROM LinkMetaData")
    void deleteAll();

    @Query("SELECT * FROM LinkMetaData WHERE id=:id")
    LinkMetaData getLinkMetaData(String id);
}
