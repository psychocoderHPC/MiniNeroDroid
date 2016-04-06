package com.noether.shen.mininero;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Created by shen on 4/2/2016.
 * This one is supposed to work more like the c# version, since that's a bit smoother..
 */

public class ApiKeyStore {
    private String salt;
    private byte[] seed;
    private byte[] nonce;
    private byte[] sk;
    private byte[] decKey;
    private boolean isunlocked = false;
    private String unlockString;
    Context context;
    SharedPreferences SP;
    private TweetNaclFast.SecretBox mnBox;
    final public int AKSGETKEY = 0;
    final public int AKSSTORE = 1;
    final public int AKSBALANCE = 2;
    final public int AKSADDRESS = 3;
    final public int AKSCLEAR = 4;
    final public int AKSTXNS = 5;
    final public int AKSSENDBTC = 6;
    final public int AKSSENDXMR = 7;
    public JsonRequester jreq;

    public ApiKeyStore(Context thiscontext) {
        context = thiscontext;
        unlockString = "Password?";
        seed = null;
        isunlocked = false;
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("asdf", "made new apikeystore");
        getSaltNonce();
        jreq = new JsonRequester(thiscontext);
    }

    public ApiKeyStore(String ustring, Context thiscontext) {
        context = thiscontext;
        unlockString = ustring;
        seed = null;
        isunlocked = false;
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        getSaltNonce();
        jreq = new JsonRequester(thiscontext);
    }

    private void getSaltNonce() {
        //
        // recover or create salt
        //
        Log.d("asdf", "recover or create salt");
        String saltt = TweetNaclFast.hexEncodeToString(randomBytes(32));
        salt = SP.getString("apiStore_salt", saltt);
        SharedPreferences.Editor editor = SP.edit();
        editor.putString("apiStore_salt", salt);
        editor.commit();

        //
        //Recover or create nonce
        //
        Log.d("asdf", "recover or create nonce");
        String noncet = TweetNaclFast.hexEncodeToString(randomBytes(24));
        String nonceS = SP.getString("apiStore_nonce", noncet);
        nonce = TweetNaclFast.hexDecode(nonceS);
        editor.putString("apiStore_nonce", nonceS);
        editor.commit();
    }

    public byte[] randomBytes(int num) {
        SecureRandom sr = new SecureRandom();
        byte[] output = new byte[num];
        sr.nextBytes(output);
        return output;
    }

    //use this one for ones which don't modify an edittext..
    public void unlockBox(int unlockaction, String paramString) {
        EditText edt = new EditText(context);
        unlockBox(unlockaction, paramString, edt);

    }

    public void changeQueryString(String qString)  {
        unlockString = qString;
    }


    //will query user for password...
    public void unlockBox(final int unlockaction, final String paramString, final EditText et) {
        Log.d("asdf", "unlocking box");
        decKey = randomBytes(64);

        if (isunlocked == false) {
            //
            //Query user for password
            //
            Log.d("asdf", "query for user password");
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            Log.d("asdf", "a1");
            final EditText edittext = new EditText(context);
            Log.d("asdf", "a2");
            alert.setTitle(unlockString);
            Log.d("asdf", "a3");
            alert.setView(edittext);
            Log.d("asdf", "a4");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String password = edittext.getText().toString();

                    //
                    //Create seed
                    //seed = hash(salt | password)
                    //
                    Log.d("asdf", "create stored version of password");
                    byte[] seedp = new byte[64];
                    seed = randomBytes(32);
                    int i = 0;
                    byte[] messageBytes = (password + salt).getBytes(StandardCharsets.UTF_8);
                    Log.d("asdf", "trying to hash the following as password..:" + password + salt);
                    Log.d("asdf", password + salt);

                    //
                    //get actual api key decrypted
                    //
                    try {
                        seedp = TweetNaclFast.Hash.sha512(messageBytes); // TweetNaclFast.Hash.sha512(salt + password); //returns 64 bytes
                        Log.d("asdf", "did hash");
                        for (i = 0; i < 32; i++) {
                            seed[i] = seedp[i];
                        }
                        mnBox = new TweetNaclFast.SecretBox(seed);
                        Log.d("asdf", "got secret box");
                        isunlocked = true;

                        //do action

                        jreq.decKey = decApiKey(paramString); //note, this has no async
                        jreq.seed = getSpecialSeed(decKey); //for encryption
                        switch (unlockaction) {
                            case AKSGETKEY:
                                //get key
                                Log.d("asdf", "apiki" + TweetNaclFast.hexEncodeToString(decKey));
                                break;
                            case AKSSTORE:
                                //store
                                storeApiKey(decKey);
                                break;
                            case AKSBALANCE:
                                //balance
                                Log.d("asdf", "getting balance");
                                jreq.GetBalance();
                                break;
                            case AKSADDRESS:
                                //address
                                jreq.GetAddress(et);
                                break;
                            case AKSCLEAR:
                                clearApiKey();
                                break;
                                //clear api key.
                            case AKSTXNS:
                                try {
                                    jreq.GetTxns();
                                } catch (Exception e) {
                                    Log.d("asdf","txn view exception");
                                }
                                break;

                            case AKSSENDBTC:
                                Log.d("asdf", "sending btc");
                                jreq.SendBtc();
                                break;

                            case AKSSENDXMR:
                                Log.d("asdf", "sending xmr");
                                jreq.SendXMR();
                                break;

                        }

                        isunlocked = true;
                    } catch (Exception e) {
                        //otherwise it's random, so it won't work..
                        Log.d("asdf", "sha512encoding exception in ApiKeyStore.java");
                        //alert dialog for error
                        if (unlockaction != AKSTXNS) {
                            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            alertDialog.setTitle("Wrong Password or incorrect api key form!");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                        isunlocked = false;
                    }

                    Log.d("asdf", "unlocked (i.e. stored pass created from user pass");
                }
            });
            Log.d("asdf", "a5");
            alert.show();
            Log.d("asdf", "a6");
        } else {
            //already unlocked , i.e. mnBox created already...
            //do action
            Log.d("asdf", "already unlocked!");
            try {
                jreq.decKey = decApiKey(paramString); //note, this has no async
                jreq.seed = getSpecialSeed(decKey); //for encryption
                switch (unlockaction) {
                    case AKSGETKEY:
                        //Don't think I'm using for anything now..
                        //lock
                        break;
                    case AKSSTORE:
                        //store
                        //decKey = decApiKey(paramString);
                        //I think, should already have it...
                        storeApiKey(decKey);
                        break;
                    case AKSBALANCE:
                        //balance
                        try {
                            jreq.GetBalance();
                        } catch (Exception e) {
                            Log.d("asdf", "error in json requester");
                        }
                        break;
                    case AKSADDRESS:
                        //address
                        jreq.GetAddress(et);
                        break;
                    case AKSCLEAR:
                        //clear api key
                        clearApiKey();
                    case AKSTXNS:
                        try {
                            jreq.GetTxns();
                        } catch (Exception e){
                            Log.d("asdf", "txn exception");
                        }
                        break;

                    case AKSSENDBTC:
                        jreq.SendBtc();
                        break;

                    case AKSSENDXMR:
                        jreq.SendXMR();
                        break;
                    } //endswitch
            } catch (Exception e) {
                Log.d("asdf","error in json requester");
                isunlocked = false;
            }
        }
    }


    //
    // decrypt the api key using your password....
    //
    public byte[] decApiKey(String paramString) {
        //do unlock
        if (paramString.length() != 128) { //already decrypted..
            //the following code after unlock.. (probably in the callback...)
            byte[] encKey;
            encKey = TweetNaclFast.hexDecode(SP.getString("api_key", ""));

            //this is just the nonce...
            Log.d("asdf", "recovered encKey" + TweetNaclFast.hexEncodeToString(encKey));
            try {
                //decKey = XSalsa20Poly1305.TryDecrypt(encKey, seed, nonce);
                decKey = mnBox.open(encKey, nonce);
                Log.d("asdf", "recovered decKey in func" + TweetNaclFast.hexEncodeToString(decKey));
            } catch (Exception e) {
                isunlocked = false; //wrong pass I guess.
            }
        } else {
            Log.d("asdf", "Already decrypted");
            decKey = TweetNaclFast.hexDecode(paramString);
        }
        return decKey;
    }

    //get 32 byte thing
    public byte[] getSeed() {
        byte[] seedrv = new byte[32];
        //unlock box

        byte[] encKey;
        encKey = TweetNaclFast.hexDecode(SP.getString("api_key", ""));
        try {
            decKey = mnBox.open(encKey, nonce);
            //cut to the actual seed

        } catch (Exception e) {
            isunlocked = false;
        }
        for (int i = 0; i < 32; i++) {
            seedrv[i] = decKey[i];
        }
        return seedrv;
    }


    public void storeApiKey(final byte[] decKey) {

        //unlock box
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        Log.d("asdf", "a1");
        final EditText edittext = new EditText(context);
        Log.d("asdf", "a2");
        alert.setTitle("New password?");
        Log.d("asdf", "a3");
        alert.setView(edittext);
        Log.d("asdf", "a4");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = edittext.getText().toString();

                //note that all the other vars (except password and after) should already exist by the unlock
                //actually, they may not if you are storing a new password!
                byte[] messageBytes = (password + salt).getBytes(StandardCharsets.UTF_8);
                Log.d("asdf", "trying to hash the following as password..:" + password + salt);
                Log.d("asdf", password + salt);

                //
                //get actual api key decrypted
                //
                try {
                    byte[] seedp = TweetNaclFast.Hash.sha512(messageBytes); // TweetNaclFast.Hash.sha512(salt + password); //returns 64 bytes
                    Log.d("asdf", "did hash");
                    seed = randomBytes(32);
                    for (int i = 0; i < 32; i++) {
                        seed[i] = seedp[i];
                    }
                    Log.d("asdf", "got seed");
                    mnBox = new TweetNaclFast.SecretBox(seed);
                    Log.d("asdf", "got secret box");
                    isunlocked = true;

                    //decKey = TweetNaclFast.hexDecode(apikey);
                    //Log.d("asdf","decKey before store:"+apikey);
                    byte[] encKey = mnBox.box(decKey, nonce);
                    Log.d("asdf", "encKey before store:" + TweetNaclFast.hexEncodeToString(encKey));
                    SharedPreferences.Editor editor = SP.edit();
                    editor.putString("api_key", TweetNaclFast.hexEncodeToString(encKey));
                    editor.commit();
                } catch (Exception e) {
                    Log.d("asdf", "error in key store with new pass");
                }
            }
        });
        alert.show();
    }

    public void clearApiKey() {
        //unlock box
        try {
            //decKey = TweetNaclFast.hexDecode(apikey);
            //Log.d("asdf","decKey before store:"+apikey);
            SharedPreferences.Editor editor = SP.edit();
            editor.putString("api_key", "");
            editor.commit();
        } catch (Exception e) {
            Log.d("asdf", "error in clear api key");
        }

    }

    //for use in "Generate Encrypted"
    //note that although this looks like a pk
    //it's not shared except between the user and their own server
    //thus the actual sk (which only exists on one end)
    //is most secure (used for authentication)
    //the pk pair exists on both server and client
    //and is used for encryption
    private byte[] getSpecialSeed(byte[] dk) {
        byte[] seed = randomBytes(32);
        //copy first 32 bytes to there...
        for (int i = 0 ; i < 32 ; i++) {
            seed[i] = dk[i + 32];
        }
        return seed;
    }

}
