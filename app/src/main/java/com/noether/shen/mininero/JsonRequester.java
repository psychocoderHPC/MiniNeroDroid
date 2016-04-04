package com.noether.shen.mininero;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
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
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;

/**
 * Created by shen on 3/27/2016.
 */
public class JsonRequester {

    Context JContext;
    public byte[] decKey;
    public byte[] seed;
    public String amount;
    public String dest;
    public String pid;
    public android.webkit.WebView wv;

    public JsonRequester(Context context, byte[] decKeyPassed, byte[] encryptionKey) {
        decKey = decKeyPassed;
        seed = encryptionKey;
        JContext = context;
    }

    //use this one to just get time..
    public JsonRequester(Context context) {
        JContext = context;
    }

    public long now() {
        return (long) (System.currentTimeMillis() / 1000);
        //return Convert.ToInt64(DateTime.Now.Subtract(new DateTime(1970, 1, 1)).TotalSeconds);
    }

    private byte[] StringToByteArray(String str, int length) {
        while(str.length() < length) {
            str = '0'+str;
        }
        return (str).getBytes(StandardCharsets.UTF_8);
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

        String rv = "";
        //Signature.KeyPair skpk = Signature.keyPair_fromSeed(TweetNaclFast.hexDecode(sk)); //use if 32 byte
        TweetNaclFast.Signature.KeyPair skpk = TweetNaclFast.Signature.keyPair_fromSecretKey(decKey); //use if 64 byte
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

    public RequestParams GenerateEncrypted(RequestParams params) {
        RequestParams encParams = new RequestParams();
        String nonce = Long.toString(now());
        byte[] nonceBytes = StringToByteArray(nonce, 24);
        String message = params.toString();
        Log.d("asdf", "request params as string:"+message);
        byte[] messageBytes = (message).getBytes(StandardCharsets.UTF_8);
        Log.d("asdf", "a1");
        TweetNaclFast.SecretBox mnBox = new TweetNaclFast.SecretBox(seed);
        Log.d("asdf", "a2");
        String messageEncrypted = TweetNaclFast.hexEncodeToString(mnBox.box(messageBytes, nonceBytes));
        Log.d("asdf", "a3");

        encParams.put("nonce", TweetNaclFast.hexEncodeToString(nonceBytes));
        encParams.put("cipher", messageEncrypted);
        //String encKey = TweetNaclFast.hexEncodeToString(mnBox.box(api_key_bytes));
        return encParams;
    }

    public String GenerateDecrypted(String messageJson) {
        //RequestParams params = new RequestParams();
        TweetNaclFast.SecretBox mnBox = new TweetNaclFast.SecretBox(seed);
        if (messageJson.contains("cipher")) {
            List<NameValuePair> decoder = URLEncodedUtils.parse(messageJson, StandardCharsets.UTF_8);
            ApiKeyStore aks = new ApiKeyStore(JContext);
            String message = "cat";
            byte[] messageBytes;
            String nonce = "0";
            byte[] nonceBytes;
            for (NameValuePair p : decoder) {
                if (p.getName().contains("cipher")) {
                   message = p.getValue();
                    Log.d("asdf", "cipher:"+p.getValue());
                }
                if (p.getName().contains("nonce")) {
                    nonce= p.getValue();
                    Log.d("asdf", "nonce:"+p.getValue());
                }
                //params.put(p.getName(), p.getValue());
            }
            messageBytes = TweetNaclFast.hexDecode(message);
            nonceBytes = TweetNaclFast.hexDecode(nonce);
            byte[] decryptedBytes = mnBox.open(messageBytes, nonceBytes);
            messageJson = new String(decryptedBytes, StandardCharsets.UTF_8);
        }
        return messageJson;
    }

    public void GetBalance() throws Exception {
        Log.d("asdf", "getting balance");
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(JContext);
        String mnip = SP.getString("mininodo_ip", "http://localhost:3000");
        Long offsetCB = SP.getLong("offsetCB", now());
        Log.d("asdf", "requesting:"+mnip);
        MiniNodoClient mnc = new MiniNodoClient(mnip);

        String timestamp = Long.toString(now() + offsetCB);
        String message = "balance" + timestamp;
        Log.d("asdf", "message to sign is:"+message);
        String signature = GenerateSignature(message); //error here..
        Log.d("asdf", "signature is:" + signature);

        RequestParams params = new RequestParams();
        params.put("timestamp", timestamp);
        params.put("Type", "balance");
        params.put("signature", signature);
        RequestParams encparams = GenerateEncrypted(params);

        mnc.post("/api/mininero/", encparams, new JsonHttpResponseHandler() {
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
                errorResponse = GenerateDecrypted(errorResponse);
                //actually not an error... need to figure that out..
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


        mnc.post("/api/mininero/", GenerateEncrypted(params), new JsonHttpResponseHandler() {
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
                balanceString = GenerateDecrypted(balanceString);
                Log.d("asdf", "success2");
                Log.d("asdf", "your address is:"+balanceString);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(JContext, balanceString, duration);
                toast.show();
                et.setText(balanceString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                errorResponse = GenerateDecrypted(errorResponse);
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
    public void SendBtc() throws Exception {
        String destination = dest;
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


        mnc.post("/api/mininero/", GenerateEncrypted(params), new JsonHttpResponseHandler() {
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
                balanceString = GenerateDecrypted(balanceString);
                Log.d("asdf", "success2");
                Log.d("asdf", "your address is:"+balanceString);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(JContext, balanceString, duration);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                errorResponse = GenerateDecrypted(errorResponse);
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
    public void SendXMR() throws Exception {
        String destination = dest;
        String Pid = pid;
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


        mnc.post("/api/mininero/", GenerateEncrypted(params), new JsonHttpResponseHandler() {
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
                balanceString = GenerateDecrypted(balanceString);
                Log.d("asdf", "success2");
                Log.d("asdf", "your address is:" + balanceString);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(JContext, balanceString, duration);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                errorResponse = GenerateDecrypted(errorResponse);
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                    //actually not an error...
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(JContext, errorResponse, duration);
                    toast.show();
            }
        });
    }

    //note you must set the public wv before calling this..
    //so ApiKeyStore aks = new ApiKeyStore
    //aks.jreq = new JsonRequester;
    //aks.jreq.wv = wv; //call in txn view...
    public void GetTxns()
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

        mnc.post("/web/mininero/", GenerateEncrypted(params), new JsonHttpResponseHandler() {
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
                result = GenerateDecrypted(result);
                wv.loadData(result, "text/html", "UTF-8");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable eee) {
                errorResponse = GenerateDecrypted(errorResponse);
                Log.d("asdf", "it may not be an error (should return dummy address on success on the test server:" + errorResponse);
                //actually not an error...
                wv.loadData(errorResponse, "text/html", "UTF-8");
            }
        });
    }


}
