package com.example.clement.tp3.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.clement.tp3.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Clement on 17/06/15.
 */
public class AppController extends Application {
    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static AppController mInstance;
    private static Context context;

    public interface AsyncCallback{
        void onSuccess(String result);
    }

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

    public static boolean postJson(String url, Map<?, ?> params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        // Checks Network Availability
        if(isNetworkAvailable()) {
            // Create Volley request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
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

    public static boolean getJsonArray(String url, Response.Listener<JSONArray> successListener, Response.ErrorListener errorListener) {
        // Checks Network Availability
        if(isNetworkAvailable()) {
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

    public static boolean getJsonObject(String url, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        // Checks Network Availability
        if(isNetworkAvailable()) {
            // Create Volley request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(),
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

    public static void getLoggedUserName(final AsyncCallback callback) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_user), Context.MODE_PRIVATE);

        String email = sharedPref.getString(context.getString(R.string.email), "");

        sharedPref = context.getSharedPreferences(email, Context.MODE_PRIVATE);
        String name = sharedPref.getString(context.getString(R.string.name), "");

        if(name.isEmpty()) {
            getLoggedUserNameFromServer(callback);
        } else {
            callback.onSuccess(name);
        }
    }

    public static void getLoggedUserNameFromServer(final AsyncCallback callback) {
        AppController.getJsonObject("http://questioncode.fr:10007/api/users/me", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name = response.getString("name");
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            context.getString(R.string.active_user), Context.MODE_PRIVATE);

                    String email = sharedPref.getString(context.getString(R.string.email), "");

                    sharedPref = context.getSharedPreferences(email, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(context.getString(R.string.name), name);
                    editor.commit();
                    callback.onSuccess(name);
                } catch (Exception e) {
                    Log.d(TAG, "Error: " + e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
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
