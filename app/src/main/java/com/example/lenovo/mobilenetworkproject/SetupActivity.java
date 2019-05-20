package com.example.lenovo.mobilenetworkproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.CropImageView;

import java.security.cert.Certificate;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText username, FullName, CountryName;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserId;
    final static int Gallery_Pick =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        loadingBar = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");


        username = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_fullname);
        CountryName = findViewById(R.id.setup_country_name);
        saveInformationButton = findViewById(R.id.setup_information_button);
        profileImage = findViewById(R.id.setup_profile_image);

        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                  SaveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  //reddirect to user to mobile phone gallery and pick an  image
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

      UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                    if(dataSnapshot.exists())
                    {
                        if(dataSnapshot.hasChild("profileImage"))
                        {
                            String image = dataSnapshot.child("profileImage").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                            // Glide.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profileImage);
                        }
                        else
                        {
                            Toast.makeText(SetupActivity.this, "Please select profile image", Toast.LENGTH_SHORT).show();
                        }


                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

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

                StorageReference filePath = UserProfileImageRef.child(currentUserId + "jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, "Profile Image stored succesfully", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().toString();
                            UsersRef.child("profileImage").setValue(downloadUrl)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {

                                    if(task.isSuccessful())
                                    {

                                        Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                        selfIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(selfIntent);

                                        Toast.makeText(SetupActivity.this, "Image stored in database succesfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error occured"+message, Toast.LENGTH_SHORT).show();
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

    private void SaveAccountSetupInformation()
    {
        String Username = username.getText().toString();
        String fullname = FullName.getText().toString();
        String countryName = CountryName.getText().toString();

        if(TextUtils.isEmpty(Username))
        {
            Toast.makeText(this,"Please write your username",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this,"Please write your Fullname",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(countryName))
        {
            Toast.makeText(this,"Please write your Country Name",Toast.LENGTH_SHORT).show();
        }else
        {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are saving your information");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("Username", Username);
            userMap.put("fullname", fullname);
            userMap.put("countryName", countryName);
            userMap.put("status", "Hey there i am coding android studio");
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationship_status", "none");

            UsersRef.updateChildren(userMap
            ).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this,"Your Account is succesfully created",Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this,"Error occured" +message,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                }
            });

        }

    }

    private void SendUserToMainActivity()
    {
                Intent setupIntent = new Intent(SetupActivity.this, MainActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
