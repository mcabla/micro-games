package com.mcabla.microbit.game.scripts.Room;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.ExecutionException;

public class GameAsyncTask {
    private AppDatabase appDatabase;

    public GameAsyncTask(Context c){
        this.appDatabase = AppDatabase.getDatabase(c);
    }

    public int getRandomId() throws ExecutionException, InterruptedException {
        return new getRandomIdAsyncTask(appDatabase).execute().get();
    }

    private static class getRandomIdAsyncTask extends AsyncTask<Void, Void, Integer>{
        private AppDatabase db;

        getRandomIdAsyncTask(AppDatabase appDatabase){
            db = appDatabase;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return db.gameModel().getRandomId();
        }

        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
        }
    }


    public GameModel getGame(int id) throws ExecutionException, InterruptedException {
        return new getGameAsyncTask(appDatabase).execute(id).get();
    }

    private static class getGameAsyncTask extends AsyncTask<Integer, Void, GameModel>{
        private AppDatabase db;

        getGameAsyncTask(AppDatabase appDatabase){
            db = appDatabase;
        }

        @Override
        protected GameModel doInBackground(final Integer... params) {
            return db.gameModel().getGame(params[0]);
        }

        @Override
        protected void onPostExecute(GameModel result){
            super.onPostExecute(result);
        }
    }


    public void addGame(GameModel gameModel) {
        new addGameAsyncTask(appDatabase).execute(gameModel);
    }

    private static class addGameAsyncTask extends AsyncTask<GameModel, Void, Void> {
        private AppDatabase db;

        addGameAsyncTask(AppDatabase appDatabase){
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final GameModel... params) {
            db.gameModel().addGame(params[0]);
            Log.d("micro:games", "Saved game data in database: " + params[0].getFilename());
            return null;
        }

    }


    public void deleteAllGames() {
        new deleteAllGamesAsyncTask(appDatabase).execute();
    }

    private static class deleteAllGamesAsyncTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase db;

        deleteAllGamesAsyncTask(AppDatabase appDatabase){
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(Void... params) {
            db.gameModel().deleteAllGames();
            return null;
        }

    }

}
