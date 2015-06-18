package com.example.clement.tp3;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.example.clement.tp3.app.AppController;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class WelcomeUserActivity extends ActionBarActivity {

    private static final String url = "http://questioncode.fr:10007/api/groups";
    private ProgressDialog pDialog;
    private static final String TAG = CreateAccountActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_user);

        String name = AppController.getLoggedUserName();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.groupsOf) + " " + name);

        ListView listView = (ListView) findViewById(android.R.id.list);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);

        pDialog = new ProgressDialog(this);
        final Context context = this;
        AppController.getJsonArray(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, "response : " + response);
                Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT);
                pDialog.hide();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                System.out.println("GET JSON EPIC FAIL =>" + error.toString());
                /* if (error.networkResponse.statusCode == 503) {
                    Toast.makeText(context, getString(R.string.serverOffline), Toast.LENGTH_SHORT).show();
                }*/

                hidePDialog();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome_user, menu);
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
        } else if(id == R.id.action_logout) {
            SharedPreferences sharedPref= this.getSharedPreferences(
                    getString(R.string.active_user), Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.commit();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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

    public void createGroup(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(R.layout.dialog_add_group);
        alertDialogBuilder.create().show();

    }
}
