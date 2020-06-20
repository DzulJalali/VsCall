package com.example.videocallproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    BottomNavigationView navView;
    RecyclerView ContactsList;
    ImageView findPeople;

    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String onlineUserID;
    private String userName="", profileImage="", calledBy="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        onlineUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        findPeople = findViewById(R.id.find_people);
        ContactsList = findViewById(R.id.contact_list);
        ContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext())); //menampilkan contact pada MainActivity


        findPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent findPeopleIntent = new Intent(MainActivity.this, FindPeopleActivity.class);
                startActivity(findPeopleIntent);

            }
        }); //tutup kurung find people

    }


    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item)
                {
                    switch (item.getItemId())
                    {
                        case R.id.navigation_home:
                            Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            break;

                        case R.id.navigation_settings:
                            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            break;

                        case R.id.navigation_notifications:
                            Intent notificationsIntent = new Intent(MainActivity.this, NotificationsActivity.class);
                            startActivity(notificationsIntent);
                            break;

                        case R.id.navigation_logout:
                            FirebaseAuth.getInstance().signOut();
                            Intent logoutIntent = new Intent(MainActivity.this, RegistrationActivity.class);
                            startActivity(logoutIntent);
                            finish();
                            break;
                    }

                    return true;
                }
            };

    @Override
    protected void onStart()
    {
        super.onStart();

        receivingCallCheck();

        validateUser();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(onlineUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts)
            {
                final String listUserId = getRef(i).getKey();


                //jika kontak exist (ada), maka akan ditampilkan
                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            userName = dataSnapshot.child("Name").getValue().toString();
                            profileImage = dataSnapshot.child("image").getValue().toString();

                            contactsViewHolder.Username.setText(userName);
                            Picasso.get().load(profileImage).into(contactsViewHolder.profileImage);
                        }


                        contactsViewHolder.VideoCallBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent callingIntent = new Intent(MainActivity.this, CallingActivity.class);
                                callingIntent.putExtra("visit_user_id", listUserId);
                                startActivity(callingIntent);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        ContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }



    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView Username;
        Button VideoCallBtn;
        ImageView profileImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            Username = itemView.findViewById(R.id.contact_name);
            VideoCallBtn = itemView.findViewById(R.id.videocall);
            profileImage = itemView.findViewById(R.id.contact_image);
        }
    }

    private void validateUser()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        //jika  User id (Online user) exist / ada (mengisis profile) maka user bisa menggunakan aplikasinya
        //jika tidak maka user akan dibawa ke profile settings untuk mengisi profile terlebih dahulu

        reference.child("Users").child(onlineUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.exists())
                {
                    //jika user baru saja membuat akun maka akan langsung di bawa ke settingActivity untuk
                    //melakukan pengisian profil, jika tidak maka tidak akan bisa mengakses MainActivity


                    Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void receivingCallCheck()
    {
        userRef.child(onlineUserID)
                .child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("ringing"))
                {
                    calledBy = dataSnapshot.child("ringing").getValue().toString();

                    Intent callingIntent = new Intent(MainActivity.this, CallingActivity.class);
                    callingIntent.putExtra("visit_user_id", calledBy);
                    startActivity(callingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

}
