package com.jhonn_aj.descubreperuandroid.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jhonn_aj.descubreperuandroid.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by jhonn_aj on 12/03/2017.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    
    @BindView(R.id.edit_email) EditText editEmail;
    @BindView(R.id.edit_password) EditText editPassword;
    @BindView(R.id.btn_sesion) Button btnSesion;
    @BindView(R.id.btn_fb) ImageButton btnFb;
    @BindView(R.id.btn_twitter) ImageButton btnTwitter;
    @BindView(R.id.btn_google) ImageButton btnGoogle;
    @BindView(R.id.btn_register) Button btnRegister;
    @BindView(R.id.rlaProgress) View rlaProgress;

    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity" ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        initAutenticationGoogle();

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btn_google)
    public void handleSessionGoogle(){
        signIn();
    }

    @OnClick(R.id.btn_fb)
    public void handleSessionFb(){

    }

    private void initAutenticationGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        rlaProgress.setVisibility(View.VISIBLE);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            rlaProgress.setVisibility(View.GONE);
                        } else {
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            rlaProgress.setVisibility(View.GONE);
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
