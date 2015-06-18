package com.example.clement.tp3.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.clement.tp3.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Clement on 17/06/15.
 */
public class AppController extends Application {
    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static AppController mInstance;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = getApplicationContext();
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static boolean postJson(String url, Map<String, String> params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        // Checks Network Availability
        if(isNetworkAvailable()) {
            // Create Volley request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                    successListener, errorListener) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(request);
            return true;
        } else {
            return false;
        }
    }

    public static boolean getJsonArray(String url, Response.Listener<JSONArray> successListener, Response.ErrorListener errorListener) {
        // Checks Network Availability
        if(isNetworkAvailable()) {
            System.out.println("GET JSON");
            // Create Volley request
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, new JSONArray(),
                    successListener, errorListener) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Authorization", "Bearer "+getLoggedUserToken());
                    return headers;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(request);
            return true;
        } else {
            return false;
        }
    }

    private static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if( activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting() ) {
            return true;
        } else {
            Toast.makeText(context, context.getString(R.string.deviceOffline), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public static String getLoggedUserName() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_user), Context.MODE_PRIVATE);

        String email = sharedPref.getString(context.getString(R.string.email), "");

        sharedPref = context.getSharedPreferences(email, Context.MODE_PRIVATE);
        String name = sharedPref.getString(context.getString(R.string.name), "");

        if(name.isEmpty()) {
            return null;
        }
        return name;
    }

    public static String getLoggedUserToken() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_user), Context.MODE_PRIVATE);

        String email = sharedPref.getString(context.getString(R.string.email), "");

        sharedPref = context.getSharedPreferences(email, Context.MODE_PRIVATE);
        String token = sharedPref.getString(context.getString(R.string.token), "");

        return token;
    }

    public static void registerAndLogUser(String token, String name, String mail) {

        SharedPreferences sharedPref = context.getSharedPreferences(
                mail, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.token), token);
        editor.putString(context.getString(R.string.name), name);
        editor.commit();

        sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_user), Context.MODE_PRIVATE);

        editor = sharedPref.edit();
        editor.putString(context.getString(R.string.email), mail);
        editor.commit();
    }

    public static void logUser(String token, String mail) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                mail, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.token), token);
        editor.commit();

        sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_user), Context.MODE_PRIVATE);

        editor = sharedPref.edit();
        editor.putString(context.getString(R.string.email), mail);
        editor.commit();
    }
}
