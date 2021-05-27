package com.example.fireblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostPage extends AppCompatActivity {
    ImageButton selectImage;
    EditText title, body;
    Button submit;
    private  static  final int  Galary_Request = 1;
    Uri imageUri = null;
    StorageReference storageRef;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    FirebaseAuth.AuthStateListener mAuthListener;
    String formattedDate;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);
        selectImage = findViewById(R.id.selectImage);
        title = findViewById(R.id.title);
        body = findViewById(R.id.body);
        submit = findViewById(R.id.submit);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Posting ...");

        storageRef = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        SimpleDateFormat curFormater = new SimpleDateFormat("dd/MM/yyyy");


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
                    // User is signed in

                } else {
                    // User is signed out
                   startActivity(new Intent(PostPage.this, LoginActivity.class));
                }
                // ...
            }
        };

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());


        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
         formattedDate = df.format(c);
//        try {
//            dateObj = curFormater.parse(dateStr);
//            SimpleDateFormat postFormater = new SimpleDateFormat("MMMM dd, yyyy");
//
//            newDateStr = postFormater.format(dateObj);
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }




        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galeryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galeryIntent.setType("image/*");
                startActivityForResult(galeryIntent,Galary_Request);


            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartPosting();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Galary_Request && resultCode == RESULT_OK){
             imageUri = data.getData();
//            try {
//                InputStream inputStream = getContentResolver().openInputStream(imageUri);
//              Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//              selectImage.setImageBitmap(bitmap);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
            selectImage.setImageURI(imageUri);
        }else{
            Toast.makeText(this, "ERORORORORO",Toast.LENGTH_LONG).show();
        }
    }

    private void  StartPosting(){

        progressDialog.show();
        final String title_val = title.getText().toString();
        final String body_val = body.getText().toString();
        if(!title_val.isEmpty() && !body_val.isEmpty() && imageUri != null){

            final StorageReference filepath = storageRef.child("Blog_Image").child(imageUri.getLastPathSegment());
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                           final String url = uri.toString();
                           // UploadTask upload = new UploadTask(et_localization,url);
                          userRef.addValueEventListener(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot snapshot) {
                                  DatabaseReference post = databaseReference.push();
                                  post.child("image").setValue(url);
                                  post.child("body").setValue(body_val);
                                  post.child("title").setValue(title_val);
                                  post.child("date").setValue(formattedDate);
                                  post.child("uid").setValue(firebaseUser.getUid());
                                  post.child("username").setValue(snapshot.child("Name").getValue());
                                  post.child("user_photo").setValue(snapshot.child("Image").getValue());

                                  title.setText("");
                                  body.setText("");
                                  selectImage.setImageResource(R.drawable.photos_icon);
                                  progressDialog.dismiss();

                                  if(!progressDialog.isShowing()){
                                      startActivity(new Intent(PostPage.this,MainActivity.class));
                                  }


                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError error) {
                                  progressDialog.dismiss();

                              }

                          });
                        }
                    });

                }
            });

        }else{
            Toast.makeText(this, "Please Fill the Whole Form Before Posting",Toast.LENGTH_LONG).show();

        }


    }

    @Override
    public void onDestroy(){
        super.onDestroy();

            progressDialog.dismiss();

    }
}
