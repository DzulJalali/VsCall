package com.example.videocallproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity
{
    private String receiverUserID="", receiverUserImage="", receiverUserName="", receiverUserStatus="";
    private ImageView background_Profile_picture;
    private TextView profile_name, profile_status;
    private Button add_friend, decline_friend;
    private FirebaseAuth mAuth;
    private String SenderID;
    private String CurrentStatus = "new";
    private DatabaseReference friendRequestRef, contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        SenderID = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        //Menampilkan Informasi profile dari kontak

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        receiverUserImage = getIntent().getExtras().get("profile_image").toString();
        receiverUserName = getIntent().getExtras().get("profile_name").toString();
        receiverUserStatus = getIntent().getExtras().get("profile_status").toString();


        background_Profile_picture = findViewById(R.id.background_Profile_picture);
        profile_name = findViewById(R.id.profile_name);
        profile_status = findViewById(R.id.profile_status);
        add_friend = findViewById(R.id.add_friend);
        decline_friend = findViewById(R.id.decline_friend);


        Picasso.get().load(receiverUserImage).into(background_Profile_picture);
        profile_name.setText(receiverUserName);
        profile_status.setText(receiverUserStatus);



        manageClickEvents();
    }

    private void manageClickEvents()
    {
        friendRequestRef.child(SenderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(receiverUserID))
                {
                    String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (requestType.equals("sent"))
                    {
                        CurrentStatus = "request_sent";
                        add_friend.setText("Cancel Friend Request");
                    }
                    else if (requestType.equals("received"))
                    {
                        CurrentStatus = "request_received";
                        add_friend.setText("Accept Friend Request");

                        decline_friend.setVisibility(View.VISIBLE);
                        decline_friend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                CancelFriendRequest();
                            }
                        });
                    }
                }
                //jika sender tidak memuat ID penerima (tidak ada/exist),maka akan dicek apakah pertemanan sudah diterima atau belum
                else
                {
                    contactsRef.child(SenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.hasChild(receiverUserID))
                            {
                                CurrentStatus = "friends";
                                add_friend.setText("Delete Contact");
                            }
                            else
                            {
                                CurrentStatus = "new";
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (SenderID.equals(receiverUserID))
        {
            add_friend.setVisibility(View.GONE);
        }
        else
        {
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (CurrentStatus.equals("new"))
                    {
                        SendFriendRequest();
                    }
                    if (CurrentStatus.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if (CurrentStatus.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if (CurrentStatus.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                }
            });
        }
    }

    private void AcceptFriendRequest()
    {
        contactsRef.child(SenderID).child(receiverUserID)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    contactsRef.child(receiverUserID).child(SenderID)
                            .child("Contact").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                friendRequestRef.child(SenderID).child(receiverUserID)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    friendRequestRef.child(receiverUserID).child(SenderID)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        CurrentStatus = "friends";
                                                                        add_friend.setText("Delete Contact");

                                                                        decline_friend.setVisibility(View.GONE);
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
        });
    }

    private void CancelFriendRequest()
    {
        friendRequestRef.child(SenderID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    friendRequestRef.child(receiverUserID).child(SenderID)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                CurrentStatus = "new";
                                add_friend.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendFriendRequest()
    {
        friendRequestRef.child(SenderID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    friendRequestRef.child(receiverUserID).child(SenderID)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                CurrentStatus = "request_sent";
                                add_friend.setText("Cancel Friend Request");
                                Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
