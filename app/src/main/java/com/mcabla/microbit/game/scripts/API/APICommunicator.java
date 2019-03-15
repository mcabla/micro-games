package com.mcabla.microbit.game.scripts.API;

import android.util.Log;

import com.mcabla.microbit.game.Constants;

import java.io.IOException;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;


/**
 * Created by Casper Haems on 16/12/2017.
 * Copyright (c) 2019 Casper Haems. All rights reserved.
 */

public class APICommunicator {
    private static APICommunicator mInstance = null;
    private Retrofit retrofitScalars;
    private String stringAnswer;

    public APICommunicator (){
        OkHttpClient okHttpClient = CustomOkHttpClient.getInstance ().getOkHttpClient ();

        retrofitScalars = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/")
                .client (okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    public static synchronized APICommunicator getInstance(){
        if(mInstance == null){
            Log.d(Constants.TAG,"Creating APICommunicator");
            mInstance = new APICommunicator ();
        }
        return mInstance;
    }

    public String StringGet(String filename) {
        GithubGet githubGet = retrofitScalars.create(GithubGet.class);

        Call<String> call = githubGet.responses(filename);
        try {
            stringAnswer = call.execute ().body ();
        } catch (IOException e) {
            stringAnswer = "-";
        }
        if ("".equals (stringAnswer)) stringAnswer = "-";
        if (stringAnswer == null) stringAnswer = "-";
        return stringAnswer;
    }

    private interface GithubGet {
        @GET("minecabla/micro-games/master/games/{filename}")
        Call<String> responses(@Path("filename") String filename);
    }
}