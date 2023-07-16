package com.dhavalpateln.linkcast.database.room.almaldata;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlMalMetaDataDao {
    @Query("SELECT * FROM AlMalMetaData")
    List<AlMalMetaData> getAll();

    @Query("SELECT * FROM AlMalMetaData WHERE id=:first LIMIT 1")
    AlMalMetaData findByID(String first);

    @Update
    void updateMALMetaData(AlMalMetaData... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(AlMalMetaData... users);

    @Delete
    void delete(AlMalMetaData user);
}
