package com.basetentwo.smstohttp;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;

public class ParseBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "ParseBroadcastReceiver";

	private void sendSMS(String phoneNumber, String message, Context context)
	{
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
 
        PendingIntent sentPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0,
            new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
        context.getApplicationContext().registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "Sending SMS: SMS sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.e(TAG, "Sending SMS: Generic SMS Sending failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.e(TAG, "Sending SMS: No Service SMS Failure");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.e(TAG, "Sending SMS: No PDU Failure");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.e(TAG, "Sending SMS: RADIO OFF Failure");
                        break;
                }
            }
        }, new IntentFilter(SENT));
 
        //---when the SMS has been delivered---
        context.getApplicationContext().registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "SMS Delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                    	Log.i(TAG, "Sending SMS CANCELLED");
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED)); 
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
      sendSMS(json.getString("number"), json.getString("message"), context.getApplicationContext());
    } catch (JSONException e) {
      Log.d(TAG, "JSONException: " + e.getMessage());
    }
  }
}