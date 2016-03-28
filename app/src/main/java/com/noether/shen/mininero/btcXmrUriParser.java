package com.noether.shen.mininero;

import android.util.Log;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;

/**
 * Created by shen on 3/28/2016.
 */
public class btcXmrUriParser {
    public String amount= "0.0";
    public String dest = "";
    public String pid = "";

    public btcXmrUriParser(String tmp) {
        amount = "0.0";
        pid = "";
        dest = "";
        if (tmp.contains("bitcoin:")) {
            Log.d("asdf", "index " + tmp.indexOf("bitcoin:") + 8);
            tmp = tmp.substring(tmp.indexOf("bitcoin:") + 8); //get stuff after bitcoin: if it's there
        } else if (tmp.contains("monero:")) {
            Log.d("asdf", "parsing btc address");
            tmp = tmp.substring(tmp.indexOf("monero:") + 7); //get stuff after monero: if it's there
        }
        if (tmp.contains("?")) {
            dest = tmp.substring(0, tmp.indexOf("?"));
            List<NameValuePair> decoder = URLEncodedUtils.parse(tmp, StandardCharsets.UTF_8);
            for (NameValuePair p : decoder) {
                Log.d("asdf", "name:" + p.getName());
                Log.d("asdf", "value:" + p.getValue());
                if (p.getName().contains("amount")) {
                    amount = p.getValue();
                }
                if (p.getName().contains("tx_payment_id")) {
                    pid = p.getValue();
                }
            }
        } else {
            dest = tmp;
        }

            //if there are more parameters..
            //if (tmp.contains("?"))
            //{
                //String tmp2 = tmp.substring(tmp.indexOf("?") + 1); //stuff after first ?
            //}
    }
}
