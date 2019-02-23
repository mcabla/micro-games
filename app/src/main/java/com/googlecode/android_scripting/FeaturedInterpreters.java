// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting;

import android.content.Context;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeaturedInterpreters {
  private static final Map<String, FeaturedInterpreter> mNameMap =
      new HashMap<>();
  private static final Map<String, FeaturedInterpreter> mExtensionMap =
      new HashMap<>();

  static {
    try {
      FeaturedInterpreter interpreters[] =
          {
            new FeaturedInterpreter("Python 3.6.4", ".py",
                "https://github.com/kuri65536/python-for-android/releases/download/r32/Python3ForAndroid-debug.apk")};
      for (FeaturedInterpreter interpreter : interpreters) {
        mNameMap.put(interpreter.mmName, interpreter);
        mExtensionMap.put(interpreter.mmExtension, interpreter);
      }
    } catch (MalformedURLException e) {
      Log.e(e);
    }
  }

  public static List<String> getList() {
    ArrayList<String> list = new ArrayList<>(mNameMap.keySet());
    Collections.sort(list);
    return list;
  }

  public static URL getUrlForName(String name) {
    if (!mNameMap.containsKey(name)) {
      return null;
    }
    return mNameMap.get(name).mmUrl;
  }

  public static String getInterpreterNameForScript(String fileName) {
    String extension = getExtension(fileName);
    if (extension == null || !mExtensionMap.containsKey(extension)) {
      return null;
    }
    return mExtensionMap.get(extension).mmName;
  }

  public static boolean isSupported(String fileName) {
    String extension = getExtension(fileName);
    return (extension != null) && (mExtensionMap.containsKey(extension));
  }

  public static int getInterpreterIcon(Context context, String key) {
    String packageName = context.getPackageName();
    String name = "_icon";
    if (key.contains(".")) {
      name = key.substring(key.lastIndexOf('.') + 1) + name;
    } else {
      name = key + name;
    }
    return context.getResources().getIdentifier(name, "drawable", packageName);
  }

  private static String getExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex == -1) {
      return null;
    }
    return fileName.substring(dotIndex);
  }

  private static class FeaturedInterpreter {
    private final String mmName;
    private final String mmExtension;
    private final URL mmUrl;

    private FeaturedInterpreter(String name, String extension, String url)
        throws MalformedURLException {
      mmName = name;
      mmExtension = extension;
      mmUrl = new URL(url);
    }
  }

}
