package com.example.myfirstapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BootCompleteReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			
			IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		    HeadsetStateReceiver receiver = new HeadsetStateReceiver();
		    context.registerReceiver( receiver, receiverFilter );
			
		}

}
