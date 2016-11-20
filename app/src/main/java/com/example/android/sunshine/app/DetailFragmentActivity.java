package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


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
        getSupportFragmentManager().beginTransaction().add(R.id.detail_fragment_layout, new DetailFragment()).commit();


    }
    // Method for ForecastFragment to call DetailFragmentActivity.
    // It packages up the forecast string into the intent.
    protected static Intent newIntent(Context packageContext, String forecast) {
        Intent intent = new Intent(packageContext, DetailFragmentActivity.class);
        intent.putExtra(FORECAST_STRING_TAG, forecast);
        return intent;
    }

    public static class DetailFragment extends Fragment {

        private String forecastStr; // string that represents the weather forecast for the selected day

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // get the fragment's host activity, get the activity's starting intent, and extract out weather forecast
            forecastStr = getActivity().getIntent().getStringExtra(FORECAST_STRING_TAG);

        }

        @Override
        // Inflate the fragment, wire up the fragment's textView, and display the forecast string in the textView.
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.detail_fragment, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.detail_fragment_textview);
            textView.setText(forecastStr);
            return rootView;
        }
    }
}
