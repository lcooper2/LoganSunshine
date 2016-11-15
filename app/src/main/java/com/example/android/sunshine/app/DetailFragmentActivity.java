package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class DetailFragmentActivity extends FragmentActivity {


    public static String FORECAST_STRING_TAG = "DOGS AND CATS"; // Intent key tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_fragment_activity); // Layout of DetailFragmentActivity

        // Get the intent that started the activity and extract the string extra we need if it has the extra
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ForecastFragment.EXTRA_TEXT)) {
            String forecast_string = intent.getStringExtra(intent.EXTRA_TEXT);
            System.out.println(forecast_string);
        }
        // Get SupportFragmentManager, pass the id of the fragment layout (detail_fragment_activity.xml), and add it to the manager
        getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_layout, new DetailFragment()).commit();

    }
    // Method for ForecastFragment to call DetailFragmentActivity.
    // It packages up the forecast string into the intent.
    protected static Intent newIntent(Context packageContext, String forecast) {
        Intent intent = new Intent(packageContext, DetailFragmentActivity.class);
        intent.putExtra(FORECAST_STRING_TAG, forecast);
        return intent;
    }

}
