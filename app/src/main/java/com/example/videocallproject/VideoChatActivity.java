package com.example.videocallproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener
{

    private static String API_key = "46729872";
    private static String SESSION_ID ="2_MX40NjcyOTg3Mn5-MTU5MjI4MTI5MDA3MX5KNWJxNUxsbjZ6VkdncjM2QktKVUV6VE1-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjcyOTg3MiZzaWc9NDBlNjRmMGFiZThkM2I1MDIxNTgxNTNjYmI0YTgwZTJhOWY2NjJmNjpzZXNzaW9uX2lkPTJfTVg0ME5qY3lPVGczTW41LU1UVTVNakk0TVRJNU1EQTNNWDVLTldKeE5VeHNialo2VmtkbmNqTTJRa3RLVlVWNlZFMS1mZyZjcmVhdGVfdGltZT0xNTkyMjgxMzMxJm5vbmNlPTAuMTA1NjIwODMxMDYwNjM0OCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTk0ODczMzI5JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERMISSION = 124;

    private FrameLayout mPublisherViewControl;
    private FrameLayout mSubscriberViewControl;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private ImageView closeVideoCall;
    private DatabaseReference usersRef;
    private String userID = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);


        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        closeVideoCall = findViewById(R.id.close_video_chat);
        closeVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child(userID).hasChild("Ringing"))
                        {
                            usersRef.child(userID).child("Ringing").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }

                        if (dataSnapshot.child(userID).hasChild("Calling"))
                        {
                            usersRef.child(userID).child("Calling").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }
                        else
                        {
                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }
        });


        requestPermissions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERMISSION)
    private void requestPermissions()
    {
        String[] permissions = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if (EasyPermissions.hasPermissions(this, permissions))
        {
            mPublisherViewControl = findViewById(R.id.publisher_container);
            mSubscriberViewControl = findViewById(R.id.subscriber_container);


            // Menginisiallisasi dan menghubungkan kedalam SESSION

            mSession = new com.opentok.android.Session.Builder(this, API_key, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);

        }
        else //jika user tidak memberi permission
        {
            EasyPermissions.requestPermissions(this, "You need to Allow Camera and Audio Permission to run this App", RC_VIDEO_APP_PERMISSION);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream)
    {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream)
    {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError)
    {

    }

    //mempublish stream kedalam session
    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG, "Session is Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewControl.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session)
    {
        Log.i(LOG_TAG, "Stream Disconnected");
    }

    //mengsubscribe dalam stream
    //maksudnya adalah untuk bergabung kedalam video Call
    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Received");

        if (mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewControl.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null)
        {
            mSubscriber = null;
            mSubscriberViewControl.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        Log.i(LOG_TAG, "Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture)
    {

    }
}
