package com.example.lenovo.mobilenetworkproject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;

    private String Post_Key, currentUserID ;

    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("PostKey").toString();


        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();



        CommentsList = findViewById(R.id.comments_list);
       // CommentsList.hasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = findViewById(R.id.comment_input);
        postCommentButton = findViewById(R.id.post_comment_btn);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("Username").getValue().toString();
                            ValidateComment(userName);
                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(PostsRef, Comments.class).build();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model)
                    {
                        holder.myUsername.setText("@"+model.getUsername()+"  ");
                        holder.Mycomment.setText(model.getComment());
                        holder.Mydate.setText("  Date: "+model.getDate());
                        holder.Mytime.setText("Time: "+model.getTime());
                    }

                    @NonNull
                    @Override
                    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout, viewGroup, false);
                        CommentsViewHolder commentsViewHolder = new CommentsViewHolder(view);
                        return commentsViewHolder;
                    }
                };
        CommentsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {

        private TextView Mycomment, Mydate, Mytime, myUsername;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            myUsername = itemView.findViewById(R.id.comment_username);
            Mydate = itemView.findViewById(R.id.comment_date);
            Mytime = itemView.findViewById(R.id.comment_time);
            Mycomment = itemView.findViewById(R.id.comment_text);

        }
    }





    private void ValidateComment(String userName)
    {
        String commentText = CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please write text to comment", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String SaveCurrentTime= currentTime.format(calForTime.getTime());

            final String RandomKey = currentUserID + saveCurrentDate +SaveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", currentUserID);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", SaveCurrentTime);
            commentsMap.put("username", userName);
            PostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(CommentsActivity.this, "You have commented succesfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(CommentsActivity.this, "Error occured, try again", Toast.LENGTH_SHORT).show();
                            }
                            
                        }
                    });
        }
    }
}
