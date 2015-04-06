package com.example.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
    private static String LOG_TAG = MainActivity.class.getName();

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG_TAG,"--------------onStop-----------------");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(LOG_TAG,"--------------onPause-----------------");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LOG_TAG,"--------------onStart-----------------");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(LOG_TAG,"--------------onDestroy-----------------");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(LOG_TAG,"--------------onCreate----------------");
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else {
            if (id == R.id.action_location) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String location = prefs.getString(getString(R.string.pref_location_key),"14615");
                if (null == location) {
                    Log.e(LOG_TAG, "unable to get location preference");
                    return false;
                }
                //Uri query = Uri.parse("geo:43.161030,-77.610922?z=10");
                Uri query = Uri.parse("geo:0,0?z=10&q="+location);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(query);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }else {
                    Log.e(LOG_TAG,"Error Device Does Not Support this intent");
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
