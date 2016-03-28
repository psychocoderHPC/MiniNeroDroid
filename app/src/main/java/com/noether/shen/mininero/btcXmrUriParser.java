package com.noether.shen.mininero;

import android.net.Uri;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import android.net.Uri;

/**
 * Created by shen on 3/28/2016.
 */
public class btcXmrUriParser {
    public double amount= 0.0;
    public String dest = "";
    public String pid = "";
    private Uri decoder;
    public btcXmrUriParser(String tmp) {

        if (tmp.contains("bitcoin:"))
        {
            Log.d("asdf", "index " + tmp.indexOf("bitcoin:") + 8);

            tmp = tmp.substring(tmp.indexOf("bitcoin:") + 8); //get stuff after bitcoin: if it's there
            if (tmp.contains("?"))
            {
                String tmp2 = tmp.substring(tmp.indexOf("?")+1); //stuff after first ?
                decoder = Uri.parse(tmp2);
                tmp = tmp.substring(0, Math.max(tmp.indexOf("?"), 0)); //get stuff before ? if it's there..
                try
                {
                    amount = Double.parseDouble(decoder.getQueryParameter("amount"));
                } catch (Exception CouldntParseException)
                {
                    amount = 0.0;
                    Log.d("asdf", "no amount");
                }


            }
            pid = "";
        }
        else if (tmp.contains("monero:"))
        {
            //need to also pull out the pid..
            Log.d("asdf", tmp);
            tmp = tmp.substring(tmp.indexOf("monero:") + 7); //get stuff after monero: if it's there
            if (tmp.contains("?"))
            {

                String tmp2 = tmp.substring(tmp.indexOf("?")+1); //stuff after first ?
                decoder = Uri.parse(tmp2);
                try
                {
                    pid = decoder.getQueryParameter("tx_payment_id");
                } catch (Exception noPidException)
                {
                    Log.d("asdf", "no pid");
                    pid = "";
                }
                try
                {
                    amount = Double.parseDouble(decoder.getQueryParameter("tx_amount"));
                } catch (Exception couldntparseexception)
                {
                    Log.d("asdf", "no amount");
                    amount = 0.0;
                }

                tmp = tmp.substring(0, Math.max(tmp.indexOf("?"), 0)); //get stuff before ? if it's there..
                Log.d("asdf", "tmp2: " + tmp2);
            }
        }
        dest = tmp;
    }
}
