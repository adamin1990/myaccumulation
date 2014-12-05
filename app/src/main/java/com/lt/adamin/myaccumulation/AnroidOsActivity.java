package com.lt.adamin.myaccumulation;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class AnroidOsActivity extends ActionBarActivity {
    public static   String ANDROID_OS_VERSION=null;
    static String ANDROID_OS_MODEL=null;
    static  String ANDROID_OS_MANUFACTURER;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anroid_os);
     ANDROID_OS_VERSION= Build.VERSION.RELEASE;      //操作系统版本  例如4.2.2
     ANDROID_OS_MODEL= Build.MODEL;   //手机型号  例如 H30-TOO
        ANDROID_OS_MANUFACTURER= Build.MANUFACTURER;   //制造商  例如 HUAWEI  /Samsung
        PackageManager packageManager=getApplicationContext().getPackageManager();
        try {
            PackageInfo packageInfo=packageManager.getPackageInfo(getApplicationContext().getPackageName(),1);
            Log.d("versionname",packageInfo.versionName);   //versionname
            Log.d("versioncode",packageInfo.versionCode+"");   //version code
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_anroid_os, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
