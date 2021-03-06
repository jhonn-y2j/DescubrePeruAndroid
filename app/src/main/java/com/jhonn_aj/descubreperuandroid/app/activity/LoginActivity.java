package com.jhonn_aj.descubreperuandroid.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.jhonn_aj.descubreperuandroid.MyApp;
import com.jhonn_aj.descubreperuandroid.R;
import com.jhonn_aj.descubreperuandroid.app.utils.InternetConnection;
import com.jhonn_aj.descubreperuandroid.app.utils.NetworkChangeReceiver;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

/**
 * Created by jhonn_aj on 12/03/2017.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, NetworkChangeReceiver.receiverListener {

    //keytool -exportcert -alias androiddebugkey -list -v -keystore "C:\Users\DP6ASUS\.android\debug.keystore" |
    // "C:\OpenSSL\bin\openssl.exe" sha1 -binary | "C:\OpenSSL\bin\openssl.exe" base64

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "jKXQbHLIV7G5AXODaTbDDidpu";
    private static final String TWITTER_SECRET = "vTLi35Qd3o3f1ZK8BeCbqk1KOCPjlOJ2Trnn5ZZrwAT68JTm1D";
    
    @BindView(R.id.edit_email) EditText editEmail;
    @BindView(R.id.edit_password) EditText editPassword;
    @BindView(R.id.btn_sesion) Button btnSesion;
    @BindView(R.id.btn_fb) ImageButton btnFb;
    @BindView(R.id.btn_twitter) ImageButton btnTwitter;
    @BindView(R.id.btn_google) ImageButton btnGoogle;
    @BindView(R.id.btn_register) Button btnRegister;
    @BindView(R.id.rlaProgress) View rlaProgress;

    @BindView(R.id.connected) LinearLayout connected;
    @BindView(R.id.disconnected) LinearLayout disconnected;

    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity" ;

    private CallbackManager callbackManager;

    private TwitterAuthClient authClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (!InternetConnection.checkInternetConnection(LoginActivity.this)){
            isDisconnected();
        }

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        authClient = new TwitterAuthClient();

        callbackManager = CallbackManager.Factory.create();

        initAutenticationGoogle();

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApp.getInstance().setConnectivityListener(LoginActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.btn_google)
    public void handleSessionGoogle(){
        signIn();
    }

    @OnClick(R.id.btn_fb)
    public void handleSessionFb(){
        signInFb();
    }

    @OnClick(R.id.btn_twitter)
    public void handleSessionTwitter(){
        signInTwitter();
    }

    @OnClick(R.id.btn_sesion)
    public void handleInitSession(){
        if (editEmail.getText().length() == 0)
            return ;
        if (editPassword.getText().length() == 0)
            return;

        initSession(editEmail.getText().toString(), editPassword.getText().toString());
    }

    @OnClick(R.id.btn_register)
    public void handleRegister(){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }

    private void initSession(String email, String password){
        rlaProgress.setVisibility(View.VISIBLE);
        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmailAndPassword:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()){
                    Log.w(TAG, "signInWithEmailAndPassword", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    rlaProgress.setVisibility(View.GONE);
                }else{
                    rlaProgress.setVisibility(View.GONE);
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                }
            }
        });

    }

    private void signInTwitter(){
        rlaProgress.setVisibility(View.VISIBLE);
        authClient.authorize(LoginActivity.this , new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                rlaProgress.setVisibility(View.GONE);
                initAutetnticationTwitter(result.data);
                requerirEmailTwitter(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "twitterLogin:failure" + exception);
                rlaProgress.setVisibility(View.GONE);
            }
        });
    }

    private void requerirEmailTwitter(TwitterSession session){
        authClient.requestEmail(session, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.updateEmail(result.data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()){
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            rlaProgress.setVisibility(View.GONE);
                        }else{
                            rlaProgress.setVisibility(View.GONE);
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        }
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                Log.v(TAG, "twitterLOgin:failure-email" + exception);
            }
        });
    }

    private  void initAutetnticationTwitter(final TwitterSession session){
        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,
                session.getAuthToken().secret);
        rlaProgress.setVisibility(View.VISIBLE);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(LoginActivity.this,
                new OnCompleteListener<AuthResult>() {
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
                }
            }
        });

    }

    private void signInFb(){
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                initAutenticationFbToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel:");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError:" + error);
            }
        });

        Collection<String>  mPermisions = new ArrayList<>();
        mPermisions.add("public_profile");
        mPermisions.add("user_friends");
        mPermisions.add("email");
        LoginManager.getInstance().logInWithReadPermissions(this, mPermisions);
    }

    private void initAutenticationFbToken(AccessToken accessToken){
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        rlaProgress.setVisibility(View.VISIBLE);
        mFirebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(LoginActivity.this,
                new OnCompleteListener<AuthResult>() {
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
                }else{
                    rlaProgress.setVisibility(View.GONE);
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                }
            }
        });
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

        // Fb
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // twitter
        authClient.onActivityResult(requestCode, resultCode, data);
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

    public void isConnected() {
        connected.setVisibility(View.VISIBLE);
        disconnected.setVisibility(View.GONE);
    }

    public void isDisconnected() {
        disconnected.setVisibility(View.VISIBLE);
        connected.setVisibility(View.GONE);
    }

    @OnClick(R.id.img_close_connected)
    public void handleCloseConnected(){
        animationNetwork(connected);
        connected.setVisibility(View.GONE);
    }

    @OnClick(R.id.img_close_disconnected)
    public void handleCloseDisconnected(){
        animationNetwork(disconnected);
        disconnected.setVisibility(View.GONE);
    }

    public void animationNetwork(LinearLayout layout){
        /*AnimationSet set = new AnimationSet(true);
        Animation animation = null;

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        animation.setDuration(1000);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);*/
        if (layout != null) {
            //layout.setLayoutAnimation(controller);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.ocultar);
            layout.startAnimation(animation);
        }

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected){
            isConnected();
        }else isDisconnected();
    }
}
