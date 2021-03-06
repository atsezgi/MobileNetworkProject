package com.example.lenovo.mobilenetworkproject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDob;
    private CircleImageView userprofImage;

    private Button SendFriendRequestButton, DeclineFriendRequestButton;

    private DatabaseReference ProfileUserRef, UsersRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        senderUserId = mAuth.getUid();

        InitializeFields();


        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    String myUserName = dataSnapshot.child("Username").getValue().toString();
                    String myProfileName= dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("countryName").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationship_status").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userprofImage);
                    userName.setText("@" + myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);
                    userRelation.setText("Relationship: " + myRelationStatus);
                    userDob.setText("DOB: " + myDOB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendRequestButton.setEnabled(false);
                }
            });
        }
        else
        {
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }


    }

    private void InitializeFields()
    {

        SendFriendRequestButton = findViewById(R.id.person_send_friend_request_button);
        DeclineFriendRequestButton = findViewById(R.id.person_decline_friend_request_button);
        userName = findViewById(R.id.person_username);
        userProfName = findViewById(R.id.person_full_name);
        userStatus = findViewById(R.id.person_profile_status);
        userCountry = findViewById(R.id.person_country);
        userGender = findViewById(R.id.person_gender);
        userRelation = findViewById(R.id.person_relation);
        userDob = findViewById(R.id.person_dob);
        userprofImage = findViewById(R.id.person_profile_pic);
        CURRENT_STATE = "not_friends";
    }
}
