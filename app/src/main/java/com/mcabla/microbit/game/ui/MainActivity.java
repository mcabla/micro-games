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
import java.util.ArrayList;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mcabla.microbit.game.Constants;
import com.mcabla.microbit.game.MicroBit;
import com.mcabla.microbit.game.R;
import com.mcabla.microbit.game.Settings;
import com.mcabla.microbit.game.Utility;
import com.mcabla.microbit.game.bluetooth.BleScanner;
import com.mcabla.microbit.game.bluetooth.BleScannerFactory;
import com.mcabla.microbit.game.bluetooth.ScanResultsConsumer;
import com.mcabla.microbit.game.python.config.GlobalConstants;
import com.mcabla.microbit.game.python.support.Utils;
import com.mcabla.microbit.game.scripts.API.APICommunicator;
import com.mcabla.microbit.game.scripts.Room.AppDatabase;
import com.mcabla.microbit.game.scripts.Room.GameAsyncTask;
import com.mcabla.microbit.game.scripts.Room.GameModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements ScanResultsConsumer {

    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean ble_scanning = false;
    private Handler handler = new Handler();
    private ListAdapter ble_device_list_adapter;
    private BleScanner ble_scanner;
    private static final long SCAN_TIMEOUT = 30000;
    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private boolean permissions_granted=false;
    private static final String DEVICE_NAME_START = "BBC micro";
    private int device_count=0;
    private Toast toast;
    private BluetoothDevice device;

    static class ViewHolder {
        public TextView text;
        public TextView bdaddr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setButtonText();
        getSupportActionBar().setTitle(R.string.screen_title_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        showMsg(Utility.htmlColorGreen("Klaar"));

        Settings.getInstance().restore(this);

        ble_device_list_adapter = new ListAdapter();

        ListView listView = this.findViewById(R.id.deviceList);
        listView.setAdapter(ble_device_list_adapter);

        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        ble_scanner = BleScannerFactory.getBleScanner(this.getApplicationContext());
        ble_scanner.setDevice_name_start(DEVICE_NAME_START);
        ble_scanner.setSelect_bonded_devices_only(true);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (ble_scanning) {
                    setScanState(false);
                    ble_scanner.stopScanning();
                }

                device = ble_device_list_adapter.getDevice(position);
                if (device.getBondState() == BluetoothDevice.BOND_NONE && Settings.getInstance().isFilter_unpaired_devices()) {
                    device.createBond();
                    showMsg(Utility.htmlColorRed("De geselecteerde micro:bit moet gekoppeld zijn - aan het koppelen"));
                    return;
                }
                nextActivity();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.d(Constants.TAG,e.getClass().getCanonicalName()+":"+e.getMessage());
        }
        Settings.getInstance().save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_main_settings) {
            Intent intent = new Intent(MainActivity.this, MainSettingsActivity.class);
            startActivityForResult(intent, MainSettingsActivity.START_MAIN_SETTINGS);
            return true;
        }
        if (id == R.id.menu_main_help) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.MAIN_HELP);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_main_about) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.MAIN_ABOUT);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "onActivityResult");
        if (requestCode == MainSettingsActivity.START_MAIN_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.d(Constants.TAG, "onActivityResult RESULT_OK");
                setButtonText();
                showMsg(Utility.htmlColorGreen("Ready"));
            } else {
                Log.d(Constants.TAG, "onActivityResult NOT RESULT_OK");
            }
        }
    }

    public void onScan(View view) {

        if (!ble_scanner.isScanning()) {
            device_count=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissions_granted = false;
                    requestLocationPermission();
                } else {
                    Log.i(Constants.TAG, "Location permission has already been granted. Starting scanning.");
                    permissions_granted = true;
                }
            } else {
                // the ACCESS_COARSE_LOCATION permission did not exist before M so....
                permissions_granted = true;
            }
            startScanning();
        } else {
            showMsg(Utility.htmlColorGreen("Stoppen met scannen"));
            ble_scanner.stopScanning();
        }
    }

    //TODO: refactor this
    public class InstallAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(GlobalConstants.LOG_TAG, "Installing...");

            Utility.copyResourcesToLocal(getBaseContext());

            // TODO
            return true;
        }

        @Override
        protected void onPostExecute(Boolean installStatus) {

            if (installStatus) Log.d(GlobalConstants.LOG_TAG, "install Succeed");
            else Log.d(GlobalConstants.LOG_TAG, "install Failed");

            scan();
        }

    }

    private void scan() {
        if (permissions_granted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ble_device_list_adapter.clear();
                    ble_device_list_adapter.notifyDataSetChanged();
                }
            });
            simpleToast(getScanningMessage(),2000);
            ble_scanner.startScanning(this, SCAN_TIMEOUT);
        } else {
            showMsg(Utility.htmlColorRed("Verzoek om naar bluetooth apparaten te zoeken was afgewezen."));
        }
    }

    private void startScanning() {

        boolean installNeeded = Utility.isInstallNeeded(this);

        if(installNeeded) {
            new InstallAsyncTask().execute();
        }
        else {
            scan();
        }
    }

    private void requestLocationPermission() {
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            Log.i(Constants.TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            Log.i(Constants.TAG, "Received response for location permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted
                Log.i(Constants.TAG, "Location permission has now been granted. Scanning.....");
                permissions_granted = true;
                if (ble_scanner.isScanning()) {
                    startScanning();
                }
            }else{
                Log.i(Constants.TAG, "Location permission was NOT granted.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void generalAlert(String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private void simpleToast(String message, int duration) {
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void setScanState(boolean value) {
        ble_scanning = value;
        ((Button) this.findViewById(R.id.scanButton)).setText(value ? Constants.STOP_SCANNING : "Find paired BBC micro:bits");
    }

    @Override
    public void candidateBleDevice(final BluetoothDevice device, byte[] scan_record, int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ble_device_list_adapter.addDevice(device);
                ble_device_list_adapter.notifyDataSetChanged();
                device_count++;
            }
        });
    }

    @Override
    public void scanningStarted() {
        setScanState(true);
        showMsg(Utility.htmlColorGreen(getScanningMessage()));
    }

    @Override
    public void scanningStopped() {
        setScanState(false);
        if (device_count > 0) {
            showMsg(Utility.htmlColorGreen("Ready"));
        } else {
            showMsg(Utility.htmlColorRed(getNoneFoundMessage()));
        }
    }
    // adaptor
    private class ListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> ble_devices;

        public ListAdapter() {
            super();
            ble_devices = new ArrayList<BluetoothDevice>();

        }

        public void addDevice(BluetoothDevice device) {
            if (!ble_devices.contains(device)) {
                ble_devices.add(device);
            }
        }

        public boolean contains(BluetoothDevice device) {
            return ble_devices.contains(device);
        }

        public BluetoothDevice getDevice(int position) {
            return ble_devices.get(position);
        }

        public void clear() {
            ble_devices.clear();
        }

        @Override
        public int getCount() {
            return ble_devices.size();
        }

        @Override
        public Object getItem(int i) {
            return ble_devices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = MainActivity.this.getLayoutInflater().inflate(
                        R.layout.list_row, null);
                viewHolder = new ViewHolder();
                viewHolder.text = view.findViewById(R.id.textView);
                viewHolder.bdaddr = view.findViewById(R.id.bdaddr);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = ble_devices.get(i);
            String deviceName = device.getName();
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                deviceName = deviceName + " (Gekoppeld)";
            }
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.text.setText(deviceName);
            else
                viewHolder.text.setText("Onbekend apparaat");

            viewHolder.bdaddr.setText(device.getAddress());

            return view;
        }
    }


    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                            showMsg(Utility.htmlColorRed("Device was not paired successfully"));
                        } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                            showMsg(Utility.htmlColorGreen("Pairing is in progress"));
                        } else {
                            showMsg(Utility.htmlColorGreen("Device was paired successfully - select it now"));
                        }
                }
            }
        };

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) MainActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    private String getScanningMessage() {
        if (Settings.getInstance().isFilter_unpaired_devices()) {
            return "Scannen voor gekoppelde micro:bits";
        } else {
            return "Scannen voor alle micro:bits";

        }
    }

    private void setButtonText() {
        String text="";
        if (Settings.getInstance().isFilter_unpaired_devices()) {
            text = Constants.FIND_PAIRED;
        } else {
            text = Constants.FIND_ANY;
        }
        final String button_text = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) MainActivity.this.findViewById(R.id.scanButton)).setText(button_text);
            }
        });

    }

    private String getNoneFoundMessage() {
        if (Settings.getInstance().isFilter_unpaired_devices()) {
            return Constants.NO_PAIRED_FOUND;
        } else {
            return Constants.NONE_FOUND;
        }
    }

    private void nextActivity(){
        new downloadAndContinue ().execute ();
    }


    public class downloadAndContinue extends AsyncTask<Void, Void, JSONObject> {
        APICommunicator apiCommunicator = APICommunicator.getInstance();

        @Override
        protected JSONObject doInBackground(Void... params) {
            String stringAnswer = apiCommunicator.StringGet ("games.json");
            if ("-".equals (stringAnswer)) stringAnswer = "{\"er\":true,\"et\":\"-\"}";
            JSONObject answer;
            try {
                answer = new JSONObject (stringAnswer);
            } catch (JSONException e) {
                e.printStackTrace ();
                answer = new JSONObject ();
            }

            return answer;
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            try {
                if (!result.getBoolean ("er")) {
                    new GameAsyncTask(getBaseContext()).deleteAllGames();
                    JSONArray jsonArray = result.getJSONArray ("games");

                    for (int i = 0; i < jsonArray.length (); i++) {

                        if (!new downloadFiles ().execute (jsonArray.getJSONObject(i)).get())
                            throw new Exception();

                    }

                    try {
                        MainActivity.this.unregisterReceiver(broadcastReceiver);
                    } catch (Exception e) {
                        // ignore!
                    }
                    if (toast != null) {
                        toast.cancel();
                    }
                    MicroBit microbit = MicroBit.getInstance();
                    microbit.setBluetooth_device(device);
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    intent.putExtra(MenuActivity.EXTRA_NAME, device.getName());
                    intent.putExtra(MenuActivity.EXTRA_ID, device.getAddress());
                    startActivity(intent);
                } else {
                    Log.d(Constants.TAG, "Failed to download games: unknown reason");
                }

            } catch (JSONException e) {
                e.printStackTrace ();
                Log.d(Constants.TAG, "Failed to download files: JSON is corrupt!");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(Constants.TAG, "Failed to download files: fault in installing a game.");
            }
        }
    }

    public class downloadFiles extends AsyncTask<JSONObject, Void, Boolean> {
        APICommunicator apiCommunicator = APICommunicator.getInstance();

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            try {
                JSONObject obj = params[0];
                String content = apiCommunicator.StringGet(obj.getString("file"));

                if ("-".equals (content)) return false;

                content = contentModifier(content);

                if (Utils.safeFile(MainActivity.this, content, obj.getString ("file"))){
                    GameModel gameModel = new GameModel(
                            obj.getInt("id"),
                            obj.getString("name"),
                            obj.getString("file"),
                            obj.getString("description"),
                            obj.getString("icon"),
                            obj.getString("color"),
                            obj.getInt("max_spelers"));
                    //new GameAsyncTask(getBaseContext()).addGame(gameModel);
                    AppDatabase.getDatabase(MainActivity.this).gameModel().addGame(gameModel);
                    Log.d(Constants.TAG, "Installed game: " + gameModel.getName());

                    return true;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean succes) {
            if (!succes) Log.d(Constants.TAG, "Failed to install game");
            super.onPostExecute(succes);
        }
    }

    private String contentModifier(String content){
        content = content.replace("from microbit import *","import android \n" +
                "droid = android.Android()\n" +
                "droid.makeToast(\"Spel gestart.\")");

        if (!content.contains("import time") & !content.contains("from time import")) content = "import time\n" + content;

        /*BUTTONS*/
        content = content.replace("button_a.''(()","droid.button_a_get_presses().result");
        content = content.replace("button_b.get_presses()","droid.button_b_get_presses().result");

        content = content.replace("button_a.is_pressed()","droid.button_a_is_pressed().result");
        content = content.replace("button_b.is_pressed()","droid.button_b_is_pressed().result");

        content = content.replace("button_a.was_pressed()","droid.button_a_was_pressed().result");
        content = content.replace("button_b.was_pressed()","droid.button_b_was_pressed().result");


        /*DISPLAY*/
        content = content.replaceAll("(\\s*?)display\\.scroll\\(\\\"(.*?)\\\"\\)","$1droid.display_scroll(\"$2\").result$1#time.sleep(round(len(\"$2\")*13/10))");
        content = content.replaceAll("(\\s*?)display\\.scroll\\(\\'(.*?)\\'\\)","$1droid.display_scroll(\'$2\').result$1#time.sleep(round(len(\'$2\')*13/10))");
        content = content.replaceAll("(\\s*?)display\\.scroll\\((.*?)\\)","$1droid.display_scroll_int($2).result$1#time.sleep(round(len(\"$2\")*13/10))");

        content = content.replaceAll("(\\s*?)display\\.set_pixel\\((.*?),(.*?),(.*?)\\)","$1droid.display_set_pixel($2,$3,$4).result");

        content = content.replaceAll("(\\s*?)display\\.show\\((.*?)\\)","$1droid.display_show($2).result");


        /*IMAGE*/
        content = content.replaceAll("\\s*?Image\\(\\\"(.*?)\\:(?:\\s*?)\"*?\\n*\\s*\"*(.*?)\\:(?:\\s*?)\"*\\n*\\s*\"*(.*?)\\:(?:\\s*?)\"*\\n*\\s*\"*(.*?)\\:(?:\\s*?)\"*\\n*\\s*\"*(.*)\\:\"\\)","\"$1$2$3$4$5\"");
        content = content.replaceAll("\\s*?Image\\(\\\'(.*?)\\:(?:\\s*?)\'*?\\n*\\s*\'*(.*?)\\:(?:\\s*?)\'*\\n*\\s*\'*(.*?)\\:(?:\\s*?)\'*\\n*\\s*\'*(.*?)\\:(?:\\s*?)\'*\\n*\\s*\'*(.*)\\:\"\\)","\'$1$2$3$4$5\'");

        Log.d("micro:games",content);

        return content;
    }

}
