package com.example.muhammadriaz.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
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

    private TextView userName,userProfName,userStatus,userCountry,userDOB,userRelation,userGender;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBl));

        mAuth = FirebaseAuth.getInstance();
        currentUserId =mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


        userName = findViewById(R.id.my_username);
        userStatus = findViewById(R.id.my_profile_status);
        userProfName= findViewById(R.id.my_profile_full_name);
        userCountry= findViewById(R.id.my_country);
        userGender= findViewById(R.id.my_gender);
        userDOB= findViewById(R.id.my_dob);
        userRelation= findViewById(R.id.my_relationship_status);
        userProfileImage = findViewById(R.id.my_profile_pic);

        profileUserRef.addValueEventListener(new ValueEventListener() {
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

                 Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                 userName.setText("@" + myUserName);
                 userProfName.setText(myProfileName);
                 userStatus.setText(myProfileStatus);
                 userDOB.setText("DOB: "+myDOB);
                 userCountry.setText("Country: "+myCountry);
                 userGender.setText("Gender: "+myGender);
                 userRelation.setText("Relation: "+myRelationStatus);
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
