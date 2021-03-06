package com.noether.shen.mininero;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SQLiteDatabase sql;
    Cursor c;
    //JsonRequester jreq;
    ApiKeyStore aksnew;

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences SP  = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String dnmode = SP.getString("dn_mode", "night");
        if (dnmode.contains("night")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
             AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        aksnew = new ApiKeyStore(MainActivity.this);

        //aks = new ApiKeyStorage(getApplicationContext(), "password?"); //shouldn't do it on first app load...
        //jreq = new JsonRequester(getApplicationContext(), aks);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        //attempt to load address book..
        loadDB();

        //Attempt to load uri if it was passed:
        EditText desttext = (EditText) findViewById(R.id.destination_text);
        EditText pidtext = (EditText) findViewById(R.id.pid_text);
        EditText amtext = (EditText) findViewById(R.id.amount_text);
        try {
            Intent intent = getIntent();
            Uri openUri = intent.getData();
            Log.d("asdf", "uri data:" + openUri.toString());
            btcXmrUriParser btcXmrUri = new btcXmrUriParser(openUri.toString());
            desttext.setText(btcXmrUri.dest);
            pidtext.setText(btcXmrUri.pid);
            amtext.setText(btcXmrUri.amount);
        } catch (Exception noUriException) {
            Log.d("asdf", "no uri passed");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

         //check if there is no api key stored!..
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String encKey = SP.getString("api_key", "");
        if (encKey == "" && (id != R.id.action_settings) && (id != R.id.action_addresses)) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(MainActivity.this, "Enter api key in settings menu!", duration);
            toast.show();

        } else {

            if (id == R.id.action_settings) {
                Intent settingIntent = new Intent(this, TabbedSettingsActivity.class);
                startActivity(settingIntent);
                return true;
            }
            if (id == R.id.action_addresses) {
                try {
                    loadAddress();
                } catch (Exception dberr) {
                    showToast("unknown sqlite db error for loading saved addresses");
                }
            }
            if (id == R.id.action_balance) {
                Context context = getApplicationContext();
                if (isNetworkConnected()) {
                    try {
                        aksnew.unlockBox(aksnew.AKSBALANCE, "unlock");
                        Log.d("asdf", "here");
                        //jreq.GetTime();
                        //jreq.GetBalance();

                        //JsonRequestGetTime(); //this will be moved to app launch later, so the offset is preloaded
                        //JsonRequestGetBalance();
                    } catch (Exception e) {
                        Log.d("asdf", "is your server running");
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, "server ip error!", duration);
                        toast.show();
                    }
                } else {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, "no network!", duration);
                    toast.show();
                }
            }
            if (id == R.id.action_my_addr) {
                try {
                    //jreq.GetTime();
                    EditText desttext = (EditText) findViewById(R.id.destination_text);
                    aksnew.unlockBox(aksnew.AKSADDRESS, "unlock", desttext);
                    //jreq.GetAddress(desttext);
                    //JsonRequestGetAddress();

                } catch (Exception addrexc) {
                    Log.d("asdf", "error in getaddress");
                }
            }
            if (id == R.id.action_account) {
                //JsonRequestGetTransactions
                //display in webview
                //need to add decryption here also..
                Intent transactionsIntent = new Intent(this, TxnActivity.class);
                startActivity(transactionsIntent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //check if there is no api key stored!..
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String encKey = SP.getString("api_key", "");
        if (encKey == "" && (id != R.id.nav_manage) && (id != R.id.nav_addresses)) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(MainActivity.this, "Enter api key in settings menu!", duration);
            toast.show();

        } else {

            if (id == R.id.nav_manage) {
                Intent settingIntent = new Intent(this, TabbedSettingsActivity.class);
                startActivity(settingIntent);
            }
            if (id == R.id.nav_addresses) {
                //            Intent settingIntent = new Intent(this, ItemListActivity.class);
                //startActivity(settingIntent);

                try {
                    loadAddress();
                } catch (Exception dberr) {
                    showToast("unknown sqlite db error for loading saved addresses");
                }
            }
            if (id == R.id.nav_balance) {
                Context context = getApplicationContext();
                if (isNetworkConnected()) {
                    try {
                        aksnew.unlockBox(aksnew.AKSBALANCE, "unlock");
                    } catch (Exception e) {
                        Log.d("asdf", "is your server running");
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, "server ip error!", duration);
                        toast.show();
                    }
                } else {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, "no network!", duration);
                    toast.show();
                }
            }
            if (id == R.id.nav_my_addr) {
                try {
                    EditText desttext = (EditText) findViewById(R.id.destination_text);
                    aksnew.unlockBox(aksnew.AKSADDRESS, "unlock", desttext);
                } catch (Exception errr) {
                    Log.d("asdf", "didn't get address");
                }
            }
            if (id == R.id.nav_account) {
                //JsonRequestGetTransactions
                //display in webview
                //need to add decryption here also..
                Intent transactionsIntent = new Intent(this, TxnActivity.class);
                startActivity(transactionsIntent);
                return true;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadDB() {
        //can move this around or encrypt it later..
        Context context = getApplicationContext();
        try {
            String fileName = "SavedAddresses.db";
            //String filePath = "/storage/sdcard0/" + Environment.DIRECTORY_DOCUMENTS + File.separator + fileName;
            sql = this.openOrCreateDatabase(fileName, MODE_PRIVATE, null );
            sql.execSQL(
                    "CREATE TABLE SavedAddress(Id INTEGER PRIMARY KEY AUTOINCREMENT, Name String, address String)"
            );
        } catch (Exception dberr) {
            Log.d("asdf", "unknown db error");
            //showToast("unknown android sqlite error - unable to store or load addresses locally");
        }
    }

    public List<SavedAddress> getAddresses () {
        List<SavedAddress> addressList = new ArrayList<SavedAddress>();
        String selectQuery = "SELECT  * FROM SavedAddress";
        try {
            Cursor cursor = sql.rawQuery(selectQuery, null);
            Log.d("asdf", "after rawquery");
            if (cursor.moveToFirst()) {
                Log.d("asdf", "got move to first");
                do {
                    SavedAddress sa = new SavedAddress();
                    sa.Id = Integer.parseInt(cursor.getString(0));
                    sa.address = cursor.getString(2);
                    sa.Name = cursor.getString(1);
                    addressList.add(sa);
                } while (cursor.moveToNext());
                Log.d("asdf","loaded db!");
            } else {
                //preinitialize with creator address + monero dev address if it's empty
                //user can delete these if they want..
                showToast("failed to load db! recreating...");
                SavedAddress me = new SavedAddress();
                me.Id = 0;
                me.Name = "shen";
                me.address = "4AjCAP7WoojjdydwkgvEyxRfxHNLhxbBz4FeLug5gW4WLJ13VnhXtrW7uk5fcLKUarTVpJtcWxRheUd7etWG9c8VHwA8gFC";
                SavedAddress xmr = new SavedAddress();
                xmr.Id = 1;
                xmr.Name = "xmr dev donate";
                xmr.address = "44AFFq5kSiGBoZ4NMDwYtN18obc8AemS33DBLWs3H7otXft3XjrpDtQGv7SqSsaBYBb98uNbr2VBBEt7f2wfn3RVGQBEP3A";
                addressList.add(me);
                addressList.add(xmr);
                insertAddress(me);
                insertAddress(xmr);
            }
        } catch (Exception dberr) {
             //preinitialize with creator address + monero dev address if it's empty
            //user can delete these if they want..
            showToast("Failed to load db! recreating...");
            SavedAddress me = new SavedAddress();
            me.Id = 0;
            me.Name = "shen";
            me.address = "4AjCAP7WoojjdydwkgvEyxRfxHNLhxbBz4FeLug5gW4WLJ13VnhXtrW7uk5fcLKUarTVpJtcWxRheUd7etWG9c8VHwA8gFC";
            SavedAddress xmr = new SavedAddress();
            xmr.Id = 1;
            xmr.Name = "xmr dev donate";
            xmr.address = "44AFFq5kSiGBoZ4NMDwYtN18obc8AemS33DBLWs3H7otXft3XjrpDtQGv7SqSsaBYBb98uNbr2VBBEt7f2wfn3RVGQBEP3A";
            addressList.add(me);
            addressList.add(xmr);
            insertAddress(me);
            insertAddress(xmr);

        }
        return addressList;
    }

    public void loadAddress() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Load Address");

        final List<SavedAddress> sa = getAddresses();
        String[] names = new String[sa.size()];

        for (int i = 0; i < sa.size(); i++) {
            names[i] = sa.get(i).Name;
        }
        b.setCancelable(true);

        b.setSingleChoiceItems(names, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();
                //String dest = sa.get(which).address;
                //EditText desttext = (EditText) findViewById(R.id.destination_text);
                //desttext.setText(dest);
            }

        });
        b.setPositiveButton("Load", new DialogInterface.OnClickListener() {
            //load on tap
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lw = ((AlertDialog)dialog).getListView();
                which = lw.getCheckedItemPosition();

                dialog.dismiss();
                String dest = sa.get(which).address;
                EditText desttext = (EditText) findViewById(R.id.destination_text);
                desttext.setText(dest);
            }
        });
        b.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            //load on tap
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lw = ((AlertDialog)dialog).getListView();
                which = lw.getCheckedItemPosition();
                dialog.dismiss();
                deleteAddress(sa.get(which));
            }
        });
        b.show();
    }

    public boolean deleteAddress(SavedAddress sa) {
        return sql.delete("SavedAddress", "Id="+sa.Id,null ) > 0;
    }

    public void onScanClick(View view) {
        //new IntentIntegrator(this).initiateScan();
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
        integrator.initiateScan();
    }

    //parses scanning
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                // handle scan result as in mn uwp app...
                EditText desttext = (EditText) findViewById(R.id.destination_text);
                EditText pidtext = (EditText) findViewById(R.id.pid_text);
                EditText amtext = (EditText) findViewById(R.id.amount_text);
                String openUri = scanResult.getContents().toString();

                btcXmrUriParser btcXmrUri = new btcXmrUriParser(openUri.toString());
                if(btcXmrUri.isApiKey) {


                        //ask if you want to set api key..
                        //confirm dialog...
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Confirm");
                        alertDialog.setMessage("Do you want to set the api key/ip to scanned value? " + btcXmrUri.dest);
                        final String apikey = btcXmrUri.dest;
                        final String ip = btcXmrUri.ip;
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                //set ip
                                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = SP.edit();
                                editor.putString("mininodo_ip", ip);
                                editor.commit();

                                //set apikey

                                ApiKeyStore aksnew = new ApiKeyStore("New Password?", MainActivity.this);
                                aksnew.storeApiKey(TweetNaclFast.hexDecode(apikey));
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("asdf", "cancelled!");
                            }
                        });
                        alertDialog.show();

                } else {
                    Log.d("asdf", "amount in scan:" + btcXmrUri.amount);
                    desttext.setText(btcXmrUri.dest);
                    pidtext.setText(btcXmrUri.pid);
                    amtext.setText(btcXmrUri.amount);

                    Context context = getApplicationContext();
                    showToast("found address:" + openUri.toString());
                }

            } else {
                showToast("bad uri!");
            }
            // else continue with any other code you need in the method
        } catch (Exception e) {
            Log.d("asdf","scan parsing error");
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    //now this is just a demo
    public void onBalanceClick(View view) {

    }

    public void insertAddress(SavedAddress sa) {
        ContentValues data = new ContentValues();
        data.put("Name", sa.Name);
        data.put("address", sa.address);
        try {
            sql.insert("SavedAddress", null, data);
        } catch (Exception excIns) {
            Log.d("asdf", "couldn't insert!");
        }
    }

    public void saveButtonClick(View view) {
        //
        final SavedAddress sa = new SavedAddress();
        EditText desttext = (EditText) findViewById(R.id.destination_text);
        sa.address = desttext.getText().toString();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Alias?");
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do something with value!
                sa.Name = input.getText().toString();
                insertAddress(sa);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                Log.d("asdf", "canceled");
            }
        });

        alert.show();
    }

    public void sendButtonClick(View view) {
        //needs to parse whether it's an xmr address or bitcoin first
         //check if there is no api key stored!..
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String encKey = SP.getString("api_key", "");
        if (encKey == "") {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(MainActivity.this, "Enter api key in settings menu!", duration);
            toast.show();

        } else {
            try {
                //jreq.GetTime();
                EditText desttext = (EditText) findViewById(R.id.destination_text);
                EditText amtext = (EditText) findViewById(R.id.amount_text);
                EditText pidtext = (EditText) findViewById(R.id.pid_text);
                aksnew.jreq.dest = desttext.getText().toString();
                aksnew.jreq.amount = amtext.getText().toString();
                amtext.setText(""); //set amount to zero, so don't accidentally resend..
                aksnew.jreq.pid = pidtext.getText().toString();

                String typec = "xmr";
                if (aksnew.jreq.dest.length() < 40) {
                    typec = "bitcoin";
                }

                //confirm dialog...
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Confirm");
                alertDialog.setMessage("Do you want to send " + aksnew.jreq.amount + " " + typec + "?");
                final String typef = typec;
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (typef == "bitcoin") {
                            aksnew.unlockBox(aksnew.AKSSENDBTC, "unlock");
                        } else if (typef == "xmr") {
                            aksnew.unlockBox(aksnew.AKSSENDXMR, "unlock");
                        }
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("asdf", "cancelled!");
                    }
                });
                alertDialog.show();

            } catch (Exception sendEx) {
                showToast("error in sending!");
                Log.d("asdf", "problem in send");
            }
        }
    }

    //method for resume, should again query for password...
    @Override
    public void onResume(){
        super.onResume();
        //reset the aksnew..
        aksnew = new ApiKeyStore(MainActivity.this);
        JsonRequester jreqt = new JsonRequester(MainActivity.this);
        try {
            jreqt.GetTime();
        } catch (Exception e) {
            Log.d("asdf","no network");
        }
    }
}
