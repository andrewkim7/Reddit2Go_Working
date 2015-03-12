package com.mycompany.reddit2go_working;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends ActionBarActivity {

    public String username;
    // The login API URL
    private final String REDDIT_LOGIN_URL = "https://ssl.reddit.com/api/login";
    // The Reddit cookie string
    // This should be used by other methods after a successful login.
    private String redditCookie = "";
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        if (id == R.id.action_login) {
            login();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void login() {

        if (username == null) {

            LayoutInflater inflater = this.getLayoutInflater();

            final View viewDialog = inflater.inflate(R.layout.activity_login, null);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(viewDialog)
                    .setTitle("TEST DIALOG")
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(final DialogInterface dialog) {

                    Button btnOK = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    btnOK.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (((EditText) viewDialog.findViewById(R.id.username)).getText().toString().trim().length() > 0) {
                                if (login(((EditText) viewDialog.findViewById(R.id.username)).getText().toString().trim(),
                                        ((EditText) viewDialog.findViewById(R.id.password)).getText().toString().trim())) {
                                    username = ((EditText) viewDialog.findViewById(R.id.username)).getText().toString().trim();
                                    dialog.dismiss();
                                    MenuItem item = menu.findItem(R.id.action_login);
                                    item.setTitle(R.string.logout);
                                }
                            }
                        }
                    });

                }
            });

            dialog.show();
        }
        else {logout();}
    }


    // This method creates a connection that allows
// you to POST data
    private HttpURLConnection getConnection(String url){
        URL u = null;
        try{
            u = new URL(url);
        }catch(MalformedURLException e){
            Log.d("Invalid URL", url);
            return null;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)u.openConnection();
        } catch (IOException e) {
            Log.d("Unable to connect", url);
            return null;
        }
        // Timeout after 30 seconds
        connection.setReadTimeout(30000);
        // Allow POST data
        connection.setDoOutput(true);
        return connection;
    }


    // This method lets you POST data to the URL.
    private boolean writeToConnection(HttpURLConnection con, String data){
        try{
            PrintWriter pw=new PrintWriter(
                    new OutputStreamWriter(
                            con.getOutputStream()
                    )
            );
            pw.write(data);
            pw.close();
            return true;
        }catch(IOException e){
            Log.d("Unable to write", e.toString());
            return false;
        }
    }

    // This method lets you log in to Reddit.
// It fetches the cookie which can be used in subsequent calls
// to the Reddit API.
    private boolean login(String username, String password){
        HttpURLConnection connection = getConnection(REDDIT_LOGIN_URL);

        if(connection == null)
            return false;

        //Parameters that the API needs
        String data="user="+username+"&passwd="+password;

        if(!writeToConnection(connection, data))
            return false;

        String cookie=connection.getHeaderField("set-cookie");

        if(cookie==null)
            return false;

        cookie=cookie.split(";")[0];
        if(cookie.startsWith("reddit_first")){
            // Login failed
            Log.d("Error", "Unable to login.");
            return false;
        }else if(cookie.startsWith("reddit_session")){
            // Login success
            Log.d("Success", cookie);
            redditCookie = cookie;
            return true;
        }
        return false;
    }

    // Validate username if it is empty, notify a user to enter username
    private boolean ValidateUsername(EditText username) {
        //check if title is empty
        if (!hasContent(username)) {
            Toast.makeText(this, "Enter username.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean hasContent(EditText username) {
        boolean bHasContent = false;

        if (username.getText().toString().trim().length() > 0) {
            // Got content
            bHasContent = true;
        }
        return bHasContent;
    }

    public void logout(){
        Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
        username = null;
        MenuItem item = menu.findItem(R.id.action_login);
        item.setTitle(R.string.signin);

//        SharedPreferences sharedpreferences = getSharedPreferences
//                (MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);
//        Editor editor = sharedpreferences.edit();
//        editor.clear();
//        editor.commit();
//        moveTaskToBack(true);
//        Welcome.this.finish();
    }
}
