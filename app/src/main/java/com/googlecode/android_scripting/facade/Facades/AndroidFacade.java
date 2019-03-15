/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting.facade.Facades;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.Toast;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.NotificationIdFactory;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.mcabla.microbit.game.NotificationHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Some general purpose Android routines.<br>
 * <h2>Intents</h2> Intents are returned as a map, in the following form:<br>
 * <ul>
 * <li><b>action</b> - action.
 * <li><b>data</b> - url
 * <li><b>type</b> - mime type
 * <li><b>packagename</b> - name of package. If used, requires classname to be useful (optional)
 * <li><b>classname</b> - name of class. If used, requires packagename to be useful (optional)
 * <li><b>categories</b> - list of categories
 * <li><b>extras</b> - map of extras
 * <li><b>flags</b> - integer flags.
 * </ul>
 * <br>
 * An intent can be built using the {@see #makeIntent} call, but can also be constructed exterally.
 * 
 */
public class AndroidFacade extends RpcReceiver {
  /**
   * An instance of this interface is passed to the facade. From this object, the resource IDs can
   * be obtained.
   */
  public interface Resources {
    int getLogo48();
  }

  private final Service mService;
  private final Handler mHandler;

  private final Vibrator mVibrator;

  @Override
  public void shutdown() {
  }

  public AndroidFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mHandler = new Handler(mService.getMainLooper());
    mVibrator = (Vibrator) mService.getSystemService(Context.VIBRATOR_SERVICE);
  }

  /**
   * Creates a new AndroidFacade that simplifies the interface to various Android APIs.
   * 
   * @ param service
   *          is the {@link Context} the APIs will run under
   */


  // TODO(damonkohler): Pull this out into proper argument deserialization and support
  // complex/nested types being passed in.
  public static void putExtrasFromJsonObject(JSONObject extras, Intent intent) throws JSONException {
    JSONArray names = extras.names();
    for (int i = 0; i < names.length(); i++) {
      String name = names.getString(i);
      Object data = extras.get(name);
      if (data == null) {
        continue;
      }
      if (data instanceof Integer) {
        intent.putExtra(name, (Integer) data);
      }
      if (data instanceof Float) {
        intent.putExtra(name, (Float) data);
      }
      if (data instanceof Double) {
        intent.putExtra(name, (Double) data);
      }
      if (data instanceof Long) {
        intent.putExtra(name, (Long) data);
      }
      if (data instanceof String) {
        intent.putExtra(name, (String) data);
      }
      if (data instanceof Boolean) {
        intent.putExtra(name, (Boolean) data);
      }
      // Nested JSONObject
      if (data instanceof JSONObject) {
        Bundle nestedBundle = new Bundle();
        intent.putExtra(name, nestedBundle);
        putNestedJSONObject((JSONObject) data, nestedBundle);
      }
      // Nested JSONArray. Doesn't support mixed types in single array
      if (data instanceof JSONArray) {
        // Empty array. No way to tell what type of data to pass on, so skipping
        if (((JSONArray) data).length() == 0) {
          Log.e("Empty array not supported in JSONObject, skipping");
          continue;
        }
        // Integer
        if (((JSONArray) data).get(0) instanceof Integer) {
          Integer[] integerArrayData = new Integer[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            integerArrayData[j] = ((JSONArray) data).getInt(j);
          }
          intent.putExtra(name, integerArrayData);
        }
        // Double
        if (((JSONArray) data).get(0) instanceof Double) {
          Double[] doubleArrayData = new Double[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            doubleArrayData[j] = ((JSONArray) data).getDouble(j);
          }
          intent.putExtra(name, doubleArrayData);
        }
        // Long
        if (((JSONArray) data).get(0) instanceof Long) {
          Long[] longArrayData = new Long[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            longArrayData[j] = ((JSONArray) data).getLong(j);
          }
          intent.putExtra(name, longArrayData);
        }
        // String
        if (((JSONArray) data).get(0) instanceof String) {
          String[] stringArrayData = new String[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            stringArrayData[j] = ((JSONArray) data).getString(j);
          }
          intent.putExtra(name, stringArrayData);
        }
        // Boolean
        if (((JSONArray) data).get(0) instanceof Boolean) {
          Boolean[] booleanArrayData = new Boolean[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            booleanArrayData[j] = ((JSONArray) data).getBoolean(j);
          }
          intent.putExtra(name, booleanArrayData);
        }
      }
    }
  }

  // Contributed by Emmanuel T
  // Nested Array handling contributed by Sergey Zelenev
  private static void putNestedJSONObject(JSONObject jsonObject, Bundle bundle)
      throws JSONException {
    JSONArray names = jsonObject.names();
    for (int i = 0; i < names.length(); i++) {
      String name = names.getString(i);
      Object data = jsonObject.get(name);
      if (data == null) {
        continue;
      }
      if (data instanceof Integer) {
        bundle.putInt(name, ((Integer) data).intValue());
      }
      if (data instanceof Float) {
        bundle.putFloat(name, ((Float) data).floatValue());
      }
      if (data instanceof Double) {
        bundle.putDouble(name, ((Double) data).doubleValue());
      }
      if (data instanceof Long) {
        bundle.putLong(name, ((Long) data).longValue());
      }
      if (data instanceof String) {
        bundle.putString(name, (String) data);
      }
      if (data instanceof Boolean) {
        bundle.putBoolean(name, ((Boolean) data).booleanValue());
      }
      // Nested JSONObject
      if (data instanceof JSONObject) {
        Bundle nestedBundle = new Bundle();
        bundle.putBundle(name, nestedBundle);
        putNestedJSONObject((JSONObject) data, nestedBundle);
      }
      // Nested JSONArray. Doesn't support mixed types in single array
      if (data instanceof JSONArray) {
        // Empty array. No way to tell what type of data to pass on, so skipping
        if (((JSONArray) data).length() == 0) {
          Log.e("Empty array not supported in nested JSONObject, skipping");
          continue;
        }
        // Integer
        if (((JSONArray) data).get(0) instanceof Integer) {
          int[] integerArrayData = new int[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            integerArrayData[j] = ((JSONArray) data).getInt(j);
          }
          bundle.putIntArray(name, integerArrayData);
        }
        // Double
        if (((JSONArray) data).get(0) instanceof Double) {
          double[] doubleArrayData = new double[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            doubleArrayData[j] = ((JSONArray) data).getDouble(j);
          }
          bundle.putDoubleArray(name, doubleArrayData);
        }
        // Long
        if (((JSONArray) data).get(0) instanceof Long) {
          long[] longArrayData = new long[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            longArrayData[j] = ((JSONArray) data).getLong(j);
          }
          bundle.putLongArray(name, longArrayData);
        }
        // String
        if (((JSONArray) data).get(0) instanceof String) {
          String[] stringArrayData = new String[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            stringArrayData[j] = ((JSONArray) data).getString(j);
          }
          bundle.putStringArray(name, stringArrayData);
        }
        // Boolean
        if (((JSONArray) data).get(0) instanceof Boolean) {
          boolean[] booleanArrayData = new boolean[((JSONArray) data).length()];
          for (int j = 0; j < ((JSONArray) data).length(); ++j) {
            booleanArrayData[j] = ((JSONArray) data).getBoolean(j);
          }
          bundle.putBooleanArray(name, booleanArrayData);
        }
      }
    }
  }

  @Rpc(description = "Vibrates the phone or a specified duration in milliseconds.")
  public void vibrate(
      @RpcParameter(name = "duration", description = "duration in milliseconds") @RpcDefault("300") Integer duration) {
    mVibrator.vibrate(duration);
  }

  @Rpc(description = "Displays a short-duration Toast notification.")
  public void makeToast(@RpcParameter(name = "message") final String message) {
    mHandler.post(new Runnable() {
      public void run() {
        Toast.makeText(mService, message, Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Rpc(description = "Displays a short-duration Toast notification. And writes message to logcat.")
  public void makeLog(@RpcParameter(name = "message") final String message) {
    Log.d(message);
  }

  @Rpc(description = "Displays a notification that will be canceled when the user clicks on it.")
  public void notify(@RpcParameter(name = "title", description = "title") String title,
      @RpcParameter(name = "message") String message) {
    // This contentIntent is a noop.
    PendingIntent contentIntent = PendingIntent.getService(mService, 0, null, 0);
    NotificationHelper notificationHelper = new NotificationHelper (mService);

    Notification.Builder notificationBuilder = notificationHelper.getNotification1 (title, message, contentIntent);

    // Get a unique notification id from the application.
    final int notificationId = NotificationIdFactory.create();
    notificationHelper.notify(notificationId, notificationBuilder);
  }

  @Rpc(description = "Returns package version code.")
  public int getPackageVersionCode(@RpcParameter(name = "packageName") final String packageName) {
    int result = -1;
    PackageInfo pInfo = null;
    try {
      pInfo =
          mService.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      pInfo = null;
    }
    if (pInfo != null) {
      result = pInfo.versionCode;
    }
    return result;
  }

  @Rpc(description = "Writes message to logcat.")
  public void log(@RpcParameter(name = "message") String message) {
    android.util.Log.v("SCRIPT", message);
  }
}
