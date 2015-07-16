package com.example.clement.tp3;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.example.clement.tp3.adapter.GroupViewAdapter;
import com.example.clement.tp3.app.AppController;
import com.example.clement.tp3.model.Group;
import com.google.gson.Gson;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroupActivity extends ActionBarActivity {

    private static final String url = "http://questioncode.fr:10007/api/groups";
    private ProgressDialog pDialog;
    private static final String TAG = CreateAccountActivity.class.getSimpleName();
    private List<Group> groups = new ArrayList<Group>();
    private ListView listView;
    private GroupViewAdapter adapter;
    private AlertDialog createGroupDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_user);

        listView = (ListView) findViewById(R.id.groupList);
        adapter = new GroupViewAdapter(this, groups);
        listView.setAdapter(adapter);

        final Context context = this;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "POSITION -> "+position);
                Group group = groups.get(position);
                String groupId = group.getId();

                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        AppController.getLoggedUserName(new AppController.AsyncCallback() {
            @Override
            public void onSuccess(String name) {
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle(getString(R.string.groupsOf) + " " + name);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);

        pDialog=new ProgressDialog(this);
        AppController.getJsonArray(url,new Response.Listener<JSONArray>() {

            @Override
            public void onResponse (JSONArray response){
            Log.d(TAG, "response : " + response);
            Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT);

            for (int i = 0; i < response.length(); i++) {
                try {
                    List<String> userEmails = new ArrayList<String>();
                    JSONObject obj = response.getJSONObject(i);
                    String name = obj.getString("name");
                    Group group = new Group(name);

                    JSONArray emails = obj.getJSONArray("emails");

                    for (int j = 0; i < emails.length(); j++) {
                        userEmails.add(emails.get(j).toString());
                    }

                    group.setId(obj.getString("_id"));

                    group.setUsers(userEmails);
                    groups.add(group);

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException -> " + e.getMessage());
                }
            }
            adapter.notifyDataSetChanged();
            pDialog.hide();
            }
        },new Response.ErrorListener()
            {

                @Override
                public void onErrorResponse (VolleyError error){
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
        if(createGroupDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(R.layout.dialog_add_group);
            alertDialogBuilder.setPositiveButton(R.string.validate,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            addGroup(dialog);
                        }
                    });
            createGroupDialog = alertDialogBuilder.create();
        }

        createGroupDialog.show();


    }

    public void addGroup(DialogInterface dialog) {
        EditText inputGroupName = (EditText)((AlertDialog) dialog).findViewById(R.id.inputGroupName);
        EditText inputGroupUsers = (EditText)((AlertDialog) dialog).findViewById(R.id.inputGroupUsers);

        String groupName = inputGroupName.getText().toString();
        String groupUsers = inputGroupUsers.getText().toString();

        String[] usersEmails = groupUsers.split(",");
        List<String> emails = new ArrayList<String>();

        for(String email: usersEmails) {
            emails.add(email);
        }

        Group group = new Group(groupName);
        group.setUsers(emails);

        Map<String, String> params = new HashMap<>();
        params = new HashMap<String, String>();
        params.put("name", groupName);
        params.put("emails", new Gson().toJson(emails));

        AppController.postJson(url, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Group created on server");
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());

            }
        });

        groups.add(group);
        adapter.notifyDataSetChanged();
        clearCreateGroupDialog(dialog);
    }

    private void clearCreateGroupDialog(DialogInterface dialog) {
        EditText name = (EditText)((AlertDialog) dialog).findViewById(R.id.inputGroupName);
        name.setText("");
        EditText users = (EditText)((AlertDialog) dialog).findViewById(R.id.inputGroupUsers);
        users.setText("");
    }

}
