package com.example.muhammadriaz.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
//    private ImageView googleSignInButton;

    private TextInputEditText UserEmail,UserPassword;

    private Button NeedNewAccountLink;

    private TextView ForgetPasswordLink;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private static final  String TAG ="LoginActivity";

//    private static final int RC_SIGN_IN = 1;
//    private GoogleApiClient mGoogleSignInClient;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBl));

        NeedNewAccountLink= findViewById(R.id.register_account_link);
        UserEmail = findViewById(R.id.login_email);
        UserPassword = findViewById(R.id.login_password);
        LoginButton = findViewById(R.id.login_button);
        ForgetPasswordLink = findViewById(R.id.forget_password_link);

        mAuth = FirebaseAuth.getInstance();

        loadingBar= new ProgressDialog(this);
//        googleSignInButton = findViewById(R.id.google_signin_button);


    NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SendUserToRegisterActivity();
        }
    });
    LoginButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SendUserToMainActivity();
            AllowingUserToLogin();
        }
    });

    ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
         startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
        }
    });

        // Configure Google Sign In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//                    }
//                })
//                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
//                .build();
//
//        googleSignInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signIn();
//            }
//        });





    }

//    private void signIn() {
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//
//        if (requestCode == RC_SIGN_IN) {
//
//            loadingBar.setTitle("Google Sign In  ");
//            loadingBar.setMessage("Please wait,While we are allowing you to login using your Google Account...");
//            loadingBar.setCanceledOnTouchOutside(true);
//            loadingBar.show();
//
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//             if(result.isSuccess())
//             {
//                 GoogleSignInAccount account=result.getSignInAccount();
//                 firebaseAuthWithGoogle(account);
//                 Toast.makeText(this, "plz wait......", Toast.LENGTH_SHORT).show();
//             }
//             else {
//                 Toast.makeText(this, "Can't get Auth", Toast.LENGTH_SHORT).show();
//                 loadingBar.dismiss();
//             }
//
//        }
//    }



//
//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful())
//                        {
//
//                            Log.d(TAG, "signInWithCredential:success");
//                            SendUserToMainActivity();
//                            loadingBar.dismiss();
//
//                        }
//                        else
//                            {
//
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            String message = task.getException().toString();
////                            SendUserToLoginActivity();
//                            Toast.makeText(LoginActivity.this, "Not Authenticated.."+message, Toast.LENGTH_SHORT).show();
//                            loadingBar.dismiss();
//                            }
//
//                        // ...
//                    }
//                });
//    }




    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser!=null){
            SendUserToMainActivity();
        }
    }

    private void AllowingUserToLogin() {
        String email=UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please write your Email...", Toast.LENGTH_SHORT).show();
        }
        else  if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Login ");
            loadingBar.setMessage("Please wait,While we are allowing you to into your account...");
            loadingBar.setCanceledOnTouchOutside(true);
         //   loadingBar.show();


            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                System.out.println("debug()enter task.issucceeful");

                                SendUserToMainActivity();
                                System.out.println("debug()main activity called");
                                Toast.makeText(LoginActivity.this, "you are Logged in successfully", Toast.LENGTH_SHORT).show();
                                System.out.println("debug()before dissimissing dialog");
           //                     loadingBar.dismiss();
                                System.out.println("debug()after dissimissing dialog");

                            }
                            else {
                                String message= task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error ocurred"+ message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent= new Intent(LoginActivity.this,MainActivity.class);
       mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent mainIntent= new Intent(LoginActivity.this,LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }
}
