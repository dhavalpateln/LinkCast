package com.dhavalpateln.linkcast.database.room;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.DeleteColumn;
import androidx.room.RenameTable;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.AutoMigrationSpec;

import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkDataDao;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaDataDao;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaDataDao;
import com.dhavalpateln.linkcast.database.room.migrations.MIGRATION_1_2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {LinkData.class, AlMalMetaData.class, LinkMetaData.class},
        version = 9,
        autoMigrations = {
                @AutoMigration(from = 2, to = 3, spec = LinkCastRoomDatabase.MALMetaDataRenameMigration_2_3.class),
                @AutoMigration(from = 3, to = 4),
                @AutoMigration(from = 4, to = 5),
                @AutoMigration(from = 5, to = 6),
                @AutoMigration(from = 6, to = 7, spec = LinkCastRoomDatabase.LinkDataDeleteEpiMigration_7_8.class),
                @AutoMigration(from = 7, to = 8, spec = LinkCastRoomDatabase.LinkDataDeleteEpiMigration_7_8.class),
                @AutoMigration(from = 8, to = 9, spec = LinkCastRoomDatabase.MALMetaDataRenameMigration_8_9.class),
        }
)
@TypeConverters({Converters.class})
public abstract class LinkCastRoomDatabase extends RoomDatabase {

    @RenameTable(fromTableName = "AnimeMALMetaData", toTableName = "MALMetaData")
    static class MALMetaDataRenameMigration_2_3 implements AutoMigrationSpec { }

    @DeleteColumn(tableName = "LinkData", columnName = "lastEpisodeNodesFetchCount")
    static class LinkDataDeleteEpiMigration_7_8 implements AutoMigrationSpec { }

    @RenameTable(fromTableName = "MALMetaData", toTableName = "AlMalMetaData")
    static class MALMetaDataRenameMigration_8_9 implements AutoMigrationSpec { }


    public abstract LinkDataDao linkDataDao();
    public abstract AlMalMetaDataDao malMetaDataDao();
    public abstract LinkMetaDataDao linkMetaDataDao();

    private static volatile LinkCastRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static LinkCastRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LinkCastRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    LinkCastRoomDatabase.class, "link_cast_room_database")
                            .addMigrations(
                                    new MIGRATION_1_2()
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
