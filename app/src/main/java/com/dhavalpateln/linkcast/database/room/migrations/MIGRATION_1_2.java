package com.dhavalpateln.linkcast.database.room.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class MIGRATION_1_2 extends Migration {

    public MIGRATION_1_2() {
        super(1, 2);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE LinkData "
                + " ADD COLUMN type TEXT");
    }
}
