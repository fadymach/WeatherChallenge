package fady.weatherchallenge;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //0 = Fahrenheit
    //1 = Celsius,
    private int UNITS = 1;

    public ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager, true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //If the units are switched, then we need to recall the api to get the new data.
        if(UNITS == 1){
            UNITS = 0;
        }else{
            UNITS = 1;
        }
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        TextView textView;
        TextView tempText;
        TextView tempHigh;
        TextView tempLow;
         TextView descMain;
        TextView descSecond;
        ImageView weatherIcon;
        String iconCode;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            textView = rootView.findViewById(R.id.city_name);
            tempText = rootView.findViewById(R.id.temp);
            tempHigh = rootView.findViewById(R.id.temp_high);
            tempLow = rootView.findViewById(R.id.temp_low);
            descMain = rootView.findViewById(R.id.desc_main);
            descSecond = rootView.findViewById(R.id.desc_second);
            weatherIcon = rootView.findViewById(R.id.weather_icon);
            //Call to get the weather data
            getData();
            return rootView;
        }

        public void getData(){
            final MainActivity activity = ((MainActivity) getActivity());
            final Bundle args = getArguments();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Call the getWeather method in the MainActivity class to populate
                    //  all the weather fields
                    activity.getWeather(callback, args.getInt(ARG_SECTION_NUMBER));
                    //Call the api to get the weather icon and populate the ImageView with that
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.with(getActivity())
                                    .load(getResources().getString(R.string.icon_url, iconCode))
                                    .into(weatherIcon);
                        }
                    });
                }
            }).start();
        }
        //Define a Callback that we can use for our API call returns
        Callback callback = new Callback() {
            @Override
            public void JSONObject(final JSONObject object) {
                final MainActivity activity = (MainActivity) getActivity();
                //Set all the TextViews in the main UI thread
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject mainObject = object.getJSONObject("main");
                            JSONObject weatherObject = object.getJSONArray("weather").getJSONObject(0);
                            textView.setText(object.getString("name"));
                            tempText.setText(mainObject.getString("temp") +
                                    activity.getUnit());
                            tempHigh.setText(mainObject.getString("temp_max") + activity.getUnit());
                            tempLow.setText(mainObject.getString("temp_min") + activity.getUnit());
                            descMain.setText(weatherObject.getString("main"));
                            descSecond.setText(weatherObject.getString("description"));
                            iconCode = weatherObject.getString("icon");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

    }

    //Calls the API url to get the weather for the three cities.
    private void getWeather(final Callback callback, final int index){
        //Build the url with all the needed information.
        //These codes are the codes for each city that we need to retrieve data for
        String url = this.getResources().getString(R.string.weather_url) + "6167865,"
                + "6173331,"
                + "5913490"
                + "&APPID=" + this.getResources().getString(R.string.auth_key);
        //add the proper query parameters accoring to whether we want fahrenheit. or celsius. temp from the api
        if(UNITS == 0){
            url = url + "&units=imperial";
        }else{
            url = url + "&units=metric";
        }

        final Request request = new Request.Builder()
                .url(url)
                .build();

        final OkHttpClient client = new OkHttpClient();
        try {
            String response = client.newCall(request).execute().body().string();
            JSONObject tempObject = new JSONObject(response);
            JSONArray array = tempObject.getJSONArray("list");
            callback.JSONObject(array.getJSONObject(index));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private String getUnit(){
        if(UNITS == 1){
            //Return unicode for Celsius
            return "\u2103";
        }else{
            //Return unicode for Fahrenheit
            return "\u2109";
        }
    }

}
