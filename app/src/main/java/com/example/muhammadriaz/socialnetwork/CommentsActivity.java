package com.example.muhammadriaz.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;

    private String Post_key,current_user_id;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,PostsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBl));

        Post_key = getIntent().getExtras().get("PostKey").toString();





        mAuth= FirebaseAuth.getInstance();
        current_user_id =mAuth.getCurrentUser().getUid();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_key).child("Comments");

        CommentsList = findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);


        CommentInputText = findViewById(R.id.comment_input);
        PostCommentButton= findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    { if(dataSnapshot.exists())
                    {
                        String userName =dataSnapshot.child("username").getValue().toString();

                        validateComment(userName);

                        CommentInputText.setText(" ");
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
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                (
                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        PostsRef
                )
        {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position)
            {
                 viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
            }
        };
        CommentsList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public CommentsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;

        }

        public void setUsername(String username)
        {
            TextView myUserName = mView.findViewById(R.id.comment_username);
            myUserName.setText("@"+username+" ");
        }
        public void setComment(String comment)
        {
            TextView myComment = mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }
        public void setDate(String date)
        {
            TextView myDate = mView.findViewById(R.id.comment_date);
            myDate.setText("  Date: "+date);
        }
        public void setTime(String time)
        {
            TextView myTime = mView.findViewById(R.id.comment_time);
            myTime.setText("  Time:   "+time);
        }
    }



    private void validateComment(String userName)
    {
        String commentText= CommentInputText.getText().toString();
        
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please Write text to comment", Toast.LENGTH_SHORT).show();
        }
        else
            {
                Calendar calFordate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
               final String saveCurrentDate = currentDate.format(calFordate.getTime());


                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currenTime = new SimpleDateFormat("HH:mm");
               final String saveCurrentTime = currenTime.format(calFordate.getTime());

               final  String RandomKey = current_user_id + saveCurrentDate+ saveCurrentTime;

                HashMap commentsMap = new HashMap();
                commentsMap.put("uid",current_user_id);
                commentsMap.put("comment",commentText);
                commentsMap.put("date",saveCurrentDate);
                commentsMap.put("time",saveCurrentTime);
                commentsMap.put("username",userName);

                PostsRef.child(RandomKey).updateChildren(commentsMap)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(CommentsActivity.this, "you have commented successfully", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(CommentsActivity.this, "Error Occured,try again", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
    }
}
