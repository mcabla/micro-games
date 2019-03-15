package com.mcabla.microbit.game.scripts.API;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

import static android.content.ContentValues.TAG;

/**
 * Created by Casper Haems on 31/05/2018.
 * Copyright (c) 2019 Casper Haems. All rights reserved.
 */
public class CustomOkHttpClient {
    private static CustomOkHttpClient mInstance = null;
    private OkHttpClient okHttpClient;

    private CustomOkHttpClient() {
        okHttpClient = new OkHttpClient();
        okHttpClient.newBuilder ()
                .build();

        try {
            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build();

            List<ConnectionSpec> specs = new ArrayList<> ();
            specs.add(cs);
            specs.add(ConnectionSpec.COMPATIBLE_TLS);
            specs.add(ConnectionSpec.CLEARTEXT);


            okHttpClient.newBuilder ()
                    .sslSocketFactory (new TLSSocketFactory (), new CustomTrustManager (/*trustStore*/))
                    .connectionSpecs(specs)
                    .build ();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException/* | CertificateException | IOException */ e) {
            e.printStackTrace ();
        }
    }

    public static synchronized CustomOkHttpClient getInstance(){
        if(mInstance == null){
            Log.d(TAG,"Creating CustomOkHttpClient");
            mInstance = new CustomOkHttpClient ();
        }
        return mInstance;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}