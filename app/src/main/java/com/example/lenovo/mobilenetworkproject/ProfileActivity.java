package com.example.lenovo.mobilenetworkproject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDob;
    private CircleImageView userprofImage;

    private DatabaseReference ProfileUserRef;
    private FirebaseAuth mAuth;

    private String CurrentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        ProfileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);


        userName = findViewById(R.id.my_username);
        userProfName = findViewById(R.id.my_profile_full_name);
        userStatus = findViewById(R.id.my_profile_status);
        userCountry = findViewById(R.id.my_country);
        userGender = findViewById(R.id.my_gender);
        userRelation = findViewById(R.id.my_relation);
        userDob = findViewById(R.id.my_dob);
        userprofImage = findViewById(R.id.my_profile_pic);


        ProfileUserRef.addValueEventListener(new ValueEventListener() {
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

    }
}
