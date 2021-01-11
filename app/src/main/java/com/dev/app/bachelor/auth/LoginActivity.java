package com.dev.app.bachelor.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.dev.app.bachelor.MainActivity;
import com.dev.app.bachelor.R;
import com.dev.app.bachelor.SupportActivity;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    //assigning variables
    private Button SignInButton;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private FirebaseUser CurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        SignInButton = (Button) findViewById(R.id.login_button_google);

        // on click buttons
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetEmailAndPasswordToSignIn();
            }
        });
    }

    // login page menus
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //login page menu on click menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.login_menu_help:
                SendUserToSupportActivity();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    //get user password from pop up dialog for google account email
    private void GetEmailAndPasswordToSignIn() {
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.custom_dialog);

        TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
        final EditText DialogInputEmail = (EditText) dialog.findViewById(R.id.dialog_custom_input);
        final EditText DialogInputPassword = (EditText) dialog.findViewById(R.id.dialog_custom_input_2);
        Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
        TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

        DialogTitle.setText("Login");
        DialogInputEmail.setHint("Email");
        DialogInputPassword.setHint("Password");
        DialogInputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        DialogInputEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mail, 0, 0, 0);
        DialogInputPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0);
        DialogInputEmail.setCompoundDrawablePadding(10);
        DialogInputPassword.setCompoundDrawablePadding(10);
        DialogOk.setText("Login");
        DialogError.setText("Register");
        DialogInputEmail.setVisibility(View.VISIBLE);
        DialogInputPassword.setVisibility(View.VISIBLE);
        DialogError.setVisibility(View.VISIBLE);

        DialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = DialogInputEmail.getText().toString();
                String password = DialogInputPassword.getText().toString();
                if (email.isEmpty()) {
                    DialogInputEmail.setError("Enter email");
                } else if (password.isEmpty()) {
                    DialogInputPassword.setError("Enter password");
                } else {
                    dialog.dismiss();
                    LoginWithEmailAndPassword(email, password);
                }
            }
        });

        DialogError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = DialogInputEmail.getText().toString();
                String password = DialogInputPassword.getText().toString();
                if (email.isEmpty()) {
                    DialogInputEmail.setError("Enter email");
                } else if (password.isEmpty()) {
                    DialogInputPassword.setError("Enter password");
                } else {
                    dialog.dismiss();
                    RegisterWithEmailAndPassword(email, password);
                }
            }
        });

        dialog.show();
    }

    //try to login with google email and dialog password
    private void LoginWithEmailAndPassword(String email, String password) {
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setMessage("Logging...");
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    SendUserToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //if account not found by email password continue to register
    private void RegisterWithEmailAndPassword(String email, String password) {
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setMessage("Registering...");
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    CurrentUser = mAuth.getCurrentUser();
                    Map UserDataMap = new HashMap();
                    UserDataMap.put("id", CurrentUser.getUid());
                    UserDataMap.put("email", CurrentUser.getEmail());

                    loadingBar.setMessage("Updating data...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    RootRef.child("Users").child(CurrentUser.getUid()).updateChildren(UserDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                            } else {
                                Toast.makeText(LoginActivity.this, "Profile data not updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //sending user to main page if login done
    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(this, MainActivity.class);
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
    }

    //sending support page if need any help
    private void SendUserToSupportActivity() {
        Intent SupportIntent = new Intent(this, SupportActivity.class);
        startActivity(SupportIntent);
    }

}