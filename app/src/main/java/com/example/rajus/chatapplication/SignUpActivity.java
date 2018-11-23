package com.example.rajus.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignUpActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DatabaseReference storeUserDefaultDataReference;
    private ProgressDialog loadingBar;

    private EditText signUpUserName;
    private EditText signUpUserEmail;
    private EditText signUpUserPassword;
    private Button createAccountButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar)findViewById(R.id.sign_up_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        signUpUserName = (EditText)findViewById(R.id.signUp_name);
        signUpUserEmail = (EditText)findViewById(R.id.signUp_email);
        signUpUserPassword = (EditText)findViewById(R.id.signUp_password);
        createAccountButton = (Button)findViewById(R.id.create_account_button);
        loadingBar = new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               final String name = signUpUserName.getText().toString();
                String email = signUpUserEmail.getText().toString();
                String password = signUpUserPassword.getText().toString();

                SignUpAccount(name,email,password);
            }
        });
    }

    private void SignUpAccount(final String name, String email, String password) {

        if (TextUtils.isEmpty(name))
        {
            Toast.makeText(SignUpActivity.this,"Please enter your name",Toast.LENGTH_LONG).show();
        }

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(SignUpActivity.this,"Please enter your email",Toast.LENGTH_LONG).show();
        }

        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(SignUpActivity.this,"Please enter your password",Toast.LENGTH_LONG).show();
        }

        else
        {
            loadingBar.setTitle("Creating new  Account");
            loadingBar.setMessage("Please wait,while creating your account");
            loadingBar.show();
          mAuth.createUserWithEmailAndPassword(email,password)
                  .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                            String current_User_Id = mAuth.getCurrentUser().getUid();
                            storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_User_Id);

                            storeUserDefaultDataReference.child("user_name").setValue(name);
                            storeUserDefaultDataReference.child("user_status").setValue("Hey, I am using ChatApp, Developed by Md razu Shaikh");
                            storeUserDefaultDataReference.child("user_image").setValue("default_profile");
                            storeUserDefaultDataReference.child("device_token").setValue(DeviceToken);
                            storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image")
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task)
                                         {
                                             if (task.isSuccessful())
                                             {
                                                 Intent mainIntent = new Intent(SignUpActivity.this,StartActivity.class);
                                                 mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                 startActivity(mainIntent);
                                                 finish();

                                             }
                                         }
                                     });
                        }
                        else
                        {
                           Toast.makeText(SignUpActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                        }
                          loadingBar.dismiss();
                      }
                  });
        }
    }
}
