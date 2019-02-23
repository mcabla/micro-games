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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.googlecode.android_scripting.FileUtils;
import com.mcabla.microbit.game.Constants;
import com.mcabla.microbit.game.MicroBit;
import com.mcabla.microbit.game.R;
import com.mcabla.microbit.game.Settings;
import com.mcabla.microbit.game.Utility;
import com.mcabla.microbit.game.bluetooth.BleAdapterService;
import com.mcabla.microbit.game.bluetooth.ConnectionStatusListener;
import com.mcabla.microbit.game.python.BackgroundScriptService;
import com.mcabla.microbit.game.python.ScriptService;
import com.mcabla.microbit.game.python.config.GlobalConstants;
import com.mcabla.microbit.game.python.support.Utils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements ConnectionStatusListener, View.OnTouchListener {

    private static final int ACCELEROMETER_G_RANGE = 2;
    private static final int ACCELEROMETER_DIVISOR = 512;

    private float[] accel_input = new float[3];
    private float[] accel_output = new float[3];

    private BleAdapterService bluetooth_le_adapter;

    private boolean exiting=false;
    private int accelerometer_period;

    private boolean notifications_on =false;
    private long start_time;
    private int minute_number;
    private int notification_count;
    private boolean apply_smoothing=true;
    private boolean accel_logs=false;

    //Buttons
    private int exit_step=0;
    private boolean b1_notifications_on =false;
    private boolean b2_notifications_on =false;

    //LEDs
    private short scrolling_delay;
    //    Octet 0, LED Row 1: bit4 bit3 bit2 bit1 bit0
    //    Octet 1, LED Row 2: bit4 bit3 bit2 bit1 bit0
    //    Octet 2, LED Row 3: bit4 bit3 bit2 bit1 bit0
    //    Octet 3, LED Row 4: bit4 bit3 bit2 bit1 bit0
    //    Octet 4, LED Row 5: bit4 bit3 bit2 bit1 bit0
    private byte[] led_matrix_state;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG,"onServiceConnected");

            //buttons
            b1_notifications_on=false;
            b2_notifications_on=false;

            notifications_on=false;
            start_time = System.currentTimeMillis();
            minute_number=1;
            notification_count=0;
            showBenchmark();
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
            bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID),Utility.normaliseUUID(BleAdapterService.ACCELEROMETERPERIOD_CHARACTERISTIC_UUID));

            if (bluetooth_le_adapter.setNotificationsState(
                    Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID),
                    Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID), true)) {
                showMsg(Utility.htmlColorGreen("Button 1 State notifications ON"),2);
            } else {
                showMsg(Utility.htmlColorRed("Failed to set Button 1 State notifications ON"),2);
            }

            if (bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID))) {
                showMsg(Utility.htmlColorGreen("Reading LED matrix state"),3);
            } else {
                showMsg(Utility.htmlColorRed("Failed to readLED matrix state"),3);
            }
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
        getSupportActionBar().setTitle(R.string.screen_title_game);

        exiting = false;

        GridLayout led_grid = GameActivity.this.findViewById(R.id.led_grid);
        int count = led_grid.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = led_grid.getChildAt(i);
            child.setOnTouchListener(this);
        }

        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setConnection_status_listener(this);

        // connect to the Bluetooth smart service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifications_on) {
            bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID), false);
        }
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
        bluetooth_le_adapter = null;
    }

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        if (MicroBit.getInstance().isMicrobit_connected() && notifications_on) {
            bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID), false);
        }
        shutdownSteps();
    }

    private void shutdownSteps() {
        exiting=true;
        exit_step++;
        if (MicroBit.getInstance().isMicrobit_connected()) {
            switch (exit_step) {
                case 1:
                    if (b1_notifications_on) {
                        Log.d(Constants.TAG, "Disabling Button 1 State notifications");
                        bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID), false);
                    }
                    break;
                case 2:
                    if (b2_notifications_on) {
                        Log.d(Constants.TAG, "Disabling Button 2 State notifications");
                        bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID), false);
                    }
                    break;
                default:
                    finish();
                    try {
                        unbindService(mServiceConnection);
                    } catch (Exception e) {
                    }

            }
        } else {
            finish();
            try {
                unbindService(mServiceConnection);
            } catch (Exception e) {
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
        Log.d(Constants.TAG, "onActivityResult");
    }

    public void onApplySmoothingChanged(View v) {
        apply_smoothing = ((Switch) v).isChecked();
    }

    public void onApplyAccelLogsChanged(View v) {
        accel_logs = ((Switch) v).isChecked();
    }

    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            String descriptor_uuid = "";
            byte[] b = null;
            TextView value_text = null;

            switch (msg.what) {
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    Log.d(Constants.TAG, "Handler received characteristic read result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " read OK");
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERPERIOD_CHARACTERISTIC_UUID))) {
                        boolean got_accelerometer_period = false;
                        byte [] period_bytes = new byte[2];
                        if (b.length == 2) {
                            period_bytes[0] = b[0];
                            period_bytes[1] = b[1];
                            got_accelerometer_period = true;
                        } else {
                            if (b.length == 1) {
                                period_bytes[0] = b[0];
                                period_bytes[1] = 0x00;
                                got_accelerometer_period = true;
                            } else {
                                Log.d(Constants.TAG,"Couldn't obtain value of accelerometer period");
                            }
                        }
                        if (got_accelerometer_period) {
                            accelerometer_period = (int) Utility.shortFromLittleEndianBytes(period_bytes);
                            Settings.getInstance().setAccelerometer_period((short) accelerometer_period);
                            showAccelerometerPeriod();
                        }
                    } else if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID))) {
                        if (b.length > 4) {
                            led_matrix_state = b;
                            Log.d(Constants.TAG, "LED matrix state=" + Utility.byteArrayAsHexString(b));
                            setUiFromMatrixState(led_matrix_state);
                        }
                        // now read the Scrolling Delay and store in the Settings singleton
                        bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.SCROLLINGDELAY_CHARACTERISTIC_UUID));
                        showMsg(Utility.htmlColorGreen("Ready"),3);
                    } else if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.SCROLLINGDELAY_CHARACTERISTIC_UUID))) {
                        if (b.length > 1) {
                            scrolling_delay = Utility.shortFromLittleEndianBytes(b);
                            Log.d(Constants.TAG,"Read Scrolling Delay from micro:bit="+scrolling_delay);
                            Settings.getInstance().setScrolling_delay(scrolling_delay);
                        }
                        showMsg(Utility.htmlColorGreen("Ready"),3);

                    }
                    bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID), true);
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    Log.d(Constants.TAG, "Handler received characteristic written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    showAccelerometerPeriod();
                    showMsg(Utility.htmlColorGreen("Ready"));
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    if (!exiting) {
                        showMsg(Utility.htmlColorGreen("Accelerometer Data notifications ON"));
                        notifications_on=true;
                        start_time = System.currentTimeMillis();
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID))) {
                            b1_notifications_on = true;
                            Log.d(Constants.TAG, "Enabling Button 2 State notifications");
                            if (bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID), true)) {
                                showMsg(Utility.htmlColorGreen("Button 2 State notifications ON"),2);
                            } else {
                                showMsg(Utility.htmlColorRed("Failed to set Button 2 State notifications ON"),2);
                            }
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
                        showMsg(Utility.htmlColorGreen("Accelerometer Data notifications OFF"));
                        notifications_on=false;
                        finish();
                    }
                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    byte btn_state = b[0];
                    if(accel_logs || !characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID))))
                        Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID)))) {
                        notification_count++;
                        if (System.currentTimeMillis() - start_time >= 60000) {
                            showBenchmark();
                            notification_count = 0;
                            minute_number++;
                            start_time = System.currentTimeMillis();
                        }
                        byte[] x_bytes = new byte[2];
                        byte[] y_bytes = new byte[2];
                        byte[] z_bytes = new byte[2];
                        System.arraycopy(b, 0, x_bytes, 0, 2);
                        System.arraycopy(b, 2, y_bytes, 0, 2);
                        System.arraycopy(b, 4, z_bytes, 0, 2);
                        short raw_x = Utility.shortFromLittleEndianBytes(x_bytes);
                        short raw_y = Utility.shortFromLittleEndianBytes(y_bytes);
                        short raw_z = Utility.shortFromLittleEndianBytes(z_bytes);
                        if(accel_logs) Log.d(Constants.TAG, "Accelerometer Data received: x=" + raw_x + " y=" + raw_y + " z=" + raw_z);


                        // range is -1024 : +1024
                        // Starting with the LED display face up and level (perpendicular to gravity) and edge connector towards your body:
                        // A negative X value means tilting left, a positive X value means tilting right
                        // A negative Y value means tilting away from you, a positive Y value means tilting towards you
                        // A negative Z value means ?

                        accel_input[0] = raw_x / 1000f;
                        accel_input[1] = raw_y / 1000f;
                        accel_input[2] = raw_z / 1000f;
                        if (apply_smoothing) {
                            accel_output = Utility.lowPass(accel_input, accel_output);
                        } else {
                            accel_output[0] = accel_input[0];
                            accel_output[1] = accel_input[1];
                            accel_output[2] = accel_input[2];
                        }

                        double pitch = Math.atan(accel_output[0] / Math.sqrt(Math.pow(accel_output[1], 2) + Math.pow(accel_output[2], 2)));
                        double roll = Math.atan(accel_output[1] / Math.sqrt(Math.pow(accel_output[0], 2) + Math.pow(accel_output[2], 2)));
                        //convert radians into degrees
                        pitch = pitch * (180.0 / Math.PI);
                        roll = -1 * roll * (180.0 / Math.PI);

                        showAccelerometerData(accel_output,pitch,roll);

                    } else if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID)))) {
                        Log.d(Constants.TAG, "Button 1 State received: " + btn_state);
                        ImageView b1_image = (ImageView) GameActivity.this.findViewById(R.id.button1);
                        setButtonImage(b1_image, btn_state);
                        TextView b1_label = (TextView) GameActivity.this.findViewById(R.id.button1_state);
                        setButtonLabel(b1_label, btn_state);
                    } else if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID)))) {
                        Log.d(Constants.TAG, "Button 2 State received: " + btn_state);
                        ImageView b2_image = (ImageView) GameActivity.this.findViewById(R.id.button2);
                        setButtonImage(b2_image, btn_state);
                        TextView b2_label = (TextView) GameActivity.this.findViewById(R.id.button2_state);
                        setButtonLabel(b2_label, btn_state);
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
                    showMsg(Utility.htmlColorRed(text));
            }
        }
    };

    private void setButtonImage(ImageView btn_image, byte btn_state) {
        switch (btn_state) {
            case 0:
                btn_image.setImageResource(R.drawable.button_black_40);
                break;
            case 1:
                btn_image.setImageResource(R.drawable.button_green_40);
                break;
            case 2:
                btn_image.setImageResource(R.drawable.button_red_40);
                break;
        }
    }

    private void setButtonLabel(TextView btn_label, byte btn_state) {
        switch (btn_state) {
            case 0:
                btn_label.setText("Not Pressed");
                break;
            case 1:
                btn_label.setText("Pressed");
                break;
            case 2:
                btn_label.setText("Long Press");
                break;
        }
    }

    private void setUiFromMatrixState(byte[] matrix_state) {
        GridLayout grid = GameActivity.this.findViewById(R.id.led_grid);
        int count = grid.getChildCount();
        int display_row = 0;
        int led_in_row = 4;
        for (int i = 0; i < count; i++) {
            Log.d(Constants.TAG, "display_row=" + display_row + ",led_in_row=" + led_in_row);
            View child = grid.getChildAt(i);
            if ((matrix_state[display_row] & (1 << led_in_row)) != 0) {
                child.setBackgroundColor(Color.RED);
            } else {
                child.setBackgroundColor(Color.parseColor("#C0C0C0"));
            }
            led_in_row = led_in_row - 1;
            if (led_in_row < 0) {
                led_in_row = 4;
                display_row++;
            }
        }
    }

    private void showMsg(final String msg) {
        showMsg(msg,1);
    }

    private void showMsg(final String msg, final int box) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (box == 1) ((TextView) GameActivity.this.findViewById(R.id.message1)).setText(Html.fromHtml(msg));
                if (box == 2) ((TextView) GameActivity.this.findViewById(R.id.message2)).setText(Html.fromHtml(msg));
                if (box == 3) ((TextView) GameActivity.this.findViewById(R.id.message3)).setText(Html.fromHtml(msg));
            }
        });
    }

    private void showAccelerometerPeriod() {
        Log.d(Constants.TAG, "Accelerometer Period: "+accelerometer_period+"ms");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) GameActivity.this.findViewById(R.id.accel_period)).setText("Polling: "+Integer.toString(accelerometer_period)+"ms");
            }
        });
    }

    private void showAccelerometerData(final float [] accel_data, final double pitch, final double roll) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) GameActivity.this.findViewById(R.id.accel_x)).setText("X: " + String.format("%.3f", accel_data[0]));
                ((TextView) GameActivity.this.findViewById(R.id.accel_y)).setText("Y: " + String.format("%.3f", accel_data[1]));
                ((TextView) GameActivity.this.findViewById(R.id.accel_z)).setText("Z: " + String.format("%.3f", accel_data[2]));
                ((TextView) GameActivity.this.findViewById(R.id.pitch)).setText("PITCH: " + String.format("%.1f", pitch));
                ((TextView) GameActivity.this.findViewById(R.id.roll)).setText("ROLL: " + String.format("%.1f", roll));
                GameActivity.this.findViewById(R.id.microbit).setRotationX((float) roll);
                GameActivity.this.findViewById(R.id.microbit).setRotationY((float) pitch);
            }
        });
    }

	private void showBenchmark() {
			final int notifications_per_minute = notification_count;
			final int notifications_per_second = notification_count / 60;
			Log.d(Constants.TAG,"Minute: " + Integer.toString(minute_number));
			Log.d(Constants.TAG,"Notification Count: " + Integer.toString(notification_count));
			Log.d(Constants.TAG,"Notifications per Second: " + Integer.toString(notifications_per_second));
	}

    @Override
    public void connectionStatusChanged(boolean connected) {
        if (connected) {
            showMsg(Utility.htmlColorGreen("Verbonden"));
        } else {
            showMsg(Utility.htmlColorRed("Verbinding verbroken"));
        }
    }

    @Override
    public void serviceDiscoveryStatusChanged(boolean new_state) {

    }

    public void onSetDisplay(View view) {
        Log.d(Constants.TAG, "onSetDisplay");
        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID), led_matrix_state);
    }

    public void onSendText(View view) {
        Log.d(Constants.TAG, "onSendText");
        EditText text = (EditText) GameActivity.this.findViewById(R.id.display_text2);
        Log.d(Constants.TAG, "onSendText: " + text.getText().toString());
        try {
            byte[] utf8_bytes = text.getText().toString().getBytes("UTF-8");
            Log.d(Constants.TAG, "UTF8 bytes: 0x" + Utility.byteArrayAsHexString(utf8_bytes));
            bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDTEXT_CHARACTERISTIC_UUID), utf8_bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            showMsg("Unable to convert text to UTF8 bytes");
        }
        startScript();
    }

    public void sendText(String text) {
        Log.d(Constants.TAG, "sendText");
        /*EditText text = (EditText) GameActivity.this.findViewById(R.id.display_text2);*/
        Log.d(Constants.TAG, "sendText: " + text);
        byte[] utf8_bytes = text.getBytes(StandardCharsets.UTF_8);
        Log.d(Constants.TAG, "UTF8 bytes: 0x" + Utility.byteArrayAsHexString(utf8_bytes));
        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDTEXT_CHARACTERISTIC_UUID), utf8_bytes);
    }

    private void startScript(){
        boolean installNeeded = Utility.isInstallNeeded(this);

        if(installNeeded) {
            new InstallAsyncTask().execute();
        }
        else {
            runScriptService();
        }
    }

    public class InstallAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(GlobalConstants.LOG_TAG, "Installing...");

            createOurExternalStorageRootDir();

            Utility.copyResourcesToLocal(getBaseContext());

            // TODO
            return true;
        }

        @Override
        protected void onPostExecute(Boolean installStatus) {

            if(installStatus) Log.d(GlobalConstants.LOG_TAG, "installSucceed");
            else Log.d(GlobalConstants.LOG_TAG, "installSucceed");

            runScriptService();
        }

    }

    private void runScriptService() {
        if(GlobalConstants.IS_FOREGROUND_SERVICE) {
            startService(new Intent(this, ScriptService.class));
        }
        else {
            startService(new Intent(this, BackgroundScriptService.class));
        }
    }

    private void createOurExternalStorageRootDir() {
        Utils.createDirectoryOnExternalStorage( this.getPackageName() );
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(Constants.TAG, "onTouch - " + event.actionToString((event.getAction())));
        if (led_matrix_state == null) {
            Log.d(Constants.TAG, "onTouch - LED state array has not yet been initialised so ignoring touch");
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            GridLayout grid = (GridLayout) GameActivity.this.findViewById(R.id.led_grid);
            int count = grid.getChildCount();
            int display_row = 0;
            int led_in_row = 4;
            for (int i = 0; i < count; i++) {
                View child = grid.getChildAt(i);
                if (child == v) {
                    Log.d(Constants.TAG,"Touched row "+display_row+", LED "+led_in_row);
                    if ((led_matrix_state[display_row] & (1 << led_in_row)) != 0) {
                        child.setBackgroundColor(Color.parseColor("#C0C0C0"));
                        led_matrix_state[display_row] = (byte) (led_matrix_state[display_row] & ~(1 << led_in_row));
                    } else {
                        child.setBackgroundColor(Color.RED);
                        led_matrix_state[display_row] = (byte) (led_matrix_state[display_row] | (1 << led_in_row));
                    }
                    return true;
                }
                led_in_row = led_in_row - 1;
                if (led_in_row < 0) {
                    led_in_row = 4;
                    display_row++;
                }
            }
            return true;
        }
        return false;
    }
}