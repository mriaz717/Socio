package com.example.muhammadriaz.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    final  static  int GalleryPick=1;

    private EditText userName,userProfName,userStatus,userCountry,userDOB,userRelation,userGender;
    private Button UpdateAcoountSettingsButton;
    private CircleImageView userProfImage;

    private DatabaseReference settingsUseRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBl));

        mAuth = FirebaseAuth.getInstance();
        currentUserId =mAuth.getCurrentUser().getUid();
        settingsUseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Image");
        loadingBar= new ProgressDialog(this);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.settings_username);
        userStatus = findViewById(R.id.settings_status);
        userProfName= findViewById(R.id.settings_profile_full_name);
        userCountry= findViewById(R.id.settings_country);
        userGender= findViewById(R.id.settings_gender);
        userDOB= findViewById(R.id.settings_dob);
        userRelation= findViewById(R.id.settings_relationship_status);

        UpdateAcoountSettingsButton = findViewById(R.id.update_account_settings_buttons);
        userProfImage = findViewById(R.id.settings_profile_image);

        settingsUseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UpdateAcoountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result =CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait,While we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri=result.getUri();
                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                StorageTask<UploadTask.TaskSnapshot> taskSnapshotStorageTask = filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "profile Image stored succesfully to firebase storage", Toast.LENGTH_SHORT).show();
                            StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl=uri.toString();
                                    //  Toast.makeText(SetupActivity.this, downloadUrl, Toast.LENGTH_SHORT).show();
                                    settingsUseRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){

                                                        Intent selfIntent = new Intent(SettingsActivity.this,SettingsActivity.class);
                                                        startActivity(selfIntent);
                                                        Toast.makeText(SettingsActivity.this, "profile image stored to Firebase succesfully", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                    else {
                                                        String message =task.getException().getMessage();
                                                        Toast.makeText(SettingsActivity.this, "Error Ocurred" + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });



                        }
                    }
                });

            }
            else {
                Toast.makeText(this, "Error ocurred Image can't be cropped try again", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }





    private void ValidateAccountInfo()
    {
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();
        
        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please Write your username....", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(profilename))
        {
            Toast.makeText(this, "Please Write your profile name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Please Write your status...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(dob))
        {
            Toast.makeText(this, "Please Write your date of birth...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please Write your Country...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "Please Write your gender...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relation))
        {
            Toast.makeText(this, "Please Write your relationship status...", Toast.LENGTH_SHORT).show();
        }
        else
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait,While we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

             UpdateAccountInfo(username,profilename,status,dob,gender,relation,country);
            }

    }

    private void UpdateAccountInfo(String username, String profilename, String status, String dob, String gender, String relation, String country)
    {
        HashMap userMap = new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",profilename);
        userMap.put("status",status);
        userMap.put("dob",dob);
        userMap.put("country",country);
        userMap.put("gender",gender);
        userMap.put("relationshipstatus",relation);
        settingsUseRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings Updated Successfully...", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Error Occured while updating account info...", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent= new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
