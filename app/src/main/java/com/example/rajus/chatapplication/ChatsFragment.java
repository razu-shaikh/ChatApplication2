package com.example.rajus.chatapplication;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView myChatLists;

    private DatabaseReference friendsReference;
    private DatabaseReference usersReference;
    private FirebaseAuth mAuth;
    String online_user_id;

    private View myMainView;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myMainView = inflater.inflate(R.layout.fragment_chats, container, false);
        // Inflate the layout for this fragment

        myChatLists = (RecyclerView) myMainView.findViewById(R.id.chats_list);

        mAuth = FirebaseAuth.getInstance();

        online_user_id = mAuth.getCurrentUser().getUid();

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");



        myChatLists.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myChatLists.setLayoutManager(linearLayoutManager);
        return myMainView;
    }



    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder>
                (
                        Chats.class,
                        R.layout.all_user_display_layout,
                        ChatsFragment.ChatsViewHolder.class,
                        friendsReference
                )
        {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder viewHolder, Chats model, int position)
            {

                final String list_user_id = getRef(position).getKey();
                usersReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot)
                    {

                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String userImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                        String userStatus = dataSnapshot.child("user_status").getValue().toString();

                        if (dataSnapshot.hasChild("online"))
                        {
                            String  online_status = (String)dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_status);

                        }

                        viewHolder.setUsername(userName);
                        viewHolder.setThumbImage(userImage, getContext());
                        viewHolder.setUserStatus(userStatus);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                if (dataSnapshot.child("online").exists())
                                {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",list_user_id);
                                    chatIntent.putExtra("user_name",userName);
                                    startActivity(chatIntent);

                                }
                                else
                                {
                                    usersReference.child(list_user_id).child("online")
                                            .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>()
                                    {
                                        @Override
                                        public void onSuccess(Void aVoid)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };
        myChatLists.setAdapter(firebaseRecyclerAdapter);
    }



    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String userName)
        {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.Users_userName);
            userNameDisplay.setText(userName);
        }

        public void setThumbImage( final String userImage, final Context ctx)
        {
            final CircleImageView thumb_image = (CircleImageView)mView.findViewById(R.id.user_profile_image);
            Picasso.with(ctx).load(userImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess()
                        {

                        }

                        @Override
                        public void onError()
                        {
                            Picasso.with(ctx).load(userImage).placeholder(R.drawable.default_profile).into(thumb_image);
                        }
                    });
        }

        public void setUserOnline(String online_status)
        {

            ImageView onlineStatusView = (ImageView)mView.findViewById(R.id.online_status);

            if (online_status.equals("true"))
            {
                onlineStatusView.setVisibility(View.VISIBLE);
            }
            else
            {
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserStatus(String userStatus)
        {
            TextView user_status = (TextView)mView.findViewById(R.id.Users_status);
            user_status.setText(userStatus);

        }
    }
}
