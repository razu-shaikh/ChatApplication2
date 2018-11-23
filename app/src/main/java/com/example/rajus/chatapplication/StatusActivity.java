package com.example.rajus.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText change_status_editText;
    private Button change_status_button;
    private DatabaseReference changeStatusRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        changeStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);


        mToolbar = (Toolbar)findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("change status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        change_status_editText = (EditText)findViewById(R.id.change_status_editText);
        change_status_button = (Button)findViewById(R.id.change_status_button);

        loadingBar = new ProgressDialog(this);
        String old_status = getIntent().getExtras().get("user_status").toString();
        change_status_editText.setText(old_status);

        change_status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
               String new_status = change_status_editText.getText().toString();
                changeProfileStatus(new_status);

            }
        });
    }

    private void changeProfileStatus(String new_status) {
        if (TextUtils.isEmpty(new_status))
        {
            Toast.makeText(StatusActivity.this,"Please write your status",Toast.LENGTH_SHORT).show();
        }
        else
        {
          loadingBar.setTitle("Change profile status");
            loadingBar.setMessage("Please wait while we are updating your profile status....");
            loadingBar.show();

            changeStatusRef.child("user_status").setValue(new_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                  if (task.isSuccessful())
                  {
                     loadingBar.dismiss();
                      Intent intent = new Intent(StatusActivity.this,SettingActivity.class);
                      startActivity(intent);

                      Toast.makeText(StatusActivity.this,"Profile Status update successfully",Toast.LENGTH_SHORT).show();
                  }
                  else
                  {
                      Toast.makeText(StatusActivity.this,"Error occurred",Toast.LENGTH_SHORT).show();
                  }
                }
            });
        }
    }
}
