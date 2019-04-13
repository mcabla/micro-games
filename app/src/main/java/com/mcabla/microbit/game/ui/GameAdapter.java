package com.mcabla.microbit.game.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mcabla.microbit.game.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class GameAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] names;
    private Integer[] scores;
    private Boolean showGames;
    private GameActivity gameActivity;


    public GameAdapter(ArrayList<String> names, ArrayList<Integer> scoresList, Boolean showGames, GameActivity gameActivity) {
        this.names = names.toArray(new String[0]);
        this.scores = new Integer[scoresList.size()];
        for(int i = 0; i < scoresList.size(); i++)
            scores[i] = Integer.parseInt(String.valueOf(scoresList.get(i)));
        this.showGames = showGames;
        this.gameActivity = gameActivity;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.list_player, parent, false);
        return new playerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        playerViewHolder playerViewHolder = (playerViewHolder) holder;

        playerViewHolder.name.setText(names[position]);
        int i = scores[position];
        if (i == -1 | showGames) playerViewHolder.score.setText(" ");
        else playerViewHolder.score.setText(String.valueOf(i));
        if(showGames){
            playerViewHolder.itemView.setOnClickListener(new gameOnClickListener(gameActivity, i));
        }
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    static class playerViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout constraintLayout;
        AppCompatTextView name;
        AppCompatTextView score;


        playerViewHolder(View itemView) {
            super (itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            name = itemView.findViewById (R.id.name);
            score = itemView.findViewById (R.id.score);

        }

    }

    private class gameOnClickListener implements View.OnClickListener {
        private GameActivity gameActivity;
        private int game;
        private gameOnClickListener(GameActivity gameActivity, int game){
            this.gameActivity = gameActivity;
            this.game = game;

        }
        @Override
        public void onClick(View v) {
            gameActivity.startScript(game);
        }
    }
}
