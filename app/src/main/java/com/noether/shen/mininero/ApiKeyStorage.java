package com.noether.shen.mininero;

/**
 * Created by shen on 3/26/2016.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.noether.shen.mininero.TweetNaclFast;

import java.nio.charset.StandardCharsets;

public class ApiKeyStorage {
    public String salt;
    public TweetNaclFast.Box.KeyPair StorageSkPk;
    SharedPreferences SP;
    Context context;
    TweetNaclFast.Box mnBox;

    public ApiKeyStorage(Context contextPassed, String password) {
        context = contextPassed;
        //Get preferences storage
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        //SharedPreferences preferences = getSharedPreferences();

        //recover salt from settings or create it if it doesn't exist
       salt = SP.getString("mn_salt", "");
        TweetNaclFast.Box.KeyPair saltPair = new TweetNaclFast.Box.KeyPair();
        if (salt == "") {
            //generate salt, and save it..
            salt = TweetNaclFast.hexEncodeToString(saltPair.getSecretKey());

            //write salt to storage
            SharedPreferences.Editor editor = SP.edit();
            editor.putString("mn_salt", salt);
            editor.commit();
        }

        //seed = hash(salt | password)
        byte[] seedp = new byte[64];
        byte[] seed = saltPair.getPublicKey();
        int i = 0;
        try {
            seedp = TweetNaclFast.Hash.sha512(salt + password); //returns 64 bytes
            for (i = 0 ; i < 32 ; i++) {
                seed[i] = seedp[i];
            }
        } catch (Exception EncExc) {
            //otherwise it's random, so it won't work..
            Log.d("asdf","sha512encoding exception in ApiKeyStorage.java");
        }

        //this is what will be used to encrypt / decrypt the api_key
        StorageSkPk = TweetNaclFast.Box.keyPair_fromSecretKey(seed); //takes 32 bytes sk I believe..
        mnBox = new TweetNaclFast.Box(StorageSkPk.getPublicKey(), StorageSkPk.getSecretKey());
    }

    public byte[] getApiKey() {
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        String EncryptedApiKey = SP.getString("mn_api_key", "");
        return mnBox.open(TweetNaclFast.hexDecode(EncryptedApiKey));
    }

    public void storeApiKey(String apikey) {
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("asdf", "apikey before enc:"+apikey);
        //byte[] api_key_bytes = apikey.getBytes(StandardCharsets.UTF_8);
        byte[] api_key_bytes = TweetNaclFast.hexDecode(apikey); //different from getBytes..
        String frombytes = TweetNaclFast.hexEncodeToString(api_key_bytes);
        Log.d("asdf", "apikey from bytes before enc:"+frombytes);
        String encKey = TweetNaclFast.hexEncodeToString(mnBox.box(api_key_bytes));
        Log.d("asdf", "encKey in storeApiKey is:"+encKey);

        //testing if can decrypt it..
        String decKey = TweetNaclFast.hexEncodeToString(mnBox.open(TweetNaclFast.hexDecode(encKey)));
        Log.d("asdf", "decKey in storeApiKey is:"+decKey);

        SharedPreferences.Editor editor = SP.edit();
        editor.putString("mn_api_key", encKey);
        editor.commit();
    }
}
