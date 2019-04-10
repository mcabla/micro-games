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
import java.util.List;

import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.WindowManager;
import android.widget.Toast;

import com.mcabla.microbit.game.bluetooth.BleAdapterService;
import com.mcabla.microbit.game.bluetooth.ConnectionStatusListener;
import com.mcabla.microbit.game.Constants;
import com.mcabla.microbit.game.MicroBit;
import com.mcabla.microbit.game.R;
import com.mcabla.microbit.game.Utility;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity implements ConnectionStatusListener {

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_ID = "id";

    private BleAdapterService bluetooth_le_adapter;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
            connectToDevice();
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
        setContentView(R.layout.activity_menu);
        getSupportActionBar().setTitle(R.string.screen_title_menu);

        Log.d(Constants.TAG, "MenuActivity onCreate");

        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setMicrobit_name(intent.getStringExtra(EXTRA_NAME));
        MicroBit.getInstance().setMicrobit_address(intent.getStringExtra(EXTRA_ID));
        MicroBit.getInstance().setConnection_status_listener(this);

        // connect to the Bluetooth service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        findViewById(R.id.join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MicroBit.getInstance().isMicrobit_connected()|| !MicroBit.getInstance().isMicrobit_services_discovered() ) {
                    Log.d(Constants.TAG, "onDemoSelected - micro:bit is not connected or service discovery has not completed so ignoring");
                    showMsg(Utility.htmlColorRed("Niet verbonden met micro:bit - probeer opnieuw"));
                    return;
                }
                if (MicroBit.getInstance().hasService(BleAdapterService.LEDSERVICE_SERVICE_UUID)) {
                    if (MicroBit.getInstance().hasService(BleAdapterService.BUTTONSERVICE_SERVICE_UUID)) {
                        Intent intent = new Intent(MenuActivity.this, GameActivity.class);
                        intent.putExtra("mode",GameActivity.MODE_JOIN);
                        startActivity(intent);
                    } else {
                        showMsg(Utility.htmlColorRed("Knoppen Service is niet toegankelijk op deze micro:bit"));
                        refreshBluetoothServices();
                    }
                } else {
                    showMsg(Utility.htmlColorRed("LED Service is niet toegankelijk op deze micro:bit"));
                    refreshBluetoothServices();
                }

            }
        });

        findViewById(R.id.make).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MicroBit.getInstance().isMicrobit_connected()|| !MicroBit.getInstance().isMicrobit_services_discovered() ) {
                    Log.d(Constants.TAG, "onDemoSelected - micro:bit is not connected or service discovery has not completed so ignoring");
                    showMsg(Utility.htmlColorRed("Niet verbonden met micro:bit - probeer opnieuw"));
                    return;
                }
                if (MicroBit.getInstance().hasService(BleAdapterService.LEDSERVICE_SERVICE_UUID)) {
                    if (MicroBit.getInstance().hasService(BleAdapterService.BUTTONSERVICE_SERVICE_UUID)) {
                        Intent intent = new Intent(MenuActivity.this, GameActivity.class);
                        intent.putExtra("mode",GameActivity.MODE_MAKE);
                        startActivity(intent);
                    } else {
                        showMsg(Utility.htmlColorRed("Knoppen Service is niet toegankelijk op deze micro:bit"));
                        refreshBluetoothServices();
                    }
                } else {
                    showMsg(Utility.htmlColorRed("LED Service is niet toegankelijk op deze micro:bit"));
                    refreshBluetoothServices();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_menu_help) {
            if (MicroBit.getInstance().isMicrobit_services_discovered()) {
                Intent intent = new Intent(MenuActivity.this, HelpActivity.class);
                intent.putExtra(Constants.URI, Constants.MENU_HELP);
                startActivity(intent);
                return true;
            } else {
                Log.d(Constants.TAG,"Services not yet discovered");
            }
        }
        if (id == R.id.menu_menu_services) {
            Intent intent = new Intent(MenuActivity.this, ServicesPresentActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_menu_refresh) {
            refreshBluetoothServices();
            return true;
        }if (id == R.id.menu_menu_dev) {
            if (!MicroBit.getInstance().isMicrobit_connected()|| !MicroBit.getInstance().isMicrobit_services_discovered() ) {
                Log.d(Constants.TAG, "onDemoSelected - micro:bit is not connected or service discovery has not completed so ignoring");
                showMsg(Utility.htmlColorRed("Niet verbonden met micro:bit - probeer opnieuw"));
                return true;
            }
            if (MicroBit.getInstance().hasService(BleAdapterService.LEDSERVICE_SERVICE_UUID)) {
                if (MicroBit.getInstance().hasService(BleAdapterService.BUTTONSERVICE_SERVICE_UUID)) {
                    Intent intent = new Intent(MenuActivity.this, GameActivity.class);
                    intent.putExtra("mode",GameActivity.MODE_DEV);
                    startActivity(intent);
                } else {
                    showMsg(Utility.htmlColorRed("Knoppen Service is niet toegankelijk op deze micro:bit"));
                    refreshBluetoothServices();
                }
            } else {
                showMsg(Utility.htmlColorRed("LED Service is niet toegankelijk op deze micro:bit"));
                refreshBluetoothServices();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshBluetoothServices() {
        if (MicroBit.getInstance().isMicrobit_connected()) {
            Toast toast = Toast.makeText(this, "Connectie aan het venieuwen", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            MicroBit.getInstance().resetAttributeTables();
            bluetooth_le_adapter.refreshDeviceCache();
            bluetooth_le_adapter.discoverServices();
        } else {
            Toast toast = Toast.makeText(this, "Verzoek genegeerd - niet verbonden", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MicroBit.getInstance().isMicrobit_connected()) {
            showMsg(Utility.htmlColorWhite("Verbonden"));
        } else {
            showMsg(Utility.htmlColorRed("Niet verbonden"));
        }
    }

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        if (MicroBit.getInstance().isMicrobit_connected()) {
            try {
                bluetooth_le_adapter.disconnect();
                // may already have unbound. No API to check state so....
                unbindService(mServiceConnection);
            } catch (Exception e) {
            }
        }
        finish();
    }

    private void connectToDevice() {
        showMsg(Utility.htmlColorBlue("Verbinden met micro:bit"));
        if (bluetooth_le_adapter.connect(MicroBit.getInstance().getMicrobit_address())) {
        } else {
            showMsg(Utility.htmlColorRed("Verbinding met micro:bit mislukt"));
        }
    }

    // Service message handler
    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            byte[] b = null;
            TextView value_text = null;

            switch (msg.what) {
                case BleAdapterService.GATT_CONNECTED:
                    showMsg(Utility.htmlColorWhite("Zoeken naar services..."));
                    bluetooth_le_adapter.discoverServices();
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    showMsg(Utility.htmlColorRed("Niet verbonden"));
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    //Log.d(Constants.TAG, "Services discovered");
                    showMsg(Utility.htmlColorWhite("Verbonden"));
                    List<BluetoothGattService> slist = bluetooth_le_adapter.getSupportedGattServices();
                    for (BluetoothGattService svc : slist) {
                        //Log.d(Constants.TAG, "UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());
                        MicroBit.getInstance().addService(svc);
                    }
                    MicroBit.getInstance().setMicrobit_services_discovered(true);
                    break;
            }
        }
    };

    private void showMsg(final String msg) {
        //Log.d(Constants.TAG, msg);
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) MenuActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
                        }
                    });
                }
            }
        );
    }

    @Override
    public void connectionStatusChanged(boolean connected) {
        if (connected) {
            showMsg(Utility.htmlColorWhite("Verbonden"));
        } else {
            showMsg(Utility.htmlColorRed("Niet verbonden"));
        }
    }

    @Override
    public void serviceDiscoveryStatusChanged(boolean new_state) {

    }
}