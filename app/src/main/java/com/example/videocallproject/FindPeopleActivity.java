package com.example.videocallproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.jar.Attributes;

public class FindPeopleActivity extends AppCompatActivity
{
    private RecyclerView findContacts;
    private EditText search;
    private String string="";
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        search = findViewById(R.id.search_contacts);
        findContacts = findViewById(R.id.search_list);
        findContacts.setLayoutManager(new LinearLayoutManager(getApplicationContext())); //menampilkan list kontak


        //menampilkan list kontak dengan nama sesuai yang dicari dan nama depan yang sama
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                if (search.getText().toString().equals(""))
                {
                    Toast.makeText(FindPeopleActivity.this, "Search Contact name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                   string = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = null;

        if (string.equals(""))
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userRef, Contacts.class).build();
        }
        else
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userRef.orderByChild("Name")
                    .startAt(string).endAt(string + "\uf8ff"), Contacts.class).build();
        }

        FirebaseRecyclerAdapter<Contacts, FindContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, FindContactsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FindContactsViewHolder findContactsViewHolder, final int idPosition, @NonNull final Contacts contacts)
            {
                findContactsViewHolder.Username.setText(contacts.getName());
                Picasso.get().load(contacts.getImage()).into(findContactsViewHolder.ProfileImage);



                findContactsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(idPosition).getKey();

                        Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class);
                        intent.putExtra( "visit_user_id", visit_user_id);
                        intent.putExtra( "profile_image", contacts.getImage());
                        intent.putExtra( "profile_name", contacts.getName());
                        intent.putExtra( "profile_status", contacts.getStatus());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
                FindContactsViewHolder viewHolder = new FindContactsViewHolder(view);
                return viewHolder;
            }
        };
        findContacts.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView Username;
        Button Videocall;
        ImageView ProfileImage;
        RelativeLayout CardView;

        public FindContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            Username = itemView.findViewById(R.id.contact_name);
            Videocall = itemView.findViewById(R.id.videocall);
            ProfileImage = itemView.findViewById(R.id.contact_image);
            CardView = itemView.findViewById(R.id.card_view2);

            Videocall.setVisibility(View.GONE); //untuk menghilangkan tombol videoCall pada saat mengklik profile.
        }
    }
}
