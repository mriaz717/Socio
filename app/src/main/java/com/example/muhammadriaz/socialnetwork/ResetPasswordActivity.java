package com.example.muhammadriaz.socialnetwork;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private Button ResetPasswordSendEmailButton;
    private EditText ResetEmailInput;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);





        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBl));

        mToolbar = findViewById(R.id.forget_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        mAuth = FirebaseAuth.getInstance();


        ResetPasswordSendEmailButton = findViewById(R.id.reset_password_email_button);
        ResetEmailInput = findViewById(R.id.reset_password_email);

        ResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String userEmail = ResetEmailInput.getText().toString();
                if(TextUtils.isEmpty(userEmail))
                {
                    Toast.makeText(ResetPasswordActivity.this, "Please Enter your valid Email..", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(ResetPasswordActivity.this, "Please check your email Account", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                                }
                                else{
                                    String message = task.getException().getMessage();
                                    Toast.makeText(ResetPasswordActivity.this, "Error Occurred" +message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

            }
        });
    }
}
