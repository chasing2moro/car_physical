package com.tencent.tmgp.car_physical;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class MainInterface extends Activity {
	Activity _activity;
	private static MainInterface _instance;
	public static int GetInt(){
		return 1;
	}
	public static MainInterface instance() {
		if (_instance == null){
			_instance = new MainInterface();
		}
		return _instance;
	}
	
	MainInterface() 
    {
    	try
    	{
			Class _unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer");
	        Field _unityPlayerActivityField = _unityPlayerClass.getField("currentActivity");
	        _activity = ((Activity) _unityPlayerActivityField.get(_unityPlayerClass));
    	}
    	catch (Exception localException) 
    	{ 
    		Log.i("MainInterface", localException.getMessage());
    	}
    }
	
	public void LaunchMainActivity(){
	    Intent intent = new Intent(_activity,MainActivity.class);
	    _activity.startActivity(intent);
	}
}
