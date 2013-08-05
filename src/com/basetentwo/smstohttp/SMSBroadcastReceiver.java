package com.basetentwo.smstohttp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSBroadcastReceiver extends BroadcastReceiver {
	public static final String tag = "SMSBroadcastReceiver";
	@Override
	public void onReceive(Context cContext, Intent intent) {
		// TODO Auto-generated method stub
		Log.e(tag, "IN ONRECEIVE OF SMSBroadcastReceiver!");
		//Parse origin number + message from Intent;
		Log.e(tag, "Received a message in SMSBroadcastReceiver!" + intent.toString());
		Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[])bundle.get("pdus");
            final SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
            }
            for (int i=0; i<messages.length; i++){
            	SmsMessage message = messages[0];
                
                String addr = message.getOriginatingAddress();
                String body = message.getMessageBody();
                long timestamp = message.getTimestampMillis();
                Log.d(tag, "Message recieved: " + body + "Message from: " + addr);
                try{
//                	postData(addr, message.getMessageBody());
//                	Intent myIntent = new Intent(this, SMSReceiverService.class);
                	Intent myIntent = new Intent(cContext, SMSReceiverService.class);
                	myIntent.putExtra("SMSMessage", body);
                	myIntent.putExtra("SMSAddress", addr);
                	myIntent.putExtra("SMSTimestamp", timestamp);
                	cContext.startService(myIntent);
                } catch (Exception e) {
                	Log.e(tag, "Exception in attempting to POST!:" + e.getMessage());
                	e.printStackTrace();
                }
            }
        }
	}

}
