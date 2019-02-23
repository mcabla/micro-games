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

package com.googlecode.android_scripting.interpreter;

import android.os.Environment;

/**
 * A collection of constants required for installation/removal of an interpreter.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public interface InterpreterConstants {

  String SDCARD_ROOT =
      Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

  String SDCARD_SL4A_ROOT = SDCARD_ROOT + "sl4a/";

  // Interpreters discovery mechanism.
  String ACTION_DISCOVER_INTERPRETERS =
      "com.mcabla.microbit.game.DISCOVER_INTERPRETERS";

  // Interpreters broadcasts.
  String ACTION_INTERPRETER_ADDED =
      "com.mcabla.microbit.game.INTERPRETER_ADDED";
  String ACTION_INTERPRETER_REMOVED =
      "com.mcabla.microbit.game.INTERPRETER_REMOVED";

  // Interpreter content provider.
  String PROVIDER_PROPERTIES = "com.mcabla.microbit.game.base";
  String PROVIDER_ENVIRONMENT_VARIABLES =
      "com.mcabla.microbit.game.env";
  String PROVIDER_ARGUMENTS = "com.mcabla.microbit.game.args";

  String MIME = "script/";
}
