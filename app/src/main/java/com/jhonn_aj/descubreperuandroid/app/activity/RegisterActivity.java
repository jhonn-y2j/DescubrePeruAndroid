package com.jhonn_aj.descubreperuandroid.app.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jhonn_aj.descubreperuandroid.MyApp;
import com.jhonn_aj.descubreperuandroid.R;
import com.jhonn_aj.descubreperuandroid.app.utils.InternetConnection;
import com.jhonn_aj.descubreperuandroid.app.utils.NetworkChangeReceiver;
import com.jhonn_aj.descubreperuandroid.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by jhonn_aj on 19/03/2017.
 */

public class RegisterActivity extends AppCompatActivity implements NetworkChangeReceiver.receiverListener{

    @BindView(R.id.llaBack) View llaBack;
    @BindView(R.id.edit_name) EditText edit_name;
    @BindView(R.id.edit_email) EditText edit_email;
    @BindView(R.id.edit_password) EditText edit_password;
    @BindView(R.id.edit_phone) EditText edit_phone;
    @BindView(R.id.image_profile) ImageView img_profile;
    @BindView(R.id.btn_image_profile) Button btn_img_profile;
    @BindView(R.id.btn_register_users) Button btn_register_users;
    @BindView(R.id.rlaProgress) View rlaProgress;

    @BindView(R.id.connected) View connected;
    @BindView(R.id.disconnected) View disconnected;

    @BindView(R.id.img_close_connected) ImageButton close_connected;
    @BindView(R.id.img_close_disconnected) ImageButton close_disconnected;

    private int PICK_IMAGE_REQUEST = 1;

    private int SELECT_PICTURE = 300;

    public static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        if (!InternetConnection.checkInternetConnection(RegisterActivity.this)){
            isDisconnected();
        }

    }

    @OnClick(R.id.llaBack)
    public void handleBack(){
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApp.getInstance().setConnectivityListener(RegisterActivity.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    @OnClick(R.id.btn_register_users)
    public void handlerRegisterUser(){
        registerUser();
    }

    private void registerUser(){
        if (validateFields()){
            rlaProgress.setVisibility(View.VISIBLE);
            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
            mFirebaseAuth.createUserWithEmailAndPassword(edit_email.getText().toString(),
                    edit_password.getText().toString()).addOnCompleteListener(RegisterActivity.this,
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Create cuenta Failled", Toast.LENGTH_SHORT).show();
                                rlaProgress.setVisibility(View.GONE);
                            }else{
                                final FirebaseUser userFirebase = task.getResult().getUser();
                                User user = new User(userFirebase.getUid(), edit_name.getText().toString(),
                                        edit_email.getText().toString(), edit_password.getText().toString(),
                                        null, edit_phone.getText().toString(), Calendar.getInstance().getTime().toString());

                                FirebaseDatabase.getInstance().getReference("users").child(userFirebase.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null){
                                            String key = databaseReference.getKey();
                                            StorageReference storageReference =
                                                    FirebaseStorage.getInstance()
                                                            .getReference("profiles/").child(key);

                                            putImageProfile(storageReference, userFirebase, key);

                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();

                                        }else {
                                            Log.w(TAG, "Unable to write message to database.",
                                                    databaseError.toException());
                                        }
                                    }
                                });
                                rlaProgress.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void putImageProfile(final StorageReference storageReference, final FirebaseUser userFirebase, final String key){
        img_profile.setDrawingCacheEnabled(true);
        img_profile.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        img_profile.layout(0, 0, img_profile.getMeasuredWidth(), img_profile.getMeasuredHeight());
        img_profile.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(img_profile.getDrawingCache());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.v( TAG, "LoadFirebaseStorage:onFailled " + exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.v( TAG, "LoadFirebaseStorage:onCompleted ");
                updateProfileFirebase(storageReference,userFirebase, key);
            }
        });
    }

    private void updateProfileFirebase(StorageReference storageReference , final FirebaseUser firebaseUser, final String key){
        storageReference.getDownloadUrl().addOnSuccessListener(RegisterActivity.this, new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(edit_name.getText().toString())
                                .setPhotoUri(uri)
                                .build();

                        firebaseUser.updateEmail(edit_email.getText().toString());

                        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.v(TAG, "Perfil actualizado");
                            }
                        });

                        User user = new User(firebaseUser.getUid(), edit_name.getText().toString(),
                                edit_email.getText().toString(), edit_password.getText().toString(),
                                uri.toString(), edit_phone.getText().toString(), Calendar.getInstance().getTime().toString());

                        Log.v(TAG, "uri" + uri.toString());
                        FirebaseDatabase.getInstance().getReference("users").child(key).setValue(user);
                    }
                }
        );
    }


    private boolean validateFields(){

        if (edit_name.getText().length() == 0)
            return false;

        if (edit_phone.getText().length() < 9)
            return false;

        if (edit_password.getText().length() < 8)
            return false;

        if (!Patterns.EMAIL_ADDRESS.matcher(edit_email.getText().toString()).matches())
            return false;

        return true;
    }

    @OnClick(R.id.btn_image_profile)
    public void handleImageProfile(){
        openViewGallery();
    }

    private void openViewGallery(){
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST); */

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent.createChooser(intent, "Seleccionar imagen"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                img_profile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        if (resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                img_profile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void isConnected() {
        connected.setVisibility(View.VISIBLE);
        disconnected.setVisibility(View.GONE);
    }

    public void isDisconnected() {
        disconnected.setVisibility(View.VISIBLE);
        connected.setVisibility(View.GONE);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected){
            isConnected();
        }else isDisconnected();
    }

    @OnClick(R.id.img_close_connected)
    public void handleCloseConnected(){
        connected.setVisibility(View.GONE);
    }

    @OnClick(R.id.img_close_disconnected)
    public void handleCloseDisconnected(){
        disconnected.setVisibility(View.GONE);
    }
}
