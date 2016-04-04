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

        //I am sure this is not the best way to code this, but should be secure at least.
        ApiKeyStore aks = new ApiKeyStore("Password?",TxnActivity.this);
        //aks.jreq = new JsonRequester(getApplicationContext());
        aks.jreq.wv = (WebView)this.findViewById(R.id.webView);
        aks.unlockBox(aks.AKSTXNS, "password?");
    }
}
