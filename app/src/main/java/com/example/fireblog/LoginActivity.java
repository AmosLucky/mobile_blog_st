package com.example.fireblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    EditText mEmail,mPassword;
    Button mButton;
    FirebaseAuth mAuth;
 DatabaseReference mRefUsers;
 ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mButton = findViewById(R.id.signin);
        mAuth = FirebaseAuth.getInstance();
        mRefUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mRefUsers.keepSynced(true);


        progressDialog = new ProgressDialog(this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSingIn();

            }
        });
    }

    private void startSingIn(){
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        if(email.isEmpty()){
            mButton.setError("Fill this field");
            return;

        }else if(password.isEmpty()){
            mPassword.setError("Fill this field");
            return;

        }else{
            progressDialog.setTitle("SignIn ....");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                checkUserExists();
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                               // updateUI(user);
                            } else {
                                progressDialog.dismiss();
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                               //updateUI(null);
                                // ...
                            }

                            // ...
                        }
                    });

//                    signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if(task.isSuccessful()){
//
//                        checkUserExists();
//
//                    }else{
//                        progressDialog.dismiss();
//                        Toast.makeText(LoginActivity.this,"Error ocored", Toast.LENGTH_LONG).show();
//                    }
//
//                }
//            });

        }

    }

    private void checkUserExists(){
        mRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String uId = mAuth.getCurrentUser().getUid();
                if(snapshot.hasChild(uId)){
                    progressDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    fileList();

                }else{
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,"No user with this username and password", Toast.LENGTH_LONG).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });


    }

    public void Register(View view) {
        startActivity(new Intent(this,RegisterActivity.class));
    }

    @Override
    protected void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }
}
