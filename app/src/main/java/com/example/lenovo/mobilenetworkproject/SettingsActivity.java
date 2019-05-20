package com.example.lenovo.mobilenetworkproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText userName, userProfName, userStatus, userCountry, userGender, userRelation, userDob;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfImage;
    private ProgressDialog loadingBar;


    private DatabaseReference SettingsUsersRef;
    private FirebaseAuth mAuth;
    private String CurrentUserID;

   // private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    final static int Gallery_Pick =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadingBar = new ProgressDialog(this);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        SettingsUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.settings_username);
        userProfName = findViewById(R.id.settings_profile_fullname);
        userStatus = findViewById(R.id.settings_status);
        userCountry = findViewById(R.id.settings_country);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relationship_status);
        userDob = findViewById(R.id.settings_db);
        UpdateAccountSettingsButton = findViewById(R.id.update_account_settings_button);
        userProfImage = findViewById(R.id.settings_profile_image);


        SettingsUsersRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);
                    userDob.setText(myDOB);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });


        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidateAccountInfo();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) //result of the picking an image
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()  //crop the image
                    .setGuidelines(com.theartofdev.edmodo.cropper.CropImageView.Guidelines.ON)
                    .start(this);

        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, updating your profile image");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(CurrentUserID + "jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile Image stored succesfully", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().toString();
                            SettingsUsersRef.child("profileImage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {

                                            if(task.isSuccessful())
                                            {

                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SettingsActivity.this, "Image stored in database successfully", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Error occured"+message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                        }
                    }
                });

            }
            else
            {
                Toast.makeText(this, "Error occured: Image can't be cropped. Try again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void ValidateAccountInfo()
    {
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String country = userCountry.getText().toString();
        String dob = userDob.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(profilename))
        {
            Toast.makeText(this, "Please write profile name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Please write status ", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write country ", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(dob))
        {
            Toast.makeText(this, "Please write date of birth", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "Please write gender", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relation))
        {
            Toast.makeText(this, "Please write relationship status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, updating your profile image");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username,profilename,status,country,dob,gender,relation);
        }



    }

    private void UpdateAccountInfo(String username, String profilename, String status, String country, String dob, String gender, String relation)
    {
        HashMap userMap = new HashMap();
        userMap.put("Username", username);
        userMap.put("countryName", country);
        userMap.put("dob", dob);
        userMap.put("fullname", profilename);
        userMap.put("gender", gender);
        userMap.put("status", status);
        userMap.put("relationship_status", relation);
        SettingsUsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) 
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(SettingsActivity.this, "Account settings updates successfully..", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                    loadingBar.dismiss();
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "Error occured while updating your account information ", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });



    }


    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}















