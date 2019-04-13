package com.mcabla.microbit.game.scripts.Room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;


@Dao
public interface GameDao {

    @Query("select id from GameModel order by RANDOM() limit 1")
    Integer getRandomId();

    @Query("select * from GameModel where id = :id limit 1")
    GameModel getGame(int id);

    @Query("select name from GameModel order by id")
    List<String> getGameTitles();

    @Query("select id from GameModel order by id")
    List<Integer> getGameIds();

    @Insert(onConflict = REPLACE)
    void addGame(GameModel game);

    @Query("DELETE FROM GameModel")
    void deleteAllGames();

}