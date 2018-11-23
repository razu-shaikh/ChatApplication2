package com.example.rajus.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class StartActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsPagerAdapter myTabsPagerAdapter;
    FirebaseUser currentUser;

    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            String online_user_id = mAuth.getCurrentUser().getUid();
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        }

        myViewPager = (ViewPager)findViewById(R.id.main_tabs_Pager);
        myTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsPagerAdapter);
        myTabLayout =(TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        mToolbar = (Toolbar)findViewById(R.id.start_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("myChat");
    }

    @Override
    protected void onStart() {
        super.onStart();

         currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
        {
            LogoutUser();
        }
        else if (currentUser != null)
        {
          userReference.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null)
        {
            userReference.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void LogoutUser() {

        Intent intent = new Intent(StartActivity.this,StartPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_button)
        {
            if (currentUser != null)
            {
                userReference.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mAuth.signOut();
            LogoutUser();
        }
        if (item.getItemId() == R.id.account_settings_button)
        {
            Intent intent = new Intent(StartActivity.this,SettingActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.all_user_list_id)
        {
            Intent intent = new Intent(StartActivity.this,AllUsersActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
