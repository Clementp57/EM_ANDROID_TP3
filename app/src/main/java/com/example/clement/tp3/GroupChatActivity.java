package com.example.clement.tp3;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.example.clement.tp3.adapter.GroupViewAdapter;
import com.example.clement.tp3.adapter.MessageViewAdapter;
import com.example.clement.tp3.app.AppController;
import com.example.clement.tp3.model.Group;
import com.example.clement.tp3.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroupChatActivity extends ActionBarActivity {

    private String url = "http://questioncode.fr:10007/api/groups/:groupid/messages";
    private static final String TAG = GroupChatActivity.class.getSimpleName();
    private final Context context = this;
    private String groupId = "";
    private ListView listView;
    private MessageViewAdapter adapter;
    private List<Message> messages = new ArrayList<Message>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        listView = (ListView) findViewById(R.id.chatList);
        adapter = new MessageViewAdapter(this, messages);
        listView.setAdapter(adapter);

        Intent intent = getIntent();
        this.groupId = intent.getStringExtra("groupId");

        AppController.getJsonArray("http://questioncode.fr:10007/api/groups/"+this.groupId+"/messages",new Response.Listener<JSONArray>() {

            @Override
            public void onResponse (JSONArray response){
                Log.d(TAG, "response : " + response);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject obj = response.getJSONObject(i);
                        String messageContent = obj.getString("content");
                        Message message = new Message(messageContent);
                        messages.add(message);
                        adapter.notifyDataSetChanged();
                    } catch(JSONException e) {

                    }


                }

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse (VolleyError error){
                VolleyLog.d(TAG, "Error: " + error.getMessage());


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_chat, menu);
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

    public void sendMessage(View v) {
        EditText messageInput = (EditText)findViewById(R.id.chatInput);
        String message = messageInput.getText().toString();
        if(!message.isEmpty()) {
            Message mess = new Message(message);
            messages.add(mess);
            adapter.notifyDataSetChanged();

            Map<String, String> params = new HashMap<String, String>();
            params.put("group", this.groupId);
            params.put("content", message);

            Log.d(TAG, "PARAMS -> "+params);

            AppController.postJson("http://questioncode.fr:10007/api/messages", params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());

                    if (error.networkResponse.statusCode == 422) {
                        String jsonError = new String(error.networkResponse.data);

                        // Dirty : Best way to do would be to create An errorMessage class and deserialize json to this class using the Gson lib
                        // and a custom adapter
                        try {
                            JSONObject obj = new JSONObject(jsonError);
                            String errorMessage = obj.getString("message");
                            JSONObject errorArray = obj.getJSONObject("errors");
                            JSONObject errorArrayEmail = errorArray.getJSONObject("email");

                            if (errorArrayEmail != null) {
                                String errorDetailedMessage = errorArrayEmail.getString("message");
                                if (errorDetailedMessage.toString().equals(getString(R.string.server_emailAlreadyInUse))) {
                                    Toast.makeText(context, getString(R.string.emailAlreadyInUse), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (error.networkResponse.statusCode == 503) {
                        Toast.makeText(context, getString(R.string.serverOffline), Toast.LENGTH_SHORT).show();
                    }

                }
            });
            messageInput.setText("");
        }
    }
}
