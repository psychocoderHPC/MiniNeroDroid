package com.noether.shen.mininero;


import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

public class TabbedSettingsActivity extends AppCompatActivity{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Log.d("asdf", "a1");
        SharedPreferences SP  = PreferenceManager.getDefaultSharedPreferences(TabbedSettingsActivity.this);
        String dnmode = SP.getString("dn_mode", "night");
                Log.d("asdf", "a2");
        setContentView(R.layout.tabbed_settings_one);
        Switch dn = (Switch)findViewById(R.id.daynightswitch);
                Log.d("asdf", "a3");

        if (dn!= null) {
            if (dnmode.contains("night")) {
                dn.setChecked(true);
            } else {
                dn.setChecked(false);
            }
            Log.d("asdf", "a4");

            dn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Log.d("asdf", "a5");
                    if(isChecked) {
                        //do stuff when Switch is ON

                        SharedPreferences SP  = PreferenceManager.getDefaultSharedPreferences(TabbedSettingsActivity.this);
                        SharedPreferences.Editor editor = SP.edit();
                        editor.putString("dn_mode", "night");
                        editor.commit();

                        Intent mainIntent = new Intent(TabbedSettingsActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                    } else {
                        SharedPreferences SP  = PreferenceManager.getDefaultSharedPreferences(TabbedSettingsActivity.this);
                        SharedPreferences.Editor editor = SP.edit();
                        editor.putString("dn_mode", "day");
                        editor.commit();
                        //do stuff when Switch if OFF
                        Intent mainIntent = new Intent(TabbedSettingsActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                    }
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed_settings, menu);
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

    public void saveipbuttonclick(View view) {
        EditText iptext = (EditText) findViewById(R.id.iptext);
        String ip = iptext.getText().toString();
        SharedPreferences SP  = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        SharedPreferences.Editor editor = SP.edit();
        editor.putString("mininodo_ip", ip);
        editor.commit();
        //put in settings..
    }

    public void saveapikeybuttonclick(View view) {
        EditText apikeytext = (EditText) findViewById(R.id.apikeytext);
        if (apikeytext != null) {
            //if length != 128, then it's an old or bad key..
            if (apikeytext.length() != 128) {
                String apikey = apikeytext.getText().toString();
                //query password, if they get the password correct, then save it...
                ApiKeyStore aksnew = new ApiKeyStore("Old password?", view.getContext());
                aksnew.unlockBox(aksnew.AKSSTORE, "unlock");
            } else {
                String apikey = apikeytext.getText().toString();
                ApiKeyStore aksnew = new ApiKeyStore("New Password?", view.getContext());
                aksnew.storeApiKey(TweetNaclFast.hexDecode(apikey));
            }
        }
    }

    public void gotoabout(View view) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
         View rootView = inflater.inflate(R.layout.tabbed_settings_two, null);

    }

    public void clearbuttonclick(View view) {
        ApiKeyStore aksnew = new ApiKeyStore("pass", view.getContext());
        aksnew.clearApiKey();
        EditText aket = (EditText) findViewById(R.id.apikeytext);
        if (aket != null) {
            aket.setText("");
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
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
            int section = getArguments().getInt(ARG_SECTION_NUMBER);
            Log.d("asdf", "section number is"+ Integer.toString(section));

            View rootView = inflater.inflate(R.layout.fragment_tabbed_settings, container, false);
            if (section == 1) {
                rootView = inflater.inflate(R.layout.tabbed_settings_one, container, false);

                //set ipbox to whatever was saved in there...
                SharedPreferences SP  = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
                String ip = SP.getString("mininodo_ip", "https://localhost:3000");
                String apikeyEnc = SP.getString("api_key", "");
                EditText ipbox = (EditText) rootView.findViewById(R.id.iptext);
                ipbox.setText(ip);
                 EditText apibox = (EditText) rootView.findViewById(R.id.apikeytext);
                apibox.setText(apikeyEnc);

            } else if (section == 2) {
                rootView = inflater.inflate(R.layout.tabbed_settings_two, container, false);
            } else if (section == 3) {
                rootView = inflater.inflate(R.layout.tabbed_settings_three, container, false);
            }
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            //here do things in the relevant section I guess...



            return rootView;
        }
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
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
