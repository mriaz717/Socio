package com.example.muhammadriaz.socialnetwork;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;

import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView  postList;
    private Toolbar mToolbar;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;



    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,PostsRef,LikesRaf;
    String currentUserID;
    Boolean LikeChecker = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBl));

        mAuth = FirebaseAuth.getInstance();

        LikesRaf = FirebaseDatabase.getInstance().getReference().child("Likes");

        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

//        RecyclerView recyclerView = findViewById(R.id.all_users_post_list);
//        recyclerView.setHasFixedSize(true);
//        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
//        recyclerView.setLayoutManager(layoutManager);



        NavProfileImage = navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = navView.findViewById(R.id.nav_user_full_name);

        AddNewPostButton = findViewById(R.id.add_new_post_button);

        if (mAuth.getCurrentUser()!=null)
        {
            currentUserID = mAuth.getCurrentUser().getUid();
            UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChild("fullname")) {
                            String fullname = dataSnapshot.child("fullname").getValue().toString();
                            NavProfileUserName.setText(fullname);
                        }
                        if (dataSnapshot.hasChild("profileimage")) {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                        }
                        else {
                            Toast.makeText(MainActivity.this, "profile name do not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });


        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });


     DisplayAllUsersPosts();

    }

    private void DisplayAllUsersPosts() {


        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                (Posts.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        PostsRef

                ) {
            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {

             final String PostKey= getRef(position).getKey();

               viewHolder.setFullname(model.getFullname());
               viewHolder.setTime(model.getTime());
               viewHolder.setDate(model.getDate());
               viewHolder.setDescription(model.getDescription());
               viewHolder.setProfileimage(model.getProfileimage());
               viewHolder.setPostimage(model.getPostimage());

               viewHolder.setLikeButtonStatus(PostKey);

               viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                       clickPostIntent.putExtra("PostKey",PostKey);
                       startActivity(clickPostIntent);
                   }
               });

               viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Intent commentsIntent = new Intent(MainActivity.this,CommentsActivity.class);
                       commentsIntent.putExtra("PostKey",PostKey);
                       startActivity(commentsIntent);

                   }
               });



               viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v)
                   {
                       LikeChecker = true;
                       LikesRaf.addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                           {
                            if(LikeChecker.equals(true)){

                                if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                {
                                    LikesRaf.child(PostKey).child(currentUserID).removeValue();
                                    LikeChecker = false;
                                }
                                else
                                {
                                    LikesRaf.child(PostKey).child(currentUserID).setValue(true);
                                    LikeChecker=false;
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
        };
        postList.setAdapter(firebaseRecyclerAdapter);
    }






           public static class PostsViewHolder extends RecyclerView.ViewHolder {

               View mView;

               ImageButton LikePostButton,CommentPostButton;
               TextView DisplayNoOfLikes;
               int countLikes;
               String currentUserId;
               DatabaseReference LikesRaf;

               public PostsViewHolder(@NonNull View itemView) {

                   super(itemView);
                   mView = itemView;

               LikePostButton = mView.findViewById(R.id.like_button);
               CommentPostButton = mView.findViewById(R.id.comment_button);
               DisplayNoOfLikes= mView.findViewById(R.id.display_no_of_likes);

               LikesRaf = FirebaseDatabase.getInstance().getReference().child("Likes");
               currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

               }

               public void setLikeButtonStatus(final String PostKey)
               {
                   LikesRaf.addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                       {
                           if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                               countLikes= (int) dataSnapshot.child(PostKey).getChildrenCount();
                               LikePostButton.setImageResource(R.drawable.like);
                               DisplayNoOfLikes.setText(Integer.toString(countLikes)+(" Likes"));
                           }
                        else{
                               countLikes= (int) dataSnapshot.child(PostKey).getChildrenCount();
                               LikePostButton.setImageResource(R.drawable.dislike);
                               DisplayNoOfLikes.setText(  Integer.toString(countLikes)+(" Likes"));

                           }

                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });
               }




               public void setFullname(String fullname) {
                   TextView username = mView.findViewById(R.id.post_user_name);
                   username.setText(fullname);
               }

               public void setProfileimage(String profileimage) {
                   CircleImageView image = mView.findViewById(R.id.post_profile_image);
                   Picasso.get().load(profileimage).into(image);
               }

               public void setTime(String time) {
                   TextView PostTime = mView.findViewById(R.id.post_time);
                   PostTime.setText("   " + time);
               }

               public void setDate(String date) {
                   TextView PostDate = mView.findViewById(R.id.post_date);
                   PostDate.setText("   " + date);
               }

               public void setDescription(String description) {
                   TextView PostDescription = mView.findViewById(R.id.post_description);
                   PostDescription.setText(description);
               }

               public void setPostimage(String postimage) {
                   ImageView PostImage = mView.findViewById(R.id.post_image);
                   Picasso.get().load(postimage).into(PostImage);
               }
           }




    private void SendUserToPostActivity() {
        Intent addPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addPostIntent);

    }


    @Override
    protected void onStart() {

        super.onStart();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser==null){
            SendUserTologinActivity();
        }
        else {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserTologinActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();

                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "Friends List", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                SendUserToFindfriendsActivity();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();

                break;
            case R.id.nav_logout:
                mAuth.signOut();
                SendUserTologinActivity();
                break;

        }
    }

    private void SendUserToSettingsActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(loginIntent);

    }
    private void SendUserToFindfriendsActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(loginIntent);

    }

    private void SendUserToProfileActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(loginIntent);

    }

}
