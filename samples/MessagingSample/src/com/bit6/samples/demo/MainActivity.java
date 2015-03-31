
package com.bit6.samples.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.ResultHandler;

public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener {

    static final String TAG = "Main";

    private Bit6 bit6;

    private EditText mUsername;
    private EditText mPassword;
    private Button mLogin;
    private Button mSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bit6 = Bit6.getInstance();

        // User credentials
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);

        // Login
        mLogin = (Button) findViewById(R.id.login);
        mLogin.setOnClickListener(this);

        // Signup
        mSignup = (Button) findViewById(R.id.signup);
        mSignup.setOnClickListener(this);

        // Environment selection
        // Useful only for testing
        Spinner spinner = (Spinner) findViewById(R.id.env_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.env_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int env = getBit6Environment();
        spinner.setSelection(env == Bit6.PRODUCTION ? 0 : 1);
        spinner.setOnItemSelectedListener(this);

        // CrashManager.register(this, App.HOCKEY_APP_TOKEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If user auth'ed - go to Chats
        doneIfAuthenticated();
    }

    // If user is authenticated - go to ChatsActivity
    private boolean doneIfAuthenticated() {
        // Is the user authenticated?
        boolean flag = bit6.getSessionClient().isAuthenticated();
        if (flag) {
            // Go the Chats activity
            Intent intent = new Intent(MainActivity.this, ChatsActivity.class);
            startActivity(intent);
            finish();
        }
        return flag;
    }

    // Handle the authentication result (from login or signup calls)
    private ResultHandler mAuthResultHandler = new ResultHandler() {

        @Override
        public void onResult(boolean success, String msg) {
            mLogin.setEnabled(true);
            mSignup.setEnabled(true);
            if (success) {
                if (doneIfAuthenticated()) {
                    // Do not show anything in the toast
                    msg = null;
                }
            } else {
                Log.e(TAG, "Auth failed: " + msg);
            }
            if (msg != null) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onClick(View v) {
        // Signup or Login clicked

        String username = mUsername.getText().toString().trim();
        String pass = mPassword.getText().toString().trim();

        // Invalid username or password
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, getString(R.string.incorrect_credentials), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Disable login/signup button
        mLogin.setEnabled(false);
        mSignup.setEnabled(false);

        // User identity - we assume username kind
        Address identity = Address.fromParts(Address.KIND_USERNAME, username);

        // Signup
        if (v == mSignup) {
            bit6.getSessionClient().signup(identity, pass, mAuthResultHandler);
        }
        // Login
        else {
            bit6.getSessionClient().login(identity, pass, mAuthResultHandler);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Environment selection
        boolean isProd = position == 0;
        int env = isProd ? Bit6.PRODUCTION : Bit6.DEVELOPMENT;
        setBit6Environment(env);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // Change environment and apikey for Bit6.
    // Useful only in this demo for testing purposes only.
    private void setBit6Environment(int env) {
        // Existing environment
        int oldEnv = getBit6Environment();

        // Nothing to change
        if (env == oldEnv)
            return;

        // Destroy current instance of Bit6
        bit6.destroy();

        // Save environment value
        SharedPreferences pref = getSharedPreferences(App.PREF_NAME, MODE_PRIVATE);
        Editor ed = pref.edit();
        ed.putInt(App.PREF_ENV_ID, env);
        ed.commit();

        // Pretty much the same code as in the App class
        String apikey = env == Bit6.PRODUCTION ? App.PROD_API_KEY : App.DEV_API_KEY;
        // Re-initialized Bit6 in the new environment
        bit6.init(getApplicationContext(), apikey, env);
    }

    private int getBit6Environment() {
        SharedPreferences pref = getSharedPreferences(App.PREF_NAME, MODE_PRIVATE);
        int oldEnv = pref.getInt(App.PREF_ENV_ID, Bit6.PRODUCTION);
        return oldEnv;
    }
}
