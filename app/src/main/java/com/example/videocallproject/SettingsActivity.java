package com.example.videocallproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity
{
    //MEMANGGIL VARIABLE
    private Button saveBtn;
    private EditText UserName, Status;
    private ImageView fotoProfil;


    private static int GalleryPictures = 1;
    private Uri UriImage;
    private StorageReference UserProfileStorageReference;
    private String downloadUrl;
    private DatabaseReference UserReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        UserProfileStorageReference = FirebaseStorage.getInstance().getReference().child("Profile Images"); //ruang penyimpanan file
        UserReference = FirebaseDatabase.getInstance().getReference().child("Users"); //sebagai realtime database


        saveBtn = findViewById(R.id.save_settings);  //memanggil button save
        UserName = findViewById(R.id.settings_username); //memanggil username
        Status = findViewById(R.id.settings_status); //memanggil status
        fotoProfil = findViewById(R.id.settings_image_profile); //memanggil profil
        progressDialog = new ProgressDialog(this);


        fotoProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT); //perintah mengakses gallery android.
                galleryIntent.setType("image/*"); // format yang bisa diupload
                startActivityForResult(galleryIntent, GalleryPictures); //mulai aktivitas


            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                saveUserData();
            }
        });
        mengambilInfoUser();
    }

    private void saveUserData()
    {
        final String getUsername = UserName.getText().toString(); //menyimpan username yang dimasukan pada profile
        final String getStatus = Status.getText().toString(); //menyimpan status

        if (UriImage == null)
        {
            UserReference.addValueEventListener(new ValueEventListener() //Untuk mengecek profile image telah di set atau tidak
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")) //jika profile image telah ada
                    {
                        UserInfoTanpaImage();
                    }
                    else
                    {
                        Toast.makeText(SettingsActivity.this, "Select Your Image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if (getUsername.equals("")) //jika username kosong
        {
            Toast.makeText(this, "Please Input your Username", Toast.LENGTH_SHORT).show();
        }
        else if (getStatus.equals("")) //jika status kosong
        {
            Toast.makeText(this, "Please Input your Status", Toast.LENGTH_SHORT).show();
        }
        else //Untuk menyimpan settingan profile
        {
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Updating account settings please wait...");
            progressDialog.show();
            final StorageReference filePath = UserProfileStorageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            final UploadTask uploadTask = filePath.putFile(UriImage);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if (task.isSuccessful())
                    {

                    }

                    downloadUrl = filePath.getDownloadUrl().toString(); //Image url yang akan dipilih dan diambil melalui getDownloadUrl
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful());
                    {
                        downloadUrl = task.getResult().toString(); //jika url Image url berhasil didownload maka tampil hasil beruba string.


                        if (getUsername.equals("")) //jika username kosong
                        {
                            Toast.makeText(SettingsActivity.this, "Please Input your Username", Toast.LENGTH_SHORT).show();
                        }
                        else if (getStatus.equals("")) //jika status kosong
                        {
                            Toast.makeText(SettingsActivity.this, "Please Input your Status", Toast.LENGTH_SHORT).show();
                        }
                        else //Untuk menyimpan settingan profile
                        {


                            HashMap<String, Object> profileMap = new HashMap<>();
                            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            profileMap.put("Name", getUsername);
                            profileMap.put("Status", getStatus);
                            profileMap.put("image", downloadUrl);

                            UserReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        progressDialog.dismiss();

                                        Toast.makeText(SettingsActivity.this, "Profile has been updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    }
                }
            });

        }
    }

    private void UserInfoTanpaImage() {
        final String getUsername = UserName.getText().toString();
        final String getStatus = Status.getText().toString();



        if (getUsername.equals("")) //jika username kosong
        {
            Toast.makeText(SettingsActivity.this, "Please Input your Username", Toast.LENGTH_SHORT).show();
        } else if (getStatus.equals("")) //jika status kosong
        {
            Toast.makeText(SettingsActivity.this, "Please Input your Status", Toast.LENGTH_SHORT).show();
        } else //Untuk menyimpan settingan profile
        {
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Updating account settings please wait...");
            progressDialog.show();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("Name", getUsername);
            profileMap.put("Status", getStatus);

            UserReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();

                        Toast.makeText(SettingsActivity.this, "Profile has been updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPictures && resultCode==RESULT_OK && data!=null)
        {
            UriImage = data.getData(); //memasukan file kedalam uri
            fotoProfil.setImageURI(UriImage); //menampilkan file
        }
    }

    private void mengambilInfoUser()
    {
        UserReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String imageDb = dataSnapshot.child("image").getValue().toString();
                    String NameDb = dataSnapshot.child("Name").getValue().toString();
                    String StatusDb = dataSnapshot.child("Status").getValue().toString();

                    UserName.setText(NameDb);
                    Status.setText(StatusDb);
                    Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(fotoProfil);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}