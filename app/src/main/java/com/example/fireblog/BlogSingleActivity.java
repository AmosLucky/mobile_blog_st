package com.example.fireblog;

import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BlogSingleActivity extends AppCompatActivity {
    String post_key,title,body,image,author1,author_photo,post_date;
    ImageView imageView,userphoto;
    TextView titleView, bodyView,date,author;
    Button commentBtn;
    EditText commenttext;
    DatabaseReference commentRef;
    FirebaseUser firebaseUser;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String formattedDate;
    RecyclerView comentRecycle;
    boolean is = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);
        post_key = getIntent().getStringExtra("post_key");
        title = getIntent().getStringExtra("title");
        body = getIntent().getStringExtra("body");
        image = getIntent().getStringExtra("image");
        author1 = getIntent().getStringExtra("author");
        author_photo = getIntent().getStringExtra("author_photo");
        post_date = getIntent().getStringExtra("date");
        //Log.i("post_key",post_key);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        commentRef = FirebaseDatabase.getInstance().getReference().child("Comments");
        commentRef.child(post_key).keepSynced(true);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());

        //Toast.makeText(BlogSingleActivity.this, author_photo +" "+post_date+" "+author1, Toast.LENGTH_LONG).show();

        titleView = findViewById(R.id.title);
        bodyView = findViewById(R.id.body);
        imageView = findViewById(R.id.image);
        date = findViewById(R.id.date);
        author = findViewById(R.id.username);
        userphoto = findViewById(R.id.users_photo);
        commentBtn = findViewById(R.id.submit);
        commenttext = findViewById(R.id.commenttext);
        comentRecycle = findViewById(R.id.comments);

        /////////////////////////
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        comentRecycle.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        /////////////////////////////////


        titleView.setText(title);
        bodyView.setText(body);
        author.setText(author1);
        date.setText(post_date);
        Picasso.get().load(image).into(imageView);
        Picasso.get().load(author_photo).into(userphoto);


        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        formattedDate = df.format(c);


        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              final String comment =  commenttext.getText().toString();
              if(comment.length() < 2){
                  commenttext.setError("Enter a valid Comment");
              }else{

                  //////////////////////////////////////////////////

                  userRef.addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {
                          DatabaseReference commentKey = commentRef.child(post_key).push();
                          commentKey.child("comment").setValue(comment);
                          commentKey.child("username").setValue(snapshot.child("Name").getValue());
                          commentKey.child("user_photo").setValue(snapshot.child("Image").getValue());
                          commentKey.child("date").setValue(formattedDate);
                          commentKey.child("uid").setValue(firebaseUser.getUid());
                          commentKey.child("postid").setValue(post_key);
                          commenttext.setText("");
                          Toast.makeText(BlogSingleActivity.this,"Comment Successful",Toast.LENGTH_LONG).show();

                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {

                      }
                  });
              }

            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<CommentModel> options =
                new FirebaseRecyclerOptions.Builder<CommentModel>()
                        .setQuery(commentRef.child(post_key), CommentModel.class)
                        .build();


        FirebaseRecyclerAdapter<CommentModel, CommentViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<CommentModel,CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull CommentModel model) {
                getRef(position).getKey();
              // if(model.getPostid().equals(post_key)){
                 //  is = true;
                   holder.setUsername(model.getUsername());
                   holder.setImage(model.getUser_photo());
                   holder.setDate(model.getDate());
                   holder.setComment(model.getComment());

             //  }





            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View view = inflater.inflate(R.layout.comment_layout,parent,false);

                return new CommentViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        comentRecycle.setAdapter(firebaseRecyclerAdapter);
    }

    public static class  CommentViewHolder extends RecyclerView.ViewHolder{
        View itemView;


        public CommentViewHolder(@NonNull View itemView1) {
            super(itemView1);
            itemView = itemView1;
        }

        public  void  setComment(String comment){
            TextView textView = itemView.findViewById(R.id.comment);
            textView.setText(comment);

        }

        public  void  setDate(String date){
            TextView textView = itemView.findViewById(R.id.date);
            textView.setText(date);

        }

        public  void  setUsername(String username){
            TextView textView = itemView.findViewById(R.id.username);
            textView.setText(username);

        }

        public  void  setImage(String image){
            ImageView imageView = itemView.findViewById(R.id.users_photo);
          //  ImageView imageView = mview.findViewById(R.id.image);
            Picasso.get()
                    .load(image)
                    .into(imageView);

        }

        public  void  postid(String postid){


        }
    }
}
