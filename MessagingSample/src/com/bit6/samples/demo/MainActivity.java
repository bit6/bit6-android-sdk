package com.bit6.samples.demo;

import android.app.Activity;
import android.content.Intent;
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
import com.bit6.sdk.OnResponseReceived;

public class MainActivity extends Activity implements OnItemSelectedListener {

	private EditText mUsername;
	private EditText mPassword;
	private Button mLogin;
	private Button mSignup;
	private Spinner mSpinner;
	private Bit6 bit6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bit6 = Bit6.getInstance();

		mUsername = (EditText) findViewById(R.id.username);
		mPassword = (EditText) findViewById(R.id.password);
		mLogin = (Button) findViewById(R.id.login);
		mLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onLoginClick();
			}
		});

		mSignup = (Button) findViewById(R.id.sign_up);
		mSignup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSignUpClick();
			}
		});

		mSpinner = (Spinner) findViewById(R.id.env_spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.env_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);
		mSpinner.setSelection(1, false);
		mSpinner.setOnItemSelectedListener(this);
	}

	private void onLoginClick() {
		String username = mUsername.getText().toString();
		String pass = mPassword.getText().toString();

		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pass)) {
			Toast.makeText(this, "Incorrect username/password",
					Toast.LENGTH_LONG).show();
		}

		Address identity = Address.fromParts(Address.KIND_USERNAME, username);

		bit6.login(identity, pass, new OnResponseReceived() {

			@Override
			public void onResponse(boolean success, String msg) {
				if (success) {
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG)
							.show();
					// If login was successful than server returned user id
					// and
					// token, which are saved in preferences.
					if (bit6.isUserLoggedIn()) {
						Intent intent = new Intent(MainActivity.this,
								ChatsActivity.class);
						startActivity(intent);
						finish();
					}
				} else {
					Log.e("login.onFailure", msg);
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG)
							.show();
				}
			}

		});
	}

	private void onSignUpClick() {
		String username = mUsername.getText().toString();
		String pass = mPassword.getText().toString();

		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pass)) {
			Toast.makeText(this, getString(R.string.incorrect_credentials),
					Toast.LENGTH_LONG).show();
		}

		Address identity = Address.fromParts(Address.KIND_USERNAME, username);

		bit6.signup(identity, pass, new OnResponseReceived() {

			@Override
			public void onResponse(boolean success, String msg) {
				if (success) {
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG)
							.show();
					if (bit6.isUserLoggedIn()) {
						Intent intent = new Intent(MainActivity.this,
								ChatsActivity.class);
						startActivity(intent);
						finish();
					}
				} else {
					Log.e("signup.onFailure", msg);
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG)
							.show();
				}
			}

		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (bit6.isUserLoggedIn()) {
			Intent intent = new Intent(MainActivity.this, ChatsActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

}
