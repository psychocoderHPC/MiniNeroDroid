package com.noether.shen.mininero;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.noether.shen.mininero.MiniNodoClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;

/**
 * Created by shen on 3/27/2016.
 */
public class JsonRequester {

    Context JContext;

    public JsonRequester(Context context) {
        JContext = context;
    }

    public long now() {
        return (long) (System.currentTimeMillis() / 1000);
        //return Convert.ToInt64(DateTime.Now.Subtract(new DateTime(1970, 1, 1)).TotalSeconds);
    }

    public long computeBackwardsOffset(String timestamp) {
        long last = Long.parseLong(timestamp);   // Convert.ToInt64(timestamp);
        long offset = last - now();
        return offset;
    }

     public void GetTime() throws Exception {
       final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(JContext);
       String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
       Log.d("asdf", "requesting:" + mnip);
       MiniNodoClient mnc = new MiniNodoClient(mnip);

       //use this handler if it's just a String, not a json..
       mnc.get("/api/mininero", null, new AsyncHttpResponseHandler() {

           @Override
           public void onSuccess(int statusCode, Header[] headers, byte[] response) {
               // called when response HTTP status is "200 OK"
               Log.d("asdf", "time2success");
               try {
                   String t2 = new String(response, "UTF-8");
                   Log.d("asdf", "response is:"+t2);
                   SharedPreferences.Editor editor = SP.edit();
                   editor.putLong("offsetCB", computeBackwardsOffset(t2) );
                   Log.d("asdf", "computed offset as:"+Long.toString(computeBackwardsOffset(t2)));
                   editor.commit();
                   /*
                   Int64 cb_time = Convert.ToInt64(s.ToString());
                   offsetCB = cb_time - Convert.ToInt64(DateTime.Now.Subtract(new DateTime(1970, 1, 1)).TotalSeconds);
                   System.Diagnostics.Log.d("computed offest as..:" + offsetCB.ToString());
                   Windows.Storage.ApplicationData.Current.LocalSettings.Values["offset"] = offsetCB;
                   */
               } catch (UnsupportedEncodingException ue) {
                   Log.d("asdf", "bad encoding");
               }
           }

           @Override
           public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
               // called when response HTTP status is "4XX" (eg. 401, 403, 404)
               Log.d("asdf", "time2failure");
           }
       });
   }

     public String GenerateSignature(String message) {

        //obviously prompt for this later, or maybe include the prompt as part of the class?
        String password = "testPassword";
        ApiKeyStorage aks = new ApiKeyStorage(JContext, password);

        String rv = "";
        //Signature.KeyPair skpk = Signature.keyPair_fromSeed(TweetNaclFast.hexDecode(sk)); //use if 32 byte
        TweetNaclFast.Signature.KeyPair skpk = TweetNaclFast.Signature.keyPair_fromSecretKey(aks.getApiKey()); //use if 64 byte
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        TweetNaclFast.Signature Ed25519 = new TweetNaclFast.Signature(skpk.getPublicKey(), skpk.getSecretKey());
        try {
            byte[] sigBytes = Ed25519.sign(messageBytes);
            rv = TweetNaclFast.hexEncodeToString(sigBytes).substring(0, 128);
            Log.d("asdf", rv);
            //Log.d("asdf", "sig length:" + Integer.toString(rv.length()));
        } catch (Exception ee) {
            Log.d("asdf","signing error");
        }
        return rv;
    }

    public void GetBalance() throws Exception {
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(JContext);
        String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);

        String timestamp = Long.toString(now() + offsetCB);
        String message = "balance" + timestamp;
        Log.d("asdf", "message to sign is:"+message);
        String signature = GenerateSignature(message);
        Log.d("asdf", "signature is:"+signature);

        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("Type", "balance");
        params.put("signature", signature);


        mnc.post("/api/mininero/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asdf", "success1");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String balanceString) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
                Log.d("asdf", "balance is:"+balanceString);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(JContext, balanceString, duration);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "error in get balance:" + errorResponse);
                    //actually not an error...
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(JContext, errorResponse, duration);
                    toast.show();
            }

        });

    }

    public void GetAddress(final EditText et) throws Exception {
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(JContext);
            String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);

        String timestamp = Long.toString(now() + offsetCB);
        String message = "address" + timestamp;
            Log.d("asdf", "message to sign is:" + message);
        String signature = GenerateSignature(message);
            Log.d("asdf", "signature is:" + signature);

        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("Type", "address");
        params.put("signature", signature);


        mnc.post("/api/mininero/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asdf", "success1");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String balanceString) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
                Log.d("asdf", "your address is:"+balanceString);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(JContext, balanceString, duration);
                toast.show();
                et.setText(balanceString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                //actually not an error...
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(JContext, errorResponse, duration);
                toast.show();
                et.setText(errorResponse);
            }

        });

    }

    //btc send function...
    //does it via xmr.to
    public void SendBtc(String destination, String amount) throws Exception {
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(JContext);
        String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);

        String timestamp = Long.toString(now() + offsetCB);
        String message = "send" + amount.replace(".", "d") + timestamp + destination;

        String signature = GenerateSignature(message);

        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("amount", amount);
        params.put("Type", "send");
        params.put("destination", destination);
        params.put("signature", signature);


        mnc.post("/api/mininero/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asdf", "success1");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String balanceString) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
                Log.d("asdf", "your address is:"+balanceString);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(JContext, balanceString, duration);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                if (errorResponse.contains("xmr")) {
                    //actually not an error...
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(JContext, errorResponse, duration);
                    toast.show();
                }
            }
        });
    }

    //xmr send function...
    public void SendXMR(String destination, String Pid, String amount) throws Exception {
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(JContext);
        String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);

        String timestamp = Long.toString(now() + offsetCB);
        String message = "sendXMR" + amount.replace(".", "d") + timestamp + destination;

        String signature = GenerateSignature(message);

        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("amount", amount);
        if (Pid != "") {
            params.put("pid", Pid);
        }
        params.put("Type", "sendXMR");
        params.put("destination", destination);
        params.put("signature", signature);


        mnc.post("/api/mininero/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asdf", "success1");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String balanceString) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
                Log.d("asdf", "your address is:" + balanceString);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(JContext, balanceString, duration);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                    //actually not an error...
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(JContext, errorResponse, duration);
                    toast.show();
            }
        });
    }

    public void GetTxns(final android.webkit.WebView wv)
    {
        Log.d("asdf","trying to get txn's");
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(JContext);
        String mnip = SP.getString("mininodo_ip", "http://localhost:8080");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);
        String timestamp = Long.toString(now() + offsetCB);
        String message = "mininerotxnwebview" + timestamp;
        String signature = GenerateSignature(message);
        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("signature", signature);

        mnc.post("/web/mininero/", params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asdf", "success1");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String result) {
                // Pull out the first event on the public timeline
                Log.d("asdf", "success2");
                //Log.d("asdf", "your address is:" + balanceString);
                wv.loadData(result, "text/html", "UTF-8");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                //actually not an error...
                wv.loadData(errorResponse, "text/html", "UTF-8");
            }
        });
    }
}
