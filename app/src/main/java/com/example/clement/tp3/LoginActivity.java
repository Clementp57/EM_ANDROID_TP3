package com.example.clement.tp3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.example.clement.tp3.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends ActionBarActivity {

    private static final String url = "http://questioncode.fr:10007/auth/local";
    private ProgressDialog pDialog;
    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View v) {
        final Context context = this;

        EditText emailView = (EditText)findViewById(R.id.emailLoginInput);
        EditText passwordView = (EditText)findViewById(R.id.passwordLoginInput);

        final String mail = emailView.getText().toString();
        final String password = passwordView.getText().toString();

        if( mail.isEmpty() || password.isEmpty() ) {
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.pleaseFillFields), Toast.LENGTH_SHORT);
            toast.show();
        } else {
            pDialog = new ProgressDialog(this);

            // Showing progress dialog before making http request
            pDialog.setMessage(getResources().getString(R.string.loading));
            pDialog.show();

            Map<String, String> params = new HashMap<>();
            params = new HashMap<String, String>();
            params.put("email", mail);
            params.put("password", password);

            boolean requestOk = AppController.postJson(url, params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String token = response.getString("token");
                        Log.d(TAG, "TOKEN : " + token);

                        AppController.logUser(token, mail);

                        Intent intent = new Intent(context, WelcomeUserActivity.class);
                        startActivity(intent);
                    } catch (JSONException exception) {
                        Log.d(TAG, exception.toString());
                    }
                    pDialog.hide();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    hidePDialog();

                    // Dirty : Best way to do would be to create An networkResponse class (+subClasses) and deserialize json to this class using the Gson lib
                    // and a custom adapter
                    if (error.networkResponse.statusCode == 401) {
                        String json = new String(error.networkResponse.data);

                        try {
                            JSONObject obj = new JSONObject(json);
                            String jsonErrorMessage = obj.getString("message");
                            // REALLY REALLY BAD, we should not tell the user which one of mail/pass is wrong ...
                            if (jsonErrorMessage != null && jsonErrorMessage.equals(getString(R.string.server_incorrect_password))) {
                                Toast.makeText(context, getString(R.string.invalidPass), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, getString(R.string.invalidMail), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (error.networkResponse.statusCode == 503) {
                        Toast.makeText(context, getString(R.string.serverOffline), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if(!requestOk) {
                hidePDialog();
            }
        }
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }
}
