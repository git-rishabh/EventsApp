package com.creation.events.eventsapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    public static final String TAG= "LoginActivity";
    private static final String ROOT_URL = HomeActivity.ROOT_URL;

    public static final int RC_SIGN_IN = 25;
    String mEmail;
    String mFullName;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInAccount googleAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //signInButton.setScopes(gso.getScopeArray());
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    public void showLoginDialog(View view){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            googleAccount = result.getSignInAccount();
            Toast.makeText(this,"Login successful!",Toast.LENGTH_LONG).show();
            SharedPreferences sharedPref = this.getSharedPreferences("loggedInUser", Context.MODE_PRIVATE);
            SharedPreferences.Editor sharedEditor = sharedPref.edit();
            sharedEditor.putString("username", googleAccount.getDisplayName());
            sharedEditor.putString("email", googleAccount.getEmail());
            sharedEditor.commit();
            final User user = new User(googleAccount.getDisplayName(),googleAccount.getEmail());

            final ProgressDialog loading = ProgressDialog.show(this,"Adding User","Please wait...",false,false);

            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint(ROOT_URL)
                    .build();

            //Creating an object of our api interface
            UsersAPI api = adapter.create(UsersAPI.class);

            //Defining the method
            api.addUser(user, new Callback<String>() {
                @Override
                public void success(String user_string, Response response) {
                    //Dismissing the loading progressbar
                    loading.dismiss();
                    Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("user",user);
                    i.putExtras(b);
                    startActivity(i);
                    finish();
                }

                @Override
                public void failure(RetrofitError error) {
                    //you can handle the errors here
                    Log.e(TAG,error.toString());
//                    Toast.makeText(getApplicationContext(), "Error adding user!", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                    Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("user",user);
                    i.putExtras(b);
                    startActivity(i);
                    finish();
                }
            });

        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this,"Not authenticated!",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


}
