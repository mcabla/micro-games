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

package com.googlecode.android_scripting.language;

import com.googlecode.android_scripting.Log;

import java.util.HashMap;
import java.util.Map;

public class SupportedLanguages {

  private enum KnownLanguage {
    PYTHON(".py", PythonLanguage.class);

    private final String mmExtension;
    private final Class<? extends Language> mmClass;

    KnownLanguage(String ext, Class<? extends Language> clazz) {
      mmExtension = ext;
      mmClass = clazz;
    }

    private String getExtension() {
      return mmExtension;
    }

    private Class<? extends Language> getLanguageClass() {
      return mmClass;
    }
  }

  private static Map<String, Class<? extends Language>> sSupportedLanguages;

  static {
    sSupportedLanguages = new HashMap<>();
    for (KnownLanguage language : KnownLanguage.values()) {
      sSupportedLanguages.put(language.getExtension(), language.getLanguageClass());
    }
  }

  public static Language getLanguageByExtension(String extension) {
    extension = extension.toLowerCase();
    if (!extension.startsWith(".")) {
      throw new RuntimeException("Extension does not start with a dot: " + extension);
    }
    Language lang = null;

    Class<? extends Language> clazz = sSupportedLanguages.get(extension);
    if (clazz == null) {
      clazz = Language.class; // revert to default language.
    }
    if (clazz != null) {
      try {
        lang = clazz.newInstance();
      } catch (IllegalAccessException e) {
        Log.e(e);
      } catch (InstantiationException e) {
        Log.e(e);
      }
    }
    return lang;
  }

  public static boolean checkLanguageSupported(String name) {
    String extension = name.toLowerCase();
    int index = extension.lastIndexOf('.');
    if (index < 0) {
      extension = "." + extension;
    } else if (index > 0) {
      extension = extension.substring(index);
    }
    return sSupportedLanguages.containsKey(extension);
  }
}
