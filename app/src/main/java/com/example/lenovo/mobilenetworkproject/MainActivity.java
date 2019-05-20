package com.example.lenovo.mobilenetworkproject;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageButton AddNewPostButton;

    private CircleImageView NavProfileImage;
    private TextView navProfileUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, Postsref, LikesRef ;

    String currentUserId;
    Boolean likeChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Postsref = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mToolbar =(Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton =findViewById(R.id.add_new_post_button);

        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigation_view);

        postList = findViewById(R.id.all_users_post_list);
      //  postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); //for the new posts be top
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = navView.findViewById(R.id.nav_profile_image);
        navProfileUserName = navView.findViewById(R.id.nav_user_full_name);



        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        navProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileImage"))
                    {
                        String image = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                        // Glide.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile name do not exist", Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                UserMenuSelecter(item);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToPostActivity();
            }
        });

        DisplayAllUsersPost();
    }

    private void DisplayAllUsersPost()
    {
        Query sortPostInDescendingOrder = Postsref.orderByChild("counter");


        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(sortPostInDescendingOrder,Posts.class).build();

        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostViewHolder>
                        (options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Posts model)
                    {
                        final String PostKey = getRef(position).getKey();

                        holder.username.setText(model.getFullname());
                        holder.time.setText(model.getTime());
                        holder.date.setText(model.getDate());
                        holder.descrption.setText(model.getDescrption());
                        Picasso.get().load(model.getProfileImage()).into(holder.user_profile_image);
                        //Picasso.get().load(model.getPostimage()).into(holder.post_image);
                        Picasso.get().load(model.getPostimage()).into(holder.post_image);

                        holder.setLikeButtonStatus(PostKey);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("PostKey",PostKey);
                                startActivity(commentsIntent);
                            }
                        });

                        holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                likeChecker = true;
                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                      if(likeChecker.equals(true))
                                      {
                                          if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                                          {
                                              LikesRef.child(PostKey).child(currentUserId).removeValue();
                                              likeChecker = false;
                                          }
                                          else
                                          {
                                              LikesRef.child(PostKey).child(currentUserId).setValue(true);
                                              likeChecker = false;
                                          }
                                      }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout, viewGroup,false);
                        PostViewHolder viewHolder = new PostViewHolder(view);
                        return viewHolder;
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        TextView username, date, time,descrption, displayNoofLikes;
        ImageView post_image, user_profile_image;
        ImageButton LikePostButton, CommentPostButton;

        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;
        public PostViewHolder(@NonNull View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.post_user_name);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
            descrption = itemView.findViewById(R.id.post_description);
            post_image = itemView.findViewById(R.id.post_image);
            user_profile_image = itemView.findViewById(R.id.post_profile_image);
            LikePostButton = itemView.findViewById(R.id.like_button);
            CommentPostButton = itemView.findViewById(R.id.comment_button);
            displayNoofLikes = itemView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        }

        public void setLikeButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                    {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount(); //count the number of likes
                        LikePostButton.setImageResource(R.drawable.like);
                        displayNoofLikes.setText((Integer.toString(countLikes))
                                + (" Likes"));
                    }
                    else
                    {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount(); //count the number of likes
                        LikePostButton.setImageResource(R.drawable.dislike);
                        displayNoofLikes.setText((Integer.toString(countLikes))
                                + (" Likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        /*
        public void setFullname(String fullname)
        {
            TextView username =mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileImage(String profileImage)
        {
            ImageView imageView = mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileImage).into(imageView);


        }
        public void setTime(String time)
        {
            TextView post_time =mView.findViewById(R.id.post_time);
            post_time.setText("  " +time);
        }
        public void setDate(String date)
        {
            TextView post_date =mView.findViewById(R.id.post_date);
            post_date.setText("  "+date);
        }
        public void setDescrption(String descrption)
        {
            TextView post_desc =mView.findViewById(R.id.post_description);
            post_desc.setText(descrption);
        }
        public void setPostimage(Context ctx, String postimage)
        {
            ImageView PostImage = mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(PostImage);
        }*/

    }

    private void SendUserToPostActivity()
    {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postIntent);
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence()
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelecter(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_Logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
        }
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToProfileActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(settingsIntent);
    }
    private void SendUserToFindFriendsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(settingsIntent);
    }
}
