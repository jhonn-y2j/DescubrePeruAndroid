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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jhonn_aj.descubreperuandroid.R;
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

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.llaBack) View llaBack;
    @BindView(R.id.edit_name) EditText edit_name;
    @BindView(R.id.edit_email) EditText edit_email;
    @BindView(R.id.edit_password) EditText edit_password;
    @BindView(R.id.edit_phone) EditText edit_phone;
    @BindView(R.id.image_profile) ImageView img_profile;
    @BindView(R.id.btn_image_profile) Button btn_img_profile;
    @BindView(R.id.btn_register_users) Button btn_register_users;
    @BindView(R.id.rlaProgress) View rlaProgress;

    private int PICK_IMAGE_REQUEST = 1;

    private int SELECT_PICTURE = 300;

    private FirebaseStorage storage ;

    private StorageReference storageReference;

    public static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://descubreperu-dff08.appspot.com");
    }

    @OnClick(R.id.llaBack)
    public void handleBack(){
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
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

                                //upImageProfile();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(edit_name.getText().toString())
                                        .setPhotoUri(Uri.parse("https://firebasestorage.googleapis.com/v0/b/descubreperu-dff08.appspot.com/o/" +
                                                "profiles%2Favatar.png?alt=media&token=8634c96d-4c58-4818-a0a7-3448bd151482"))
                                        //.setPhotoUri(storageReference.getDownloadUrl().getResult())
                                        .build();

                                userFirebase.updateEmail(edit_email.getText().toString());

                                userFirebase.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        User user = new User(userFirebase.getUid(), edit_name.getText().toString(), edit_email.getText().toString(),
                                                edit_password.getText().toString(), null,
                                                edit_phone.getText().toString(), Calendar.getInstance().getTime().toString());

                                        FirebaseDatabase.getInstance().getReference("users").child(userFirebase.getUid()).setValue(user);

                                        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            rlaProgress.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void upImageProfile(){
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
            }
        });
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
}
