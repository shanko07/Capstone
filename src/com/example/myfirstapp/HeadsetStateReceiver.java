package com.example.myfirstapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HeadsetStateReceiver extends BroadcastReceiver {

	private boolean headsetConnected = false;
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("HeadsetStateReceiver", "Headset");
		if (intent.hasExtra("state")){
	         if (headsetConnected && intent.getIntExtra("state", -1) == 0){
	             headsetConnected = false;
	             
	         } else if (!headsetConnected && intent.getIntExtra("state", -1) == 1){
	            headsetConnected = true;
	            Intent i = new Intent();
	            i.setClassName("com.example.myfirstapp", "com.example.myfirstapp.MainActivity");
	            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            context.startActivity(i);
	         }
	     }
		
	}

}


/*
<receiver android:name="HeadsetStateReceiver"
android:enabled="true"
android:exported="true" >
<intent-filter>
    <action android:name="android.intent.action.HEADSET_PLUG" >
    </action>
</intent-filter>
</receiver>
*/
