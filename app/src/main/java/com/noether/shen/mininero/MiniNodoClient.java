package com.noether.shen.mininero;
import com.loopj.android.http.*;

/**
 * Created by shen on 3/25/2016.
 */
public class MiniNodoClient {
    public String BASE_URL = "http://localhost:8080"; //need to change this with settings
    public AsyncHttpClient client;

    public MiniNodoClient(String newUrl) {
        BASE_URL = newUrl;
        client = new AsyncHttpClient(true, 8080, 4300); //accept self-signed (it's nacl authenticated anyway)
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
