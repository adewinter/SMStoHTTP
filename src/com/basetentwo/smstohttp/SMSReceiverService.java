package com.basetentwo.smstohttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.anim;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiverService extends IntentService {
	
	public static final String tag = "SMSReceiverService";
	
	/** 
	* A constructor is required, and must call the super IntentService(String)
	* constructor with a name for the worker thread.
	*/
	public SMSReceiverService() {
		super("SMSReceiverService");
		Log.e(tag, "SMSReceiverService constructor called!");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e(tag, "In handle intent!");
		String addr = intent.getExtras().getString("SMSAddress");
		String message = intent.getExtras().getString("SMSMessage");
		long timestamp = intent.getExtras().getLong("SMSTimestamp");
		try{
			this.postData(addr, message);
		} catch (JSONException jse) {
			jse.printStackTrace();
		}
	}
	
    public void postData(String number, String message) throws JSONException{  
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("https://base102.net/postsms/");
        JSONObject json = new JSONObject();
 
        try {
            // JSON data:
            json.put("message", message);
            json.put("number", number);
 
            JSONArray postjson = new JSONArray();
            postjson.put(json);
 
            // Post the data:
            httppost.setHeader("json",json.toString());
            httppost.getParams().setParameter("jsonpost",postjson);
            httppost.setEntity(new StringEntity(json.toString()));
 
            // Execute HTTP Post Request
            Log.d(tag, "JSON Data to be sent:" + json);
            HttpResponse response = httpclient.execute(httppost);
            Log.d(tag, "RESPONSE RECEIVED:" + response);
            // for JSON:
            if(response != null)
            {
                InputStream is = response.getEntity().getContent();
 
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
 
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    Log.d(tag,"Full JSON Response" + sb);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
 
        }catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }
}
