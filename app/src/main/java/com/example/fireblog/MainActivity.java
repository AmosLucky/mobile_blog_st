package com.example.fireblog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference mRef;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    DatabaseReference mRefUsers;
    FirebaseUser firebaseUser;
    private boolean processLike = false;
    DatabaseReference mDatabaseLike;
    Boolean isShowing = false;
    ProgressDialog progressDialog;
    DatabaseReference commentRef;
    //ImageView gif;
    FloatingActionButton floatingActionButton;
    Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // gif = findViewById(R.id.gif);

        progressDialog = new ProgressDialog(this);
        recyclerView = findViewById(R.id.blog_list);
        recyclerView.setHasFixedSize(true);
       LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRef = FirebaseDatabase.getInstance().getReference().child("Blog");
        query = mRef.orderByPriority();

        mAuth = FirebaseAuth.getInstance();
        mRefUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentRef = FirebaseDatabase.getInstance().getReference().child("Comments");

        mDatabaseLike.keepSynced(true);
        firebaseUser = mAuth.getCurrentUser();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();


                    return;


                }
            }
        };
        progressDialog.setTitle("Fetching posts...");
        progressDialog.show();
    }


    private void checkUserExists(){
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();


                    return;


                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();


        checkUserExists();
        mAuth.addAuthStateListener(authStateListener);
        FirebaseRecyclerOptions<BlogModel> options =
                new FirebaseRecyclerOptions.Builder<BlogModel>()
                        .setQuery(query, BlogModel.class)
                        .build();
        FirebaseRecyclerAdapter<BlogModel,BlogViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<BlogModel, BlogViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, int position, @NonNull final BlogModel model) {
                final String post_key = getRef(position).getKey();
                    progressDialog.dismiss();
                holder.setTitle(model.getTitle());
                holder.setDesc(model.getBody());
                holder.setImage(model.getImage());
                holder.setUsername(model.getUsername());
                holder.setDate(model.getDate());
                holder.setUser_photo(model.getUser_photo());
                holder.setLike_btn(post_key);
                holder.relative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                holder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        Log.i("post_key",post_key);
                       // Toast.makeText(MainActivity.this, post_key+"ppp", Toast.LENGTH_LONG).show();
                        Intent intent = new   Intent(MainActivity.this, BlogSingleActivity.class);
                        intent.putExtra("post_key",post_key);
                        intent.putExtra("title",model.getTitle());
                        intent.putExtra("body",model.getBody());
                        intent.putExtra("image",model.getImage());
                        intent.putExtra("author",model.getUsername());
                        intent.putExtra("author_photo",model.getUser_photo());
                        intent.putExtra("date",model.getDate());

                        startActivity(intent);

                    }
                });

                mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildrenCount() != 0){
                            holder.numOfLikes.setText(String.valueOf(snapshot.getChildrenCount()));
                        }else{holder.numOfLikes.setText("");}

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



                if(model.getUid().equalsIgnoreCase(mAuth.getCurrentUser().getUid())){
                   // Toast.makeText(MainActivity.this,model.getUid() +" " + mAuth.getCurrentUser().getUid(),Toast.LENGTH_LONG).show();

                    holder.options.setVisibility(View.VISIBLE);

                }else {
                    holder.options.setVisibility(View.GONE);
                }




                commentRef.child(post_key).keepSynced(true);

                commentRef.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildrenCount() != 0){
                            holder.numOfComments.setText(String.valueOf(snapshot.getChildrenCount()));
                        }else{holder.numOfComments.setText("");}

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                holder.like_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        processLike = true;


                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if(processLike){
                                    if (snapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();

                                        processLike = false;

                                    } else {
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Liked");
                                        processLike = false;

                                    }

                                }

                                }




                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });




                    }
                });

                holder.options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                        View view1 = inflater.inflate(R.layout.options_layout,holder.container,false);
                        LinearLayout edit = view1.findViewById(R.id.edit);
                        LinearLayout delete = view1.findViewById(R.id.delete);


                        edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new   Intent(MainActivity.this, EditPage.class);
                                intent.putExtra("post_key",post_key);
                                intent.putExtra("title",model.getTitle());
                                intent.putExtra("body",model.getBody());
                                intent.putExtra("image",model.getImage());
//                                intent.putExtra("author",model.getUsername());
//                                intent.putExtra("author_photo",model.getUser_photo());
//                                intent.putExtra("date",model.getDate());

                                startActivity(intent);


                            }
                        });

                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Delete entry")
                                        .setMessage("Are you sure you want to delete this entry?")

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Continue with delete operation
                                                mRef.child(post_key).removeValue();
                                            }
                                        })

                                        // A null listener allows the button to dismiss the dialog and take no further action.
                                        .setNegativeButton(android.R.string.no, null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();

                            }
                        });

                        if(isShowing){
                            isShowing = false;
                            holder.container.removeAllViews();

                        }else {
                            isShowing = true;
                            holder.container.addView(view1);

                        }

                    }
                });

                holder.share_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      // Uri imgUri = Uri.parse(model.getImage());
                        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                        whatsappIntent.setType("text/plain");
                        whatsappIntent.setPackage("com.whatsapp");
                        whatsappIntent.putExtra(Intent.EXTRA_TEXT,"*"+model.getTitle()+"* \n"+ model.getBody());
                        //whatsappIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
                       // whatsappIntent.setType("image/jpeg");
                        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        try {
                            MainActivity.this.startActivity(whatsappIntent);
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(MainActivity.this,"Whatsapp have not been installed.",Toast.LENGTH_LONG).show();
                        }
                    }
                });


                holder.users_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       // Toast.makeText(MainActivity.this,model.getUid(),Toast.LENGTH_LONG).show();
                    }
                });




            }







            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View view = inflater.inflate(R.layout.blog_row,parent,false);

                return new BlogViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public void Navigate(View view) {
        startActivity(new Intent(this,PostPage.class));
    }


    public static class  BlogViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout relative;
        DatabaseReference likeRef;
        FirebaseAuth likeAuth;
        View mview;
        ImageView like_btn;
        TextView numOfLikes;
        View options;
        LinearLayout container;
        TextView numOfComments;
        ImageView share_btn;
        ImageButton users_photo;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
             mview = itemView;
            like_btn = mview.findViewById(R.id.like_btn);
            numOfLikes = mview.findViewById(R.id.numOfLikes);
            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            likeAuth = FirebaseAuth.getInstance();
            likeRef.keepSynced(true);
            relative = mview.findViewById(R.id.relative);
            options = mview.findViewById(R.id.options);
            container = mview.findViewById(R.id.container);
            numOfComments = mview.findViewById(R.id.numOfComments);
            share_btn = mview.findViewById(R.id.share_btn);
            users_photo = mview.findViewById(R.id.users_photo);


        }
        public  void setLike_btn(final String post_key){

            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {


                    if(snapshot.child(post_key).hasChild(likeAuth.getCurrentUser().getUid())){
                        like_btn.setImageResource(R.drawable.ic_thumb_up_pink);

                    }else{
                        like_btn.setImageResource(R.drawable.ic_thumb_up);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        public  void  setUser_photo(String user_photo){

            ImageView imageView = mview.findViewById(R.id.users_photo);
            Picasso.get()
                    .load(user_photo)
                    .into(imageView);
        }

        public  void  setTitle(String title){
            TextView textView = mview.findViewById(R.id.title);
           if(title.length() >= 40){
               textView.setText(title.substring(0,40)+"...");
               return;
           }
            textView.setText(title);
        }
        public  void setDesc(String body){
            TextView textView = mview.findViewById(R.id.body);
           // textView.setText(body.substring(0,120)+"...");
            if(body.length() >= 120){
                textView.setText(body.substring(0,120)+"...");
                return;
            }
            textView.setText(body);
        }

        public  void setUsername(String username){
            TextView textView = mview.findViewById(R.id.post_username);
            textView.setText(username);
        }
        public  void setDate(String date){
            TextView textView = mview.findViewById(R.id.post_date);
            textView.setText(date);
        }
        public  void setImage(String image){
            ImageView imageView = mview.findViewById(R.id.image);
            Picasso.get()
                    .load(image)
                    .into(imageView);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_file,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.add:
                Intent intent = new Intent(this,PostPage.class);
                startActivity(intent);
                break;
            case R.id.logout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }


}
