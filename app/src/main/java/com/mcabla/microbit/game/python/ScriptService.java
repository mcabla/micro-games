/*
 * Copyright (C) 2010 Google Inc.
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

/*
 * Copyright (C) 2012, Anthony Prieur & Daniel Oppenheim. All rights reserved.
 *
 * Original from SL4A modified to allow to embed Interpreter and scripts into an APK
 */

package com.mcabla.microbit.game.python;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.mcabla.microbit.game.Constants;
import com.mcabla.microbit.game.NotificationHelper;
import com.mcabla.microbit.game.R;
import com.mcabla.microbit.game.python.config.GlobalConstants;
import com.mcabla.microbit.game.python.process.MyScriptProcess;
import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.ForegroundService;
import com.googlecode.android_scripting.NotificationIdFactory;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.mcabla.microbit.game.scripts.Room.GameAsyncTask;
import com.mcabla.microbit.game.scripts.Room.GameModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class ScriptService extends ForegroundService {
	private final static int NOTIFICATION_ID = NotificationIdFactory.create();
	private final CountDownLatch mLatch = new CountDownLatch(1);
	private final IBinder mBinder;
	private MyScriptProcess myScriptProcess;

	private int script_id = 0;
	private GameModel gameModel;

	private static ScriptService instance;
	private boolean killMe;
	  
	private InterpreterConfiguration mInterpreterConfiguration = null;
	private RpcReceiverManager mFacadeManager;
    private AndroidProxy mProxy;
    
    private static Context context = null;
    static {
      instance = null;
    }

	public class LocalBinder extends Binder {
		public ScriptService getService() {
			return ScriptService.this;
		}
	}

	public ScriptService() {
		super(NOTIFICATION_ID);
		mBinder = new LocalBinder();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    public static Context getAppContext() {
        return ScriptService.context;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		ScriptService.context = getApplicationContext();
	}

	@Override
	public void onDestroy(){
		killProcess();
    	super.onDestroy();
	}

	private void killProcess() {
		this.killMe = true;
	    instance = null;
	    if (myScriptProcess != null) {
	    	myScriptProcess.kill();
	    }
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
    	handleStart(intent,startId);
    	return START_STICKY;
	}

	public void handleStart(Intent intent, final int startId){
		killProcess();

		instance = this;
		this.killMe = false;


        script_id = intent.getIntExtra("id",0);
        try {
            gameModel = new GameAsyncTask(context).getGame(script_id);
            new startMyAsyncTask().execute();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

	  public class startMyAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
		   @Override
		   protected void onPreExecute() {
		   }
	
		   @Override
		   protected Boolean doInBackground(Integer... params) {	    
			   startMyMain();
			   
			   // TODO
			   return true;
		   }
	
		   @Override
		   protected void onProgressUpdate(Integer... values) {
		   }
	
		   @Override
		   protected void onPostExecute(Boolean installStatus) {
		   }
	   
	  }

	private void startMyMain() {
		String scriptName;
		if (script_id == 0) scriptName = GlobalConstants.PYTHON_MAIN_SCRIPT_NAME;
		else{
			try {
				scriptName = gameModel.getFilename();
			} catch (Exception e) {
				e.printStackTrace();
				scriptName = GlobalConstants.PYTHON_MAIN_SCRIPT_NAME;
				stopSelf();

			}
		}

		scriptName = this.getFilesDir().getAbsolutePath() + "/" + scriptName;
		File script = new File(scriptName);

		if (!script.exists()){
			Log.d(Constants.TAG, "NO SCRIPT TO LAUNCH!");
			stopSelf();
		}
		
		// arguments
		ArrayList<String> args = new ArrayList<>();
		args.add(scriptName);
		args.add("--foreground");

		File pythonBinary = new File(this.getFilesDir().getAbsolutePath() + "/python3/bin/python3");

		// env var
		Map<String, String> environmentVariables;
		environmentVariables = new HashMap<>();
		environmentVariables.put("PYTHONPATH", this.getFilesDir().getAbsolutePath() + "/extras/python3" + ":" + this.getFilesDir().getAbsolutePath() + "/python3/lib/python3.2/lib-dynload" + ":" + this.getFilesDir().getAbsolutePath() + "/python3/lib/python3.2");
		environmentVariables.put("TEMP", this.getFilesDir().getAbsolutePath() + "/extras/tmp");
		environmentVariables.put("PYTHONHOME", this.getFilesDir().getAbsolutePath() + "/python3");		
		environmentVariables.put("LD_LIBRARY_PATH", this.getFilesDir().getAbsolutePath() + "/python3/lib" + ":" + this.getFilesDir().getAbsolutePath() + "/python3/lib/python3.2/lib-dynload");		
		
		// launch script
		mProxy = new AndroidProxy(this, null, true);
		mProxy.startLocal();
		mLatch.countDown();
	      
		myScriptProcess = MyScriptProcess.launchScript(script, mInterpreterConfiguration, mProxy, new Runnable() {
					@Override
					public void run() {
						//mProxy.shutdown();
						//stopSelf(startId);
						
						// hard force restart
//				        if (!ScriptService.this.killMe) {
//				        	startMyMain();				        	
//				        }

					}
				}, script.getParent(),  this.getFilesDir().getAbsolutePath() + "/" + this.getPackageName(), args, environmentVariables, pythonBinary);
	}

	RpcReceiverManager getRpcReceiverManager() throws InterruptedException {
		mLatch.await();
		
		if (mFacadeManager==null) { // Facade manage may not be available on startup.
		mFacadeManager = mProxy.getRpcReceiverManagerFactory()
		.getRpcReceiverManagers().get(0);
		}
		return mFacadeManager;
	}

	@Override
	protected Notification createNotification() {
	    // This contentIntent is a noop.
	    PendingIntent contentIntent = PendingIntent.getService(this, 0, new Intent(), 0);

		NotificationHelper notificationHelper = new NotificationHelper (this);

		Notification.Builder notificationBuilder = notificationHelper.getNotification1 (this.getString(R.string.app_name), this.getString(R.string.micro_game_running), contentIntent);

		return notificationBuilder.build();
	}

	
}