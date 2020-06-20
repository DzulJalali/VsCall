package com.example.videocallproject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueBtn;
    private String checker = "", phoneNumber = "";
    private RelativeLayout relativeLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationid;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;


    @SuppressLint({"CutPasteId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueBtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);


        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (continueBtn.getText().equals("Submit") || checker.equals("Code Sent"))
                {
                    String verificationCode = codeText.getText().toString();
                    if (verificationCode.equals(""))
                    {
                        Toast.makeText(RegistrationActivity.this, "Enter Verification code first.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        loadingBar.setTitle("Verification Code");
                        loadingBar.setMessage("Verifying code, Please Wait..");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationid, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
                else
                {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals(""))
                    {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Verifying Phone Number, Please Wait..");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, RegistrationActivity.this, mCallbacks);

                    }
                    else
                    {
                        Toast.makeText(RegistrationActivity.this, "Enter Valid Number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
                        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
                            {
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e)
                            {
                                Toast.makeText(RegistrationActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();

                                loadingBar.dismiss();
                                relativeLayout.setVisibility(View.VISIBLE);

                                continueBtn.setText("Continue");
                                codeText.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);

                                mVerificationid = s;
                                mResendToken = forceResendingToken;

                                relativeLayout.setVisibility(View.GONE);
                                checker = "Code Sent";
                                continueBtn.setText("Submit");
                                codeText.setVisibility(View.VISIBLE);

                                Toast.makeText(RegistrationActivity.this, "Code has been sent, check your message", Toast.LENGTH_SHORT).show();
                            }
                        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) //jika firebase user tidak sama dengan null maka artinya user telah terdaftar
        {
            Intent menuUtamaIntent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(menuUtamaIntent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Congratulation, You are Logged in Successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                        else
                            {
                                loadingBar.dismiss();
                                String e = task.getException().toString();
                                Toast.makeText(RegistrationActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                };


    private void sendUserToMainActivity()
    {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
