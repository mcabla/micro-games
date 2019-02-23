package com.googlecode.android_scripting.facade;

import android.util.Log;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.mcabla.microbit.game.python.config.GlobalConstants;
import com.mcabla.microbit.game.ui.GameActivity;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

@RpcMinSdk(4)
public class MicrobitFacade extends RpcReceiver {
  private static WeakReference<GameActivity> gameActivityWeakReference;

  private final CountDownLatch mOnInitLock;

  public MicrobitFacade(FacadeManager manager) {
    super(manager);
    mOnInitLock = new CountDownLatch(1);
    mOnInitLock.countDown();
  }

  public void shutdown() {
  }

  public static void updateActivity(GameActivity gameActivity){
    gameActivityWeakReference = new WeakReference<>(gameActivity);
  }

  // Usage example from Python code:
  // 
  // import android
  // droid = android.Android()
  // droid.aHelloFonction("hello rpc")
  @Rpc(description = "Print hello in logcat")
  public void sendText(@RpcParameter(name = "message") String message) throws InterruptedException {
    mOnInitLock.await();
    Log.i(GlobalConstants.LOG_TAG, "Microbit received: " + message);
    gameActivityWeakReference.get().sendText(message);
  }
}