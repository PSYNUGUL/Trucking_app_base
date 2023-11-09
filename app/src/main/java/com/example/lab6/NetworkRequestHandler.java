package com.example.lab6;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

public class NetworkRequestHandler {
    private static NetworkRequestHandler instance;
    private RequestQueue requestQueue;

    private NetworkRequestHandler(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized NetworkRequestHandler getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkRequestHandler(context);
        }
        return instance;
    }

    public interface NetworkResponseListener {
        void onSuccess(JSONObject response);

        void onError(VolleyError error);
    }

    public void fetchData(String url, final NetworkResponseListener listener, final Map<String, String> headers) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                return headers;  // Set custom headers here
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
}
