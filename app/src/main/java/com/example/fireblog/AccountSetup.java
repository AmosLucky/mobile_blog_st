package com.example.fireblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class AccountSetup extends AppCompatActivity {
    private static  int GALLERYCODE = 1;
    ImageView imageView;
    Uri imageUri = null;
    StorageReference imageStorage;
    Button button;
    EditText mName;
    DatabaseReference mRef;

    FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        imageView = findViewById(R.id.profilePic);
        imageStorage = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        button = findViewById(R.id.finish);
        mName = findViewById(R.id.name);
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        progressDialog = new ProgressDialog(this);



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERYCODE);


            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = mName.getText().toString().trim();
                if(name.isEmpty()){
                    mName.setError("Fill this field");
                    return;

                }
               else if(imageUri == null){
                    Toast.makeText(AccountSetup.this, "Please select a Profile picture", Toast.LENGTH_LONG).show();
                    return;

                }else{
                    final StorageReference filePath = imageStorage.child(imageUri.getLastPathSegment());
                    filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //createUser(name,email,password,uri.toString());
                                    String currentId = mAuth.getCurrentUser().getUid();
                                    DatabaseReference Uid = mRef.child(currentId);
                                    Uid.child("Name").setValue(name);
                                   // Uid.child("Email").setValue(email);
                                    Uid.child("Image").setValue(imageUri);

                                    progressDialog.dismiss();
                                    Intent i = new Intent(AccountSetup.this,MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    startActivity(i);

                                }
                            });

                        }
                    });


                }


            }
        });
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
