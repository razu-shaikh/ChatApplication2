package com.example.rajus.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

     private Toolbar mToolbar;

    private RecyclerView all_users_list;
    private DatabaseReference allDatabaseUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolbar = (Toolbar)findViewById(R.id.all_user_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        all_users_list = (RecyclerView)findViewById(R.id.all_user_list);

        all_users_list.setHasFixedSize(true);
        all_users_list.setLayoutManager(new LinearLayoutManager(this));

        allDatabaseUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        allDatabaseUserReference.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<AllUsers,AllUsersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>
                (
                    AllUsers.class,
                    R.layout.all_user_display_layout,
                    AllUsersViewHolder.class,
                    allDatabaseUserReference
                )
        {
            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position) {
               viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext(),model.getUser_thumb_image());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id = getRef(position).getKey();
                        Intent intent = new Intent(AllUsersActivity.this,ProfileActivity.class);
                        intent.putExtra("visit_user_id",visit_user_id);
                        startActivity(intent);
                    }
                });
            }
        };
        all_users_list.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public AllUsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setUser_name(String user_name)
        {
            TextView name = (TextView) mView.findViewById(R.id.Users_userName);
            name.setText(user_name);
        }
        public void setUser_status(String user_status)
        {
            TextView status = (TextView) mView.findViewById(R.id.Users_status);
            status.setText(user_status);
        }
        public void setUser_thumb_image(final Context ctx,final String user_thumb_image)
        {
            final CircleImageView thumb_image = (CircleImageView)mView.findViewById(R.id.user_profile_image);
            Picasso.with(ctx).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess()
                        {

                        }

                        @Override
                        public void onError()
                        {
                            Picasso.with(ctx).load(user_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);
                        }
                    });
        }

    }

}
