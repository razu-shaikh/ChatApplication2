package com.example.rajus.chatapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Button decline_friend_request_button;
    private Button  send_friend_request_button;
    private TextView user_visit_name;
    private TextView user_visit_status;
    private ImageView user_profile_visit_image;
    private DatabaseReference UsersRefference;
    private String current_state;
    private DatabaseReference FriendRequestRef;
    private FirebaseAuth mAuth;
    String sender_user_id;
    String receiver_user_id;
    private DatabaseReference FriendsRef;
    private DatabaseReference NotificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
       FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRef.keepSynced(true);

        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationRef.keepSynced(true);


        UsersRefference = FirebaseDatabase.getInstance().getReference().child("Users");
        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();

        decline_friend_request_button = (Button)findViewById(R.id.decline_friend_request);
        send_friend_request_button = (Button)findViewById(R.id.send_friend_request);
        user_visit_name = (TextView)findViewById(R.id.user_visit_name);
        user_visit_status = (TextView)findViewById(R.id.user_visit_status);
        user_profile_visit_image = (ImageView)findViewById(R.id.user_profile_visit_image);

        current_state = "not_friends";

        UsersRefference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                user_visit_name.setText(name);
                user_visit_status.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_profile).into(user_profile_visit_image);

                FriendRequestRef.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot)
                            {
                                 if (dataSnapshot.hasChild(receiver_user_id))
                                 {
                                     String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();
                                     if (req_type.equals("sent"))
                                     {
                                         current_state ="request_sent";
                                         send_friend_request_button.setText("Cancel Request");

                                         decline_friend_request_button.setVisibility(View.INVISIBLE);
                                         decline_friend_request_button.setEnabled(false);
                                     }
                                     else if (req_type.equals("received"))
                                     {
                                         current_state = "request_received";
                                         send_friend_request_button.setText("Accept Request");

                                         decline_friend_request_button.setVisibility(View.VISIBLE);
                                         decline_friend_request_button.setEnabled(true);

                                         decline_friend_request_button.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View view) {
                                                 DeclineFriendRequest();
                                             }
                                         });
                                     }
                                 }


                             else
                             {
                                FriendsRef.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild(receiver_user_id))
                                        {
                                            current_state = "friends";
                                            send_friend_request_button.setText("unfriend");

                                            decline_friend_request_button.setVisibility(View.INVISIBLE);
                                            decline_friend_request_button.setEnabled(false);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError)
                                    {

                                    }
                                });

                             }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        decline_friend_request_button.setVisibility(View.INVISIBLE);
        decline_friend_request_button.setEnabled(false);

       if (!sender_user_id.equals(receiver_user_id))
       {
           send_friend_request_button.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view)
               {
                   send_friend_request_button.setEnabled(false);
                   if (current_state.equals("not_friends"))
                   {
                       sendFriendRequestToAperson();
                   }
                   if(current_state.equals("request_sent"))
                   {
                       cancelFriendRequest();
                   }
                   if (current_state.equals("request_received"))
                   {
                       AcceptFriendsRequest();
                   }
                   if(current_state.equals("friends"))
                   {
                       unfriendaFriend();
                   }


               }
           });
       }
        else
       {
          decline_friend_request_button.setVisibility(View.INVISIBLE);
           send_friend_request_button.setVisibility(View.INVISIBLE);
       }
    }

    private void DeclineFriendRequest()
    {
        FriendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                send_friend_request_button.setEnabled(true);
                                                current_state = "not_friends";
                                                send_friend_request_button.setText("Send Friend Request");

                                                decline_friend_request_button.setVisibility(View.INVISIBLE);
                                                decline_friend_request_button.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void unfriendaFriend()
    {
       FriendsRef.child(sender_user_id).child(receiver_user_id).removeValue()
               .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task)
                   {
                       if (task.isSuccessful())
                       {
                          FriendsRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            send_friend_request_button.setEnabled(true);
                                            current_state = "not_friends";
                                            send_friend_request_button.setText("Send Friend Request");

                                            decline_friend_request_button.setVisibility(View.INVISIBLE);
                                            decline_friend_request_button.setEnabled(false);
                                        }

                                    }
                                });
                       }

                   }
               });
    }

    private void AcceptFriendsRequest()
    {
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final  String saveCurrentdate = currentDate.format(calDate.getTime());

        FriendsRef.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                      FriendsRef.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentdate)
                              .addOnSuccessListener(new OnSuccessListener<Void>() {
                                  @Override
                                  public void onSuccess(Void aVoid)
                                  {
                                      FriendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<Void> task)
                                                  {
                                                      if (task.isSuccessful()) {
                                                          FriendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                      @Override
                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                          if (task.isSuccessful()) {
                                                                              send_friend_request_button.setEnabled(true);
                                                                              current_state = "friends";
                                                                              send_friend_request_button.setText("unfriend");

                                                                              decline_friend_request_button.setVisibility(View.INVISIBLE);
                                                                              decline_friend_request_button.setEnabled(false);
                                                                          }
                                                                      }
                                                                  });
                                                      }
                                                  }
                                              });

                                  }
                              });
                    }
                });
    }

    private void cancelFriendRequest()
    {
       FriendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
               .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task)
                   {
                       if (task.isSuccessful())
                       {
                           FriendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task)
                                       {
                                         if (task.isSuccessful())
                                         {
                                             send_friend_request_button.setEnabled(true);
                                             current_state = "not_friends";
                                             send_friend_request_button.setText("Send Friend Request");

                                             decline_friend_request_button.setVisibility(View.INVISIBLE);
                                             decline_friend_request_button.setEnabled(false);
                                         }
                                       }
                                   });
                       }
                   }
               });
    }

    private void sendFriendRequestToAperson()
    {
        FriendRequestRef.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                     {
                     if (task.isSuccessful())
                     {
                         FriendRequestRef.child(receiver_user_id).child(sender_user_id)
                                 .child("request_type").setValue("received")
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task)
                                     {
                                       if (task.isSuccessful())
                                       {

                                           HashMap<String, String> notificationData = new HashMap<String, String>();
                                           notificationData.put("from", sender_user_id);
                                           notificationData.put("type", "request");

                                           NotificationRef.child(receiver_user_id).push().setValue(notificationData)
                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task)
                                                       {
                                                           if (task.isSuccessful())
                                                           {
                                                               send_friend_request_button.setEnabled(true);
                                                               current_state = "request_sent";
                                                               send_friend_request_button.setText("Cancel Request");

                                                               decline_friend_request_button.setVisibility(View.INVISIBLE);
                                                               decline_friend_request_button.setEnabled(false);
                                                           }

                                                       }
                                                   });
                                       }
                                     }
                                 });
                     }
                    }
                });
    }
}
