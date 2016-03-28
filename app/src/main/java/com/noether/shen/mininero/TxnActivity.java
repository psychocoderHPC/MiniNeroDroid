package com.noether.shen.mininero;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class TxnActivity extends Activity {

    JsonRequester jreq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txn);
        jreq = new JsonRequester(getApplicationContext());
        WebView wv = (WebView)this.findViewById(R.id.webView);
        jreq.GetTxns(wv);
    }
}
