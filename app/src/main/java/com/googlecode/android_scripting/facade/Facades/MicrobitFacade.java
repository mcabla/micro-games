package com.googlecode.android_scripting.facade.Facades;

import android.util.Log;

import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.mcabla.microbit.game.Constants;
import com.mcabla.microbit.game.ui.GameActivity;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

public class MicrobitFacade extends RpcReceiver {
    private static WeakReference<GameActivity> gameActivityWeakReference;
    private final CountDownLatch mOnInitLock;
    private long startTime = 0;

    public MicrobitFacade(FacadeManager manager) {
    super(manager);
    mOnInitLock = new CountDownLatch(1);
    mOnInitLock.countDown();
    startTime = Calendar.getInstance().getTimeInMillis();
    }

    public void shutdown() {
    }

    public static void updateActivity(GameActivity gameActivity){
    gameActivityWeakReference = new WeakReference<>(gameActivity);
    }


    /*---------------------------------------------*/
    /*-------------------DISPLAY-------------------*/
    /*---------------------------------------------*/
    @Rpc(description = "Print text on microbit")
    public void display_scroll(@RpcParameter(name = "message") String message) throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) gameActivityWeakReference.get().sendText(message);
    }

    @Rpc(description = "Print int on microbit")
    public void display_scroll_int(@RpcParameter(name = "message") int message) throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) gameActivityWeakReference.get().sendText(String.valueOf(message));
    }

    @Rpc(description = "Print pixel on microbit")
    public void display_set_pixel(@RpcParameter(name = "x") Integer x,
                                  @RpcParameter(name = "y") Integer y,
                                  @RpcParameter(name = "z") Integer z) throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) gameActivityWeakReference.get().sendPixel(x,y,z);
    }

    @Rpc(description = "Print image on microbit")
    public void display_show(@RpcParameter(name = "image") String img) throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) gameActivityWeakReference.get().sendImage(img);
    }

    @Rpc(description = "Print image on microbit")
    public void display_clear() throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) gameActivityWeakReference.get().sendImage("0000000000000000000000000");
    }

    /*---------------------------------------------*/
    /*-------------------BUTTONS-------------------*/
    /*---------------------------------------------*/
    @Rpc(description = "Get button a presses")
    public int button_a_get_presses() throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) return gameActivityWeakReference.get().getPresses(1);
        return 0;
    }

    @Rpc(description = "Get button b presses")
    public int button_b_get_presses() throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) return gameActivityWeakReference.get().getPresses(2);
        return 0;
    }

    @Rpc(description = "Get button a pressed")
    public Boolean button_a_is_pressed() throws InterruptedException {
        mOnInitLock.await();
        Log.d("micro:games BUTTON","" + String.valueOf(gameActivityWeakReference.get().getPressed(1)));
        if (gameActivityWeakReference != null) return gameActivityWeakReference.get().getPressed(1);
        return false;
    }

    @Rpc(description = "Get button b pressed")
    public boolean button_b_is_pressed() throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) return gameActivityWeakReference.get().getPressed(2);
        return false;
    }

    @Rpc(description = "Get button a was pressed")
    public boolean button_a_was_pressed() throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null) return gameActivityWeakReference.get().getWasPressed(1);
        return false;
    }

    @Rpc(description = "Get button b was pressed")
    public boolean button_b_was_pressed() throws InterruptedException {
        mOnInitLock.await();
        if (gameActivityWeakReference != null)
            return gameActivityWeakReference.get().getWasPressed(2);
        return false;
    }

    /*---------------------------------------------*/
    /*----------------ACCELEROMETER----------------*/
    /*---------------------------------------------*/


    /*---------------------------------------------*/
    /*---------------------MISC--------------------*/
    /*---------------------------------------------*/


    @Rpc(description = "Get time")
    public int running_time() throws InterruptedException {
        mOnInitLock.await();
        return (int)  (Calendar.getInstance().getTimeInMillis() - startTime);
    }



}