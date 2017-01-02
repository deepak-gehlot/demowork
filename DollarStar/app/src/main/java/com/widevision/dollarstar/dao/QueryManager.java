package com.widevision.dollarstar.dao;

import android.util.Log;


import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.NetworkUtils;

import java.io.IOException;

import okhttp3.Request;

/**
 * Created by wudevision
 */
public abstract class QueryManager<T> {
    /**
     * Generate a request that encapsulates the API endpoint and any necessary parameters/values the API needs.
     *
     * @return
     */
    protected abstract Request.Builder buildRequest() throws IOException;

    /**
     * Asynchronously queries the web api using the parameters provided in the overridden buildRequest method. Data is handed back in the
     * parseResponse method.
     *
     * @param onFinishedCallback
     */
    public void query(final AsyncCallback<T> onFinishedCallback) {
        try {
            NetworkUtils.asyncQuery(buildRequest(), new AsyncCallback<String>() {
                @Override
                public void onOperationCompleted(String response, Exception e) {
                    if (e == null) {
                        if (response != null) {
                            String serializedResponse = response.toString();
                            Log.e("response////////////", "" + serializedResponse);
                            T output = parseResponse(serializedResponse);
                            onFinishedCallback.onOperationCompleted(output, null);
                        } else {
                            onFinishedCallback.onOperationCompleted(null, new Exception("Null response from server"));
                        }
                    } else {
                        onFinishedCallback.onOperationCompleted(null, new Exception("Null response from server"));
                        e.printStackTrace();
                        Log.e("", "Exception  " + e);
                    }
                }
            });
        } catch (Exception e) {
            onFinishedCallback.onOperationCompleted(null, new Exception("Null response from server"));
            e.printStackTrace();
            Log.e("", "Io Exception " + e);
        }
    }
    protected abstract T parseResponse(String jsonResponse);
}
