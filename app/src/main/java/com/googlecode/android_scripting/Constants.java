/*
 * Copyright (C) 2016 Google Inc.
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

package com.googlecode.android_scripting;

import android.content.ComponentName;

public interface Constants {

  public static final String ACTION_LAUNCH_FOREGROUND_SCRIPT =
      "com.mcabla.microbit.game.action.LAUNCH_FOREGROUND_SCRIPT";
  public static final String ACTION_LAUNCH_BACKGROUND_SCRIPT =
      "com.mcabla.microbit.game.action.LAUNCH_BACKGROUND_SCRIPT";
  public static final String ACTION_LAUNCH_SCRIPT_FOR_RESULT =
      "com.mcabla.microbit.game.action.ACTION_LAUNCH_SCRIPT_FOR_RESULT";
  public static final String ACTION_LAUNCH_INTERPRETER =
      "com.mcabla.microbit.game.action.LAUNCH_INTERPRETER";
  public static final String ACTION_EDIT_SCRIPT =
      "com.mcabla.microbit.game.action.EDIT_SCRIPT";
  public static final String ACTION_SAVE_SCRIPT =
      "com.mcabla.microbit.game.action.SAVE_SCRIPT";
  public static final String ACTION_SAVE_AND_RUN_SCRIPT =
      "com.mcabla.microbit.game.action.SAVE_AND_RUN_SCRIPT";
  public static final String ACTION_KILL_PROCESS =
      "com.mcabla.microbit.game.action.KILL_PROCESS";
  public static final String ACTION_KILL_ALL = "com.mcabla.microbit.game.action.KILL_ALL";
  public static final String ACTION_SHOW_RUNNING_SCRIPTS =
      "com.mcabla.microbit.game.action.SHOW_RUNNING_SCRIPTS";
  public static final String ACTION_CANCEL_NOTIFICATION =
      "com.mcabla.microbit.game.action.CANCEL_NOTIFICAITON";
  public static final String ACTION_ACTIVITY_RESULT =
      "com.mcabla.microbit.game.action.ACTIVITY_RESULT";
  public static final String ACTION_LAUNCH_SERVER =
      "com.mcabla.microbit.game.action.LAUNCH_SERVER";

  public static final String EXTRA_RESULT = "SCRIPT_RESULT";
  public static final String EXTRA_SCRIPT_PATH =
      "com.mcabla.microbit.game.extra.SCRIPT_PATH";
  public static final String EXTRA_SCRIPT_CONTENT =
      "com.mcabla.microbit.game.extra.SCRIPT_CONTENT";
  public static final String EXTRA_INTERPRETER_NAME =
      "com.mcabla.microbit.game.extra.INTERPRETER_NAME";

  public static final String EXTRA_USE_EXTERNAL_IP =
      "com.mcabla.microbit.game.extra.USE_PUBLIC_IP";
  public static final String EXTRA_USE_SERVICE_PORT =
      "com.mcabla.microbit.game.extra.USE_SERVICE_PORT";
  public static final String EXTRA_SCRIPT_TEXT =
      "com.mcabla.microbit.game.extra.SCRIPT_TEXT";
  public static final String EXTRA_RPC_HELP_TEXT =
      "com.mcabla.microbit.game.extra.RPC_HELP_TEXT";
  public static final String EXTRA_API_PROMPT_RPC_NAME =
      "com.mcabla.microbit.game.extra.API_PROMPT_RPC_NAME";
  public static final String EXTRA_API_PROMPT_VALUES =
      "com.mcabla.microbit.game.extra.API_PROMPT_VALUES";
  public static final String EXTRA_PROXY_PORT = "com.mcabla.microbit.game.extra.PROXY_PORT";
  public static final String EXTRA_PROCESS_ID =
      "com.mcabla.microbit.game.extra.SCRIPT_PROCESS_ID";
  public static final String EXTRA_IS_NEW_SCRIPT =
      "com.mcabla.microbit.game.extra.IS_NEW_SCRIPT";
  public static final String EXTRA_TRIGGER_ID =
      "com.mcabla.microbit.game.extra.EXTRA_TRIGGER_ID";
  public static final String EXTRA_LAUNCH_IN_BACKGROUND =
      "com.mcabla.microbit.game.extra.EXTRA_LAUNCH_IN_BACKGROUND";
  public static final String EXTRA_TASK_ID = "com.mcabla.microbit.game.extra.EXTRA_TASK_ID";

  // BluetoothDeviceManager
  public static final String EXTRA_DEVICE_ADDRESS =
      "com.mcabla.microbit.game.extra.device_address";

  public static final ComponentName SL4A_SERVICE_COMPONENT_NAME = new ComponentName(
      "com.googlecode.android_scripting",
      "com.googlecode.android_scripting.activity.ScriptingLayerService");
  public static final ComponentName SL4A_SERVICE_LAUNCHER_COMPONENT_NAME = new ComponentName(
      "com.googlecode.android_scripting",
      "com.googlecode.android_scripting.activity.ScriptingLayerServiceLauncher");
  public static final ComponentName BLUETOOTH_DEVICE_LIST_COMPONENT_NAME = new ComponentName(
      "com.googlecode.android_scripting",
      "com.googlecode.android_scripting.activity.BluetoothDeviceList");
  public static final ComponentName TRIGGER_SERVICE_COMPONENT_NAME = new ComponentName(
      "com.googlecode.android_scripting",
      "com.googlecode.android_scripting.activity.TriggerService");

  // Preference Keys

  public static final String FORCE_BROWSER = "helpForceBrowser";
  public final static String HIDE_NOTIFY = "hideServiceNotifications";
}
