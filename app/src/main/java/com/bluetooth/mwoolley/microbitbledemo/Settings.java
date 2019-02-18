package com.bluetooth.mwoolley.microbitbledemo;
/*
 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
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
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.SeekBar;

public class Settings {

    private static Settings instance;
    private short accelerometer_period=20;
    private short scrolling_delay=500;
    private boolean filter_unpaired_devices=true;

    private static final String SETTINGS_FILE = "com.bluetooth.mwoolley.microbitbledemo.settings_file";
    private static final String ACCELEROMETER_PERIOD = "accelerometer_period";
    private static final String SCROLLING_DELAY = "scrolling_delay";
    private static final String FILTER_UNPAIRED_DEVICES = "filter_unpaired_devices";

    private Settings() {
    }

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public void save(Context context) {
        Log.d(Constants.TAG,"Saving preferences");
        SharedPreferences sharedPref = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(ACCELEROMETER_PERIOD, accelerometer_period);
        editor.putInt(SCROLLING_DELAY, scrolling_delay);
        editor.putBoolean(FILTER_UNPAIRED_DEVICES, filter_unpaired_devices);
        editor.commit();
    }


    public void restore(Context context) {
        Log.d(Constants.TAG,"Restoring preferences");
        SharedPreferences sharedPref = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        accelerometer_period = (short) sharedPref.getInt(ACCELEROMETER_PERIOD,20);
        scrolling_delay = (short)  sharedPref.getInt(SCROLLING_DELAY,500);
        filter_unpaired_devices = sharedPref.getBoolean(FILTER_UNPAIRED_DEVICES,true);
    }

    public short getAccelerometer_period() {
        return accelerometer_period;
    }

    public void setAccelerometer_period(short accelerometer_period) {
        this.accelerometer_period = accelerometer_period;
    }

    public short getScrolling_delay() {
        return scrolling_delay;
    }

    public void setScrolling_delay(short scrolling_delay) {
        this.scrolling_delay = scrolling_delay;
    }

    public boolean isFilter_unpaired_devices() {
        return filter_unpaired_devices;
    }

    public void setFilter_unpaired_devices(boolean filter_unpaired_devices) {
        this.filter_unpaired_devices = filter_unpaired_devices;
    }
}

