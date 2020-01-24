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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private static  final  int GalleryPick=1;
    private Uri ImageUri;
    private String Description;

    private StorageReference PostImagesRefrence;
    private DatabaseReference UsersRef,PostsRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName,downloadUrl,current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBl));

        mAuth = FirebaseAuth.getInstance();
        current_user_id= mAuth.getCurrentUser().getUid();

        PostImagesRefrence = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        SelectPostImage=findViewById(R.id.select_post_image);
        UpdatePostButton=findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo() {
         Description = PostDescription.getText().toString();
        if (ImageUri == null){
            Toast.makeText(this, "Please Select post image...", Toast.LENGTH_SHORT).show();
        }
       else if (TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait,While we updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }

    }

    private void StoringImageToFirebaseStorage() {

        Calendar calFordate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currenTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currenTime.format(calFordate.getTime());

        postRandomName = saveCurrentDate+saveCurrentTime;

        StorageReference filePath = PostImagesRefrence.child("Post Image").child(ImageUri.getLastPathSegment() + postRandomName +  ".jpg");
        StorageTask<UploadTask.TaskSnapshot> taskSnapshotStorageTask = filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
              if (task.isSuccessful()){

                  Toast.makeText(PostActivity.this, "Image uploaded succesfully to storage...", Toast.LENGTH_SHORT).show();
                  StorageReference filePath = PostImagesRefrence.child("Post Image").child(ImageUri.getLastPathSegment() +postRandomName + ".jpg");
                  filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                      @Override
                      public void onSuccess(Uri uri) {
                          String downlooadPicUrl = uri.toString();

                          SavingPostInformationToDatabase(downlooadPicUrl);

                      }
                  });



              }
              else {
                  String message= task.getException().getMessage();
                  Toast.makeText(PostActivity.this, "Error ocurred: " + message, Toast.LENGTH_SHORT).show();
              }
            }
        });


    }

    private void SavingPostInformationToDatabase(final String download) {
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if (dataSnapshot.exists()){
                  String userFullName = dataSnapshot.child("fullname").getValue().toString();
                  String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                  HashMap postsMap = new HashMap();
                  postsMap.put("uid",current_user_id);
                  postsMap.put("date",saveCurrentDate);
                  postsMap.put("time",saveCurrentTime);
                  postsMap.put("description",Description);
                  postsMap.put("postimage",download);
                  postsMap.put("profileimage",userProfileImage);
                  postsMap.put("fullname",userFullName);
                  PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                          .addOnCompleteListener(new OnCompleteListener() {
                              @Override
                              public void onComplete(@NonNull Task task) {
                                  if (task.isSuccessful()){
                                      SendUserToMainActivity();

                                      Toast.makeText(PostActivity.this, "New Post is updated successfully..", Toast.LENGTH_SHORT).show();
                                      loadingBar.dismiss();
                                  }
                                  else {
                                      Toast.makeText(PostActivity.this, "Error Ocurred While updating your profile", Toast.LENGTH_SHORT).show();
                                      loadingBar.dismiss();
                                  }
                              }
                          });
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}
