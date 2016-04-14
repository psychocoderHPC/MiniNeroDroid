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
 * Parses string from qr code
 */
public class btcXmrUriParser {
    public String amount = "0.0";
    public String dest = "";
    public String pid = "";

    public btcXmrUriParser(String tmp) {
        amount = "0.0";
        pid = "";
        dest = "";
        if (tmp.contains("bitcoin:")) {
            tmp = tmp.substring(tmp.indexOf("bitcoin:") + 8); //get stuff after bitcoin: if it's there
        } else if (tmp.contains("monero:")) {
            tmp = tmp.substring(tmp.indexOf("monero:") + 7); //get stuff after monero: if it's there
        }
        if (tmp.contains("?")) {
            dest = tmp.substring(0, tmp.indexOf("?"));
            List<NameValuePair> decoder = URLEncodedUtils.parse(tmp, StandardCharsets.UTF_8);
            for (NameValuePair p : decoder) {
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
    }
}
