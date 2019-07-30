package discuss.discussall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private EditText e1,e2;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        e1 = (EditText)findViewById(R.id.login_email);
        e2 = (EditText)findViewById(R.id.login_password);
        Button b1 = (Button) findViewById(R.id.login_btn);
        Button b2 = (Button) findViewById(R.id.login_to_register_btn);
        mProgress = new ProgressDialog(LoginActivity.this);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if(!TextUtils.isEmpty(e1.getText().toString()) && !TextUtils.isEmpty(e2.getText().toString()))
                {
                    mProgress.setTitle("Logging In...");
                    mProgress.setMessage("Please wait while we login...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    mAuth.signInWithEmailAndPassword(e1.getText().toString(),e2.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        mProgress.dismiss();
                                        startActivity(new Intent(LoginActivity.this,MainActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                    }
                                    else {
                                        mProgress.dismiss();
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        }
                                        catch(FirebaseAuthInvalidUserException e) {
                                            Snackbar.make(view, "User Not Found, Create New Account!", Snackbar.LENGTH_SHORT).show();
                                        } catch(FirebaseAuthInvalidCredentialsException e) {
                                            Snackbar.make(view, "Invalid Password!", Snackbar.LENGTH_SHORT).show();
                                        } catch(FirebaseNetworkException e) {
                                            Snackbar.make(view, "Please Check Your Internet Connection!", Snackbar.LENGTH_SHORT).show();
                                        } catch(Exception e) {
                                            Snackbar.make(view, "Error: "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }
                else{
                    Snackbar.make(view, "Please Enter Credentials", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });


        SignInButton mGoogleBtn = (SignInButton) findViewById(R.id.googleBtn);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.setTitle("Checking Your Google Account...");
                mProgress.setMessage("Please wait while we login...\nIt may take upto 30 seconds");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                assert account != null;
                firebaseAuthWithGoogle(account);
            }else {
                mProgress.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                            mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(uid)){
                                        startActivity(new Intent(LoginActivity.this,MainActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                    }else if(!dataSnapshot.hasChild(uid)){

                                        mDatabase.child("Posts").child(uid).setValue(0);
                                        mDatabase.child("Favs").child(uid).setValue(0);

                                        HashMap<String,String> userMap = new HashMap<>();
                                        userMap.put("name","Username");
                                        userMap.put("dp","default");
                                        mDatabase.child("Users").child(uid).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    startActivity(new Intent(LoginActivity.this,EditProfileActivity.class)
                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                }else {
                                                    Toast.makeText(LoginActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                        mProgress.dismiss();
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            mProgress.dismiss();
                            Toast.makeText(LoginActivity.this, "There is some problem!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}
