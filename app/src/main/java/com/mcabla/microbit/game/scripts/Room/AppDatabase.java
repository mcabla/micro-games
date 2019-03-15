package com.mcabla.microbit.game.scripts.Room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Created by Casper Haems on 15/06/2018.
 * Copyright (c) 2019 Casper Haems. All rights reserved.
 */
@Database(entities = {GameModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
/*    private static final Migration MIGRATION_1_2 = new Migration (1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Since we didn't alter the table, there's nothing else to do here.
        }
    };*/

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, AppDatabase.class, "app_db")
/*
                            .addMigrations (MIGRATION_1_2)
*/
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public abstract GameDao gameModel();

}