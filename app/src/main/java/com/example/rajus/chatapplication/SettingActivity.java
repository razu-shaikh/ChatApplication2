package com.example.rajus.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

    private CircleImageView set_profile_image;
    private TextView setUser_name;
    private TextView setUser_status;
    private Button set_change_profile_image_button;
    private Button set_change_satatus_Button;

    private DatabaseReference getUserDataReference;
    private FirebaseAuth mAuth;

    private final static int galleryPic = 1;
    private StorageReference storeProfileDatastorageReference;
    Bitmap thumb_bitmap = null;
    private StorageReference thumbImageRef;
    private ProgressDialog loadingBar;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);

        storeProfileDatastorageReference = FirebaseStorage.getInstance().getReference().child("Profile_image");
        thumbImageRef = FirebaseStorage.getInstance().getReference().child("thumb_images");

        set_profile_image = (CircleImageView) findViewById(R.id.setting_profile_image);
        setUser_name = (TextView) findViewById(R.id.setting_userName);
        setUser_status = (TextView) findViewById(R.id.setting_status);
        set_change_profile_image_button = (Button) findViewById(R.id.setting_changeImage);
        set_change_satatus_Button = (Button) findViewById(R.id.setting_changeStatus);

        loadingBar = new ProgressDialog(this);

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String thum_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                setUser_name.setText(name);
                setUser_status.setText(status);

                if(!image.equals("default_profile"))
                {

                    Picasso.with(SettingActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                            .into(set_profile_image, new Callback() {
                                @Override
                                public void onSuccess()
                                {

                                }

                                @Override
                                public void onError()
                                {
                                    Picasso.with(SettingActivity.this).load(image).placeholder(R.drawable.default_profile).into(set_profile_image);
                                }
                            });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        set_change_profile_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });

        set_change_satatus_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
               Intent intent = new Intent(SettingActivity.this,StatusActivity.class);
                String old_status = setUser_status.getText().toString();
                intent.putExtra("user_status",old_status);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPic && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Updating profile image");
                loadingBar.setMessage("Please wait while updating your profile image.....");
                loadingBar.show();
                Uri resultUri = result.getUri();

                File thumb_filePath_url = new File(resultUri.getPath());

                String user_id = mAuth.getCurrentUser().getUid();

                try
                {
                   thumb_bitmap = new Compressor(this)
                           .setMaxWidth(200)
                           .setMaxHeight(200)
                           .setQuality(50)
                           .compressToBitmap(thumb_filePath_url);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();



                StorageReference filePath = storeProfileDatastorageReference.child(user_id + ".jpg");

                final StorageReference thumb_filePath = thumbImageRef.child(user_id + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this, "Image saving in Firebase successfully", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                           UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                                {
                                  String thumb_downloadUri = thumb_task.getResult().getDownloadUrl().toString();
                                  if (task.isSuccessful())
                                  {
                                      Map update_user_data = new HashMap();
                                      update_user_data.put("user_image", downloadUrl);
                                      update_user_data.put("user_thumb_image", thumb_downloadUri);

                                      getUserDataReference.updateChildren(update_user_data)
                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<Void> task) {
                                                      Toast.makeText(SettingActivity.this, "profile image updated successfully", Toast.LENGTH_SHORT).show();

                                                     loadingBar.dismiss();
                                                  }
                                              });
                                  }
                                }
                            });


                        } else {
                            Toast.makeText(SettingActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
