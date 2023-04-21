package com.dhavalpateln.linkcast.database.room.maldata;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MALMetaDataDao {
    @Query("SELECT * FROM MALMetaData")
    List<MALMetaData> getAll();

    @Query("SELECT * FROM MALMetaData WHERE id=:first LIMIT 1")
    MALMetaData findByID(String first);

    @Update
    void updateMALMetaData(MALMetaData... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MALMetaData... users);

    @Delete
    void delete(MALMetaData user);
}
