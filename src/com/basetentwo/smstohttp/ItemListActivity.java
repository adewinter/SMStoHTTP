package com.basetentwo.smstohttp;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends Activity {

    private static final String TAG="SMStoHTTP";
    
    private IConnectToRabbitMQ mConsumer;
    private TextView mOutput;
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG,"In Oncreate of main activity!");
        super.onCreate(savedInstanceState);
        
        Parse.initialize(this, "h6xOsjUapysYL613ph7Obx9cH3Kp81IXvNIN9FMK", "SeAOm2gyeHWonpsMuMlbX6urvkXrQZLZPYDooLlX");
        PushService.setDefaultPushCallback(this, ItemListActivity.class);
        PushService.subscribe(this, "send_sms", ItemListActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
          }
        setContentView(R.layout.main);


        //The output TextView we'll use to display messages
        mOutput =  (TextView) findViewById(R.id.output);
 
        //Create the consumer
        mConsumer = new IConnectToRabbitMQ("base102.net",
                "aamnotifs",
                "direct",
                "adewinter",
                "qsczse12",
                5672);
 
        //Connect to broker
        mConsumer.connectToRabbitMQ();
 
        //register for messages
        mConsumer.setOnReceiveMessageHandler(new IConnectToRabbitMQ.OnReceiveMessageHandler() {
        	
        	private void sendSMS(String phoneNumber, String message)
        	{
        		SmsManager sms = SmsManager.getDefault();
        		sms.sendTextMessage(phoneNumber, null, message, null, null);
        	}
			
        	 public void onReceiveMessage(byte[] message) {
                 String text = "";
                 try {
                     text = new String(message, "UTF8");
                 } catch (UnsupportedEncodingException e) {
                     e.printStackTrace();
                 }
                 mOutput.append("\n"+text);
                 
                 try {
                	 JSONObject json = new JSONObject(text);
                	 String myMessage = json.getString("message");
                	 String myNumber = json.getString("title");
                	 mOutput.append("\n"+myMessage + ": " + myNumber);
                	 
                	 sendSMS(myNumber, myMessage);
                 } catch (JSONException jse) {
                	 jse.printStackTrace();
                 }
                 
                 
                 
             }
  
		});
    }

    
    @Override
    protected void onResume() {
        super.onPause();
        mConsumer.connectToRabbitMQ();
    }
 
    @Override
    protected void onPause() {
        super.onPause();
        mConsumer.dispose();
    }
}
