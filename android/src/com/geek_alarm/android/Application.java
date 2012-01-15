package com.geek_alarm.android;

import android.content.Context;

public class Application extends android.app.Application {

    private static Context context;

    public void onCreate(){
        context=getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
