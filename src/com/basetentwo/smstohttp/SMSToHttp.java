package com.basetentwo.smstohttp;

import com.parse.Parse;
import com.parse.ParseInstallation;

import android.app.Application;
import android.os.StrictMode;

public class SMSToHttp extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
        Parse.initialize(this, "h6xOsjUapysYL613ph7Obx9cH3Kp81IXvNIN9FMK", "SeAOm2gyeHWonpsMuMlbX6urvkXrQZLZPYDooLlX");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
          }
	}
	
}
