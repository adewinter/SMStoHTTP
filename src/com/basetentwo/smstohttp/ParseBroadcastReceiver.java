package com.basetentwo.smstohttp;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class ParseBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "ParseBroadcastReceiver";

	private void sendSMS(String phoneNumber, String message)
	{
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
		Log.d(TAG, "SMS Sent!");
	}
 
  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      String action = intent.getAction();
      String channel = intent.getExtras().getString("com.parse.Channel");
      JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
 
      Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
      Iterator itr = json.keys();
      while (itr.hasNext()) {
        String key = (String) itr.next();
        Log.d(TAG, "..." + key + " => " + json.getString(key));
      }
      
      Log.d(TAG, "Sending SMS: "+ json.getString("number") + ":: " + json.getString("message") );
      sendSMS(json.getString("number"), json.getString("message"));
    } catch (JSONException e) {
      Log.d(TAG, "JSONException: " + e.getMessage());
    }
  }
}