package com.example.fireblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class RegisterActivity extends AppCompatActivity {
    EditText mName, mEmail, mPassword,mCpassword;
    Button mButton, mNext;
    FirebaseAuth mAuth;
    ProgressDialog  progressDialog;
    DatabaseReference mRef;
    //DatabaseReference mRefUsers;
    LinearLayout profileLayout, registerLayout;
    ImageView imageView;
    private static  int GALLERYCODE = 1;
    Uri imageUri = null;
    StorageReference imageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mCpassword = findViewById(R.id.cpassword);
        mButton = findViewById(R.id.signup);
        mNext = findViewById(R.id.next);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        profileLayout = findViewById(R.id.accountSetup);
        registerLayout  = findViewById(R.id.registerLayout);
        profileLayout.setVisibility(View.GONE);
        imageView = findViewById(R.id.profilePic);
        imageStorage = FirebaseStorage.getInstance().getReference().child("Profile_Images");


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegistration();
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check();


            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERYCODE);

            }
        });
    }

    private void startRegistration(){

        final String name = mName.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();
        String cpassword = mCpassword.getText().toString().trim();
        if(name.isEmpty()){
            mName.setError("Fill this field");
            return;

        }else if(email.isEmpty()){
            mEmail.setError("Fill this field");
            return;

        }else if(password.isEmpty()){
            mPassword.setError("Fill this field");
            return;

        }else if(!password.equals(cpassword)){
            mPassword.setError("Password dos'nt match confirm password");
            return;

        }else if(imageUri == null){
            Toast.makeText(RegisterActivity.this, "Please select a Profile picture", Toast.LENGTH_LONG).show();
            return;

        }else{
            progressDialog.setTitle("Signing Up...");
            progressDialog.show();

            final StorageReference filePath = imageStorage.child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i("check","succcc");

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            createUser(name,email,password,uri.toString());

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "An Error Occoured", Toast.LENGTH_LONG).show();

                        }
                    });

                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "An Error Occoured", Toast.LENGTH_LONG).show();


                }
            });



        }

    }

    private void createUser(final String name, final String email, final String password, final String imageurl){
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                  mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {

                          String currentId = mAuth.getCurrentUser().getUid();
                          DatabaseReference Uid = mRef.child(currentId);
                          Uid.child("Name").setValue(name);
                          Uid.child("Email").setValue(email);
                          Uid.child("Image").setValue(imageurl);

                          progressDialog.dismiss();
                          Intent i = new Intent(RegisterActivity.this,MainActivity.class);
                          i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                          startActivity(i);

                      }
                  });

                }else{
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Error occoured If you already have and account, SignIn instead", Toast.LENGTH_LONG).show();

                }

            }
        });
    }
    private void check(){
        final String name = mName.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String cpassword = mCpassword.getText().toString().trim();
        if(name.isEmpty()){
            mName.setError("Fill this field");
            return;

        }else if(email.isEmpty()){
            mEmail.setError("Fill this field");
            return;

        }else if(password.isEmpty()){
            mPassword.setError("Fill this field");
            return;

        }else if(!password.equals(cpassword)){
            mPassword.setError("Password dosent match confirm password");
            return;

        }else{
            registerLayout.setVisibility(View.GONE);
            profileLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERYCODE && resultCode == RESULT_OK){
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);
            Uri uri = data.getData();

// start cropping activity for pre-acquired image saved on the device
            CropImage.activity(uri)
                    .start(this);



        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

               imageUri = result.getUri();
                imageView.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
