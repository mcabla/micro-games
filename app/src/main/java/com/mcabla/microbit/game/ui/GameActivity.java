package com.mcabla.microbit.game.ui;
/*
 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.googlecode.android_scripting.facade.Facades.MicrobitFacade;
import com.mcabla.microbit.game.Constants;
import com.mcabla.microbit.game.MicroBit;
import com.mcabla.microbit.game.R;
import com.mcabla.microbit.game.Settings;
import com.mcabla.microbit.game.Utility;
import com.mcabla.microbit.game.bluetooth.BleAdapterService;
import com.mcabla.microbit.game.bluetooth.ConnectionStatusListener;
import com.mcabla.microbit.game.python.ScriptService;
import com.mcabla.microbit.game.python.config.GlobalConstants;
import com.mcabla.microbit.game.scripts.Room.GameAsyncTask;
import com.mcabla.microbit.game.scripts.Room.GameModel;
import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ibt.ortc.extensibility.*;
import ibt.ortc.api.*;

public class GameActivity extends AppCompatActivity implements ConnectionStatusListener{
    public static int MODE_DEV = 0;
    public static int MODE_JOIN = 1;
    public static int MODE_MAKE = 2;

    private ConstraintLayout constraintLayout;

    private ActionBar actionBar;
    private ImageView image;
    private TextView title;
    private TextView description;
    private EditText input;
    private EditText input2;
    private Button button;
    private MaterialCardView card;
    private RecyclerView deviceList;

    private int mode = 0;

    private int rondes = 1;
    private int huidigeRonde = 1;
    private boolean gestart = false;
    private GameAsyncTask gameAsyncTask;
    private boolean einde = false;


    private OrtcFactory factory;
    private OrtcClient client;
    private String mChannel;
    private String mName;
    private String mID;

    private ArrayList<String> IDs = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<Integer> scores = new ArrayList<>();
    private ArrayList<Integer> tempScores = new ArrayList<>();
    private ArrayList<String> winnaars = new ArrayList<>();


    private BleAdapterService bluetooth_le_adapter;

    private boolean exiting=false;

    //Buttons
    private int exit_step=0;
    private boolean b1_notifications_on =false;
    private boolean b2_notifications_on =false;

    private boolean b1_pressed = false;
    private boolean b2_pressed = false;

    private int b1_presses = 0;
    private int b2_presses = 0;

    private boolean b1_was_pressed = false;
    private boolean b2_was_pressed = false;

    //LEDs
    private short scrolling_delay;
    //    Octet 0, LED Row 1: bit4 bit3 bit2 bit1 bit0
    //    Octet 1, LED Row 2: bit4 bit3 bit2 bit1 bit0
    //    Octet 2, LED Row 3: bit4 bit3 bit2 bit1 bit0
    //    Octet 3, LED Row 4: bit4 bit3 bit2 bit1 bit0
    //    Octet 4, LED Row 5: bit4 bit3 bit2 bit1 bit0
    private int[][] leds = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
    private byte[] led_matrix_state;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            //Log.d(Constants.TAG,"onServiceConnected");

            //buttons
            b1_notifications_on=false;
            b2_notifications_on=false;

            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
            bluetooth_le_adapter.setNotificationsState(
                Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID),
                Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID), true);

            bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetooth_le_adapter = null;
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_game);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.screen_title_game);
        actionBar.setElevation(0);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        constraintLayout = findViewById(R.id.constraintLayout);

        image = findViewById(R.id.image);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        input = findViewById(R.id.input);
        input2 = findViewById(R.id.input2);
        button = findViewById(R.id.button);
        card = findViewById(R.id.card);
        deviceList = findViewById(R.id.deviceList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceList.setLayoutManager(layoutManager);


        gameAsyncTask = new GameAsyncTask(this);

        exiting = false;

        // read intent data
        final Intent intent = getIntent();
        mode = intent.getIntExtra("mode",0);


        MicroBit.getInstance().setConnection_status_listener(this);
        mID = MicroBit.getInstance().getMicrobit_address();

        if (mode == MODE_JOIN){
            setBackgroundColor("#FF4081");
            setText("Deelnemen aan een groep","Vul hieronder het groepsnummer in waaraan je wilt deelnemen.");
            input.setHint("Groepsnummer");
            button.setText("Deelnemen");
        } else {
            button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            if(mode == MODE_MAKE){
                setText("Een groep maken",  "Vul hieronder het aantal rondes in dat je wilt spelen.");
                input.setHint("Aantal rondes");
                button.setText("Maak");
            }
        }

        // connect to the Bluetooth smart service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Ortc ortc = new Ortc();
        try {
            factory = ortc.loadOrtcFactory("IbtRealtimeSJ");
            client = factory.createClient();
            client.setClusterUrl("http://ortc-developers.realtime.co/server/2.1");
            client.connect("9dQp0d", "testToken");
        } catch (Exception e) {
            System.out.println(String.format("Realtime Error: %s", e.toString()));
            finish();
        }
        if(mode == MODE_MAKE) {
            subscribeToChannel(String.valueOf(ThreadLocalRandom.current().nextInt(1000, 999999)));
            setText("Een groep maken", "Vul hieronder het aantal rondes in dat je wilt spelen.");
            button.setVisibility(View.GONE);
            new Handler ().postDelayed(new Runnable() {

                @Override
                public void run() {
                    setText("Een groep maken", "Vul hieronder het aantal rondes in dat je wilt spelen. Jouw groepsnummer is: " + mChannel);
                    button.setVisibility(View.VISIBLE);
                }
            }, 7000);
        } else if(mode == MODE_DEV) {
            setText("Oefenmodus", "Selecteer het spel dat je wilt oefenen.");
            input2.setVisibility(View.GONE);
            input.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            card.setVisibility(View.VISIBLE);
            deviceList.setVisibility(View.VISIBLE);
            try {
                names = gameAsyncTask.getGameTitles();
                scores = gameAsyncTask.getGameIds();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deviceList.setAdapter(new GameAdapter(names,scores, true,GameActivity.this));
        }


    }

    private  void subscribeToChannel(final String channel){
        mChannel = channel;
        Log.d(Constants.TAG,mChannel);

        client.onConnected = new OnConnected() {
            @Override
            public void run(final OrtcClient sender) {
                // Messaging client connected
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        Log.d(Constants.TAG, "Connected 1");

                        // Now subscribe the channel
                        client.subscribe(channel, true, new OnMessage() {
                            // This function is the message handler
                            // It will be invoked for each message received in myChannel
                            public void run(OrtcClient sender, String channel, String message) {
                                // Received a message
                                final String[] data = message.split(";");
                                Log.d(Constants.TAG, "RECIEVED: 0=" + data[0]);
                                try {
                                    Log.d(Constants.TAG, "RECIEVED: 1=" + data[1]);
                                    Log.d(Constants.TAG, "RECIEVED: 2=" + data[2]);
                                } catch (ArrayIndexOutOfBoundsException ignored) {
                                }


                                if (Objects.equals(data[0], "STATUSNAMES")) {
                                    IDs = new ArrayList(Arrays.asList(data[1].split("\\|")));
                                    names = new ArrayList(Arrays.asList(data[2].split("\\|")));
                                    scores = new ArrayList<>();
                                    for (int i = 0; i < IDs.size(); i++)
                                        scores.add(i, 0);

                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            //System.out.println(IDs);
                                            //System.out.println(names);
                                            GameAdapter mAdapter = new GameAdapter(names, scores, false, GameActivity.this);
                                            deviceList.setAdapter(mAdapter);
                                            card.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else if (Objects.equals(data[0], "STATUSSCORES")) {
                                    scores = new ArrayList(Arrays.asList(data[2].split("\\|")));
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            GameAdapter mAdapter = new GameAdapter(names, scores, false, GameActivity.this);
                                            deviceList.setAdapter(mAdapter);
                                            card.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else if (Objects.equals(data[0], "START")) {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            startScript(Integer.valueOf(data[1]));
                                        }
                                    });
                                } else if (Objects.equals(data[0], "END")) {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            einde = true;
                                            winnaars = new ArrayList(Arrays.asList(data[1].split("\\|")));
                                            StringBuilder winnaarsnamen = new StringBuilder();
                                            for (int i = 0; i < winnaars.size(); i++) {
                                                if (i != 0) {
                                                    if (i == winnaars.size() - 1)
                                                        winnaarsnamen.append(" en");
                                                    else winnaarsnamen.append(",");
                                                }
                                                winnaarsnamen.append(" ").append(winnaars.get(i));
                                            }
                                            setText("Einde", "Proficiat" + winnaarsnamen.toString() + "!");
                                            button.setText("Sluiten");
                                            button.setVisibility(View.VISIBLE);
                                            image.setImageDrawable(getDrawable(R.mipmap.ic_launcher));
                                            setBackgroundColor("#006E65");
                                        }
                                    });
                                } else if (mode == MODE_MAKE) {
                                    if (Objects.equals(data[0], "ADD") && !Objects.equals(data[1], mID) && !gestart) {
                                        if (!IDs.contains(data[1])) {
                                            IDs.add(data[1]);
                                            names.add(data[2]);
                                            scores.add(0);
                                            //Log.d(Constants.TAG,"ADDED: "+data[1]+" "+ data[2] + " 0");

                                        } else {
                                            names.set(IDs.indexOf(data[1]), data[2]);
                                            //Log.d(Constants.TAG,"MODIFIED: "+data[1]+" "+ data[2] + " 0");
                                        }
                                        sendToChannel("STATUSNAMES",
                                                TextUtils.join("|", IDs),
                                                TextUtils.join("|", names));

                                    } else if (Objects.equals(data[0], "SET")) {

                                        tempScores.set(IDs.indexOf(data[1]), Math.round(Float.valueOf(data[2]) * 1000));

                                        if (!tempScores.contains(-1)) {

                                            Integer max = Collections.max(tempScores);
                                            for (int i = 0; i < tempScores.size(); i++) {

                                                if (Objects.equals(tempScores.get(i), max)) {
                                                    scores.set(i, Integer.parseInt(String.valueOf(scores.get(i))) + 1);
                                                }
                                            }

                                            sendToChannel("STATUSSCORES",
                                                    TextUtils.join("|", IDs),
                                                    TextUtils.join("|", scores));


                                            if (huidigeRonde == rondes) {
                                                Integer max2 = 0;
                                                for (int i = 0; i < scores.size(); i++) {
                                                    Integer tempInt = Integer.parseInt(String.valueOf(scores.get(i)));
                                                    if (tempInt > max2) max2 = tempInt;
                                                }
                                                System.out.println("MAX: " + String.valueOf(max2));
                                                for (int i = 0; i < scores.size(); i++) {

                                                    Log.d(Constants.TAG, names.get(i) + " heeft " + String.valueOf(scores.get(i)));
                                                    // accessing each element of array
                                                    if (Integer.parseInt(String.valueOf(scores.get(i))) == max2) {
                                                        Log.d(Constants.TAG, "is gewonnen!");
                                                        winnaars.add(names.get(i));
                                                    }
                                                }
                                                sendToChannel("END",
                                                        TextUtils.join("|", winnaars),
                                                        "");
                                            } else {
                                                huidigeRonde++;

                                                runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        new Handler().postDelayed(new Runnable() {

                                                            @Override
                                                            public void run() {

                                                                tempScores = new ArrayList<>();
                                                                for (int i = 0; i < IDs.size(); i++) {
                                                                    tempScores.add(i, -1);
                                                                }

                                                                try {
                                                                    sendToChannel("START",
                                                                            String.valueOf(gameAsyncTask.getRandomId()),
                                                                            "");
                                                                } catch (ExecutionException | InterruptedException e) {
                                                                    e.printStackTrace();
                                                                    sendToChannel("START",
                                                                            "1",
                                                                            "");
                                                                }
                                                            }
                                                        }, 5000);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        });

                        Log.d(Constants.TAG, "Connected 2");
                        if (mode == MODE_JOIN) new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                Log.d(Constants.TAG, "and adding");
                                sendToChannel("ADD", mID, mName);
                            }
                        }, 1000);
                    }
            });
            }
        };

        Log.d(Constants.TAG, "verbonden: " + String.valueOf(client.isSubscribed(mChannel)));
    }

    private void sendToChannel(String type,String message, String message2){
        client.send(mChannel, type+";"+message+";"+message2);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
        bluetooth_le_adapter = null;

        stopScriptService();
    }

    public void onBackPressed() {
        //Log.d(Constants.TAG, "onBackPressed");
        shutdownSteps();
    }

    private void shutdownSteps() {
        exiting=true;
        exit_step++;
        if (MicroBit.getInstance().isMicrobit_connected()) {
            switch (exit_step) {
                case 1:
                    if (b1_notifications_on) {
                        //Log.d(Constants.TAG, "Disabling Button 1 State notifications");
                        bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID), false);
                    }
                    break;
                case 2:
                    if (b2_notifications_on) {
                        //Log.d(Constants.TAG, "Disabling Button 2 State notifications");
                        bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID), false);
                    }
                    break;
                default:
                    finish();
                    try {
                        unbindService(mServiceConnection);
                    } catch (Exception e) {
                        Log.d(Constants.TAG, e.toString());
                    }

            }
        } else {
            finish();
            try {
                unbindService(mServiceConnection);
            } catch (Exception e) {
                Log.d(Constants.TAG, e.toString());
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.menu_game_help) {
            Intent intent = new Intent(GameActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.GAME_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(Constants.TAG, "onActivityResult");
    }

    @SuppressLint("HandlerLeak")
    private Handler mMessageHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle;
            String service_uuid;
            String characteristic_uuid;
            String descriptor_uuid;
            byte[] b;
            TextView value_text = null;

            switch (msg.what) {
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    //Log.d(Constants.TAG, "Handler received characteristic read result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    //Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " read OK");
                    //Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID))) {
                        if (b.length > 4) {
                            led_matrix_state = b;
                            //Log.d(Constants.TAG, "LED matrix state=" + Utility.byteArrayAsHexString(b));
                        }
                        // now read the Scrolling Delay and store in the Settings singleton
                        bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.SCROLLINGDELAY_CHARACTERISTIC_UUID));
                        showMsg(Utility.htmlColorWhite("Verbonden"));
                    } else if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.SCROLLINGDELAY_CHARACTERISTIC_UUID))) {
                        if (b.length > 1) {
                            scrolling_delay = Utility.shortFromLittleEndianBytes(b);
                            //Log.d(Constants.TAG,"Read Scrolling Delay from micro:bit="+scrolling_delay);
                            Settings.getInstance().setScrolling_delay(scrolling_delay);
                        }
                        showMsg(Utility.htmlColorWhite("Verbonden"));

                    }
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    //Log.d(Constants.TAG, "Handler received characteristic written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    //Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    showMsg(Utility.htmlColorWhite("Verbonden"));
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    //Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    //Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    if (!exiting) {
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID))) {
                            b1_notifications_on = true;
                            //Log.d(Constants.TAG, "Enabling Button 2 State notifications");
                            bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID), true);
                        }
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID))) {
                            b2_notifications_on = true;
                        }
                    } else {
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID))) {
                            b1_notifications_on = false;
                            shutdownSteps();
                        } else {
                            b2_notifications_on = false;
                            shutdownSteps();
                        }
                        finish();
                    }
                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    byte btn_state = b[0];
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID)))) {
                        switch (btn_state) {
                            case 0:
                                //Niet ingedrukt
                                b1_pressed = false;
                                break;
                            case 1:
                                //Ingedrukt
                                b1_pressed = true;
                                b1_presses += 1;
                                b1_was_pressed = true;
                                break;
                            case 2:
                                //Lang ingedrukt
                                b1_pressed = true;
                                break;
                        }

                    } else if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID)))) {
                        //Log.d(Constants.TAG, "Button 2 State received: " + btn_state);
                        switch (btn_state) {
                            case 0:
                                //Niet ingedrukt
                                b2_pressed = false;
                                break;
                            case 1:
                                //Ingedrukt
                                b2_pressed = true;
                                b2_presses += 1;
                                b2_was_pressed = true;
                                break;
                            case 2:
                                //Lang ingedrukt
                                b2_pressed = true;
                                break;
                        }
                    }
                    break;
                case BleAdapterService.GATT_REMOTE_RSSI:
                    bundle = msg.getData();
                    int rssi = bundle.getInt(BleAdapterService.PARCEL_RSSI);
//                    PeripheralControlActivity.this.updateRssi(rssi);
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(Utility.htmlColorWhite(text));
            }
        }
    };


    private void showMsg(final String msg) {
        //Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) GameActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    @Override
    public void connectionStatusChanged(boolean connected) {
        if (connected) {
            showMsg(Utility.htmlColorWhite("Verbonden"));
        } else {
            showMsg(Utility.htmlColorWhite("Verbinding verbroken"));
        }
    }

    @Override
    public void serviceDiscoveryStatusChanged(boolean new_state) {

    }

    public void run(View view) {
        if (mode == MODE_DEV){
            int number = 1;
            try {
                number = Integer.parseInt(((EditText) GameActivity.this.findViewById(R.id.input)).getText().toString());
            } catch (Exception ignored) {
            }
            Log.d(Constants.TAG, "onRunScript with id: " + number);
            startScript(number);
        } else {
            button.setVisibility(View.GONE);
            if (einde) finish();
            else {
                gestart = true;
                input.setVisibility(View.GONE);
                input2.setVisibility(View.GONE);
                mName = input2.getText().toString();
                if (mode == MODE_JOIN) {
                    subscribeToChannel(input.getText().toString());
                } else {
                    IDs.add(0, mID);
                    names.add(0, mName);
                    scores.add(0, 0);
                    rondes = Integer.valueOf(input.getText().toString());
                    sendToChannel("STATUSNAMES",
                            TextUtils.join("|", IDs),
                            TextUtils.join("|", names));

                    tempScores = new ArrayList<>();
                    for (int i = 0; i < IDs.size(); i++) {
                        tempScores.add(i, -1);
                    }

                    try {
                        sendToChannel("START",
                                String.valueOf(gameAsyncTask.getRandomId()),
                                "");
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        sendToChannel("START",
                                "1",
                                "");
                    }
                }
            }
        }
    }

    public void startScript(int id){
        Log.d(Constants.TAG, "STARTING SCRIPT " + String.valueOf(id));
        boolean installNeeded = Utility.isInstallNeeded(this);

        if(installNeeded) {
            new InstallAsyncTask().execute(id);
        }
        else {
            runScriptService(id);
        }
    }

    public class InstallAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
        private Integer id;
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            id = params[0];
            Log.i(GlobalConstants.LOG_TAG, "Installing...");

            Utility.copyResourcesToLocal(getBaseContext());

            // TODO
            return true;
        }

        @Override
        protected void onPostExecute(Boolean installStatus) {

            if(installStatus) Log.d(GlobalConstants.LOG_TAG, "installSucceed");
            else Log.d(GlobalConstants.LOG_TAG, "installSucceed");

            runScriptService(id);
        }

    }

    private void setBackgroundColor(String colorString){
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorString)));
        constraintLayout.setBackgroundColor(Color.parseColor(colorString));
    }

    private void setText(String titleString, String descriptionString) {
        title.setText(titleString);
        description.setText(descriptionString);

    }

    private void runScriptService(int id) {
        Intent intent = new Intent(this, ScriptService.class);
        intent.putExtra("id",id);

        try {
            GameModel gameModel = gameAsyncTask.getGame(id);
            setBackgroundColor(gameModel.getColor());
            setText(gameModel.getName(),gameModel.getDescription());
            Picasso.get()
                    .load("https://raw.githubusercontent.com/minecabla/micro-games/master/games/"+gameModel.getIcon())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(image);


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startService(intent);
        b1_pressed = false;
        b2_pressed = false;

        b1_presses = 0;
        b2_presses = 0;

        b1_was_pressed = false;
        b2_was_pressed = false;

        MicrobitFacade.updateActivity(this);
    }

    private void stopScriptService() {
            stopService(new Intent(this, ScriptService.class));
    }

    /*---------------------------------------------*/
    /*------------------MICRO:BIT------------------*/
    /*---------------------------------------------*/

    /*-------------------DISPLAY-------------------*/
    public void sendText(String text) {
        byte[] utf8_bytes = text.getBytes(StandardCharsets.UTF_8);
        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDTEXT_CHARACTERISTIC_UUID), utf8_bytes);
    }

    public void sendPixel(int x, int y, int z){
        if (led_matrix_state == null) {
            //Log.d(Constants.TAG, "sendPixel - LED state array has not yet been initialised so ignoring touch");
        } else {
            //Log.d(Constants.TAG, "x:" +x + " y:" + y + " z:" + z);
            if (z > 0) {
                //AAN
                leds[y][x] = 9;
                led_matrix_state[y] = (byte) (led_matrix_state[y] | (1 << (4-x)));
            } else {
                //UIT
                leds[y][x] = 0;
                led_matrix_state[y] = (byte) (led_matrix_state[y] & ~(1 << (4-x)));
            }
        }

        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID), led_matrix_state);
    }

    public void sendImage(String img){
        switch (img) {
            case "R":
                img = Constants.IMAGE_R;
                break;
            case "1":
                img = Constants.IMAGE_1;
                break;
            case "2":
                img = Constants.IMAGE_2;
                break;
            case "3":
                img = Constants.IMAGE_3;
                break;
            case "4":
                img = Constants.IMAGE_4;
                break;
            case "5":
                img = Constants.IMAGE_5;
                break;
            case "6":
                img = Constants.IMAGE_6;
                break;
            case "7":
                img = Constants.IMAGE_7;
                break;
            case "8":
                img = Constants.IMAGE_8;
                break;
            case "9":
                img = Constants.IMAGE_9;
                break;
            case "10":
                img = Constants.IMAGE_10;
                break;
        }

        if (led_matrix_state == null) {
            //Log.d(Constants.TAG, "sendImage - LED state array has not yet been initialised so ignoring touch");
        } else {
            char[] imgList = img.toCharArray();
            int i = 0;
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    //Log.d(Constants.TAG, "x:" +x + " y:" + y + " z:" + String.valueOf((int)  imgList[i]));
                    if (imgList[i] > 48) {
                        //AAN
                        leds[y][x] = 9;
                        led_matrix_state[y] = (byte) (led_matrix_state[y] | (1 << (4-x)));
                    } else {
                        //UIT
                        leds[y][x] = 0;
                        led_matrix_state[y] = (byte) (led_matrix_state[y] & ~(1 << (4-x)));
                    }
                    i++;
                }
            }

        }

        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID), led_matrix_state);

    }

    public int getPixel(int x, int y){
        if (led_matrix_state != null){
            //Log.d(Constants.TAG,"GET PIXEL: x:"+String.valueOf(x)+" y:"+String.valueOf(y)+" z:"+String.valueOf(leds[y][x]));
            return leds[y][x];
        }
        return 0;
    }

    /*-------------------BUTTONS-------------------*/
    public int getPresses(int button) {
        if (button == 1){
            int presses = b1_presses;
            b1_presses = 0;
            return presses;
        }
        int presses = b2_presses;
        b2_presses = 0;
        return presses;
    }

    public boolean getPressed(int button) {
        if (button == 1) return b1_pressed;
        return b2_pressed;
    }

    public boolean getWasPressed(int button) {
        if (button == 1){
            if (b1_was_pressed) {
                b1_was_pressed = false;
                return true;
            }
            return false;
        }
        if (b2_was_pressed) {
            b2_was_pressed = false;
            return true;
        }
        return false;
    }

    /*---------------------MISC--------------------*/
    public void sendScore(String score) {
        if (mode == MODE_DEV) Log.d(Constants.TAG, "SCORE: "+score);
        else sendToChannel("SET", mID, score);
    }

}