package com.dhavalpateln.linkcast.migration;

public interface MigrationListener {
    void onMigrationProgressChange(int progress);
    void onMigrationSuccess(int version);
    void onMigrationFail();
}
