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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private EditText e1,e2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        e1 = (EditText) findViewById(R.id.register_email);
        e2 = (EditText) findViewById(R.id.register_password);
        Button b1 = (Button) findViewById(R.id.register_btn);
        Button b2 = (Button) findViewById(R.id.register_to_login_btn);
        mProgress = new ProgressDialog(RegisterActivity.this);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(!TextUtils.isEmpty(e1.getText().toString()) && !TextUtils.isEmpty(e2.getText().toString()))
                {
                    mProgress.setTitle("Registering...");
                    mProgress.setMessage("Please wait while we register...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    mAuth.createUserWithEmailAndPassword(e1.getText().toString(),e2.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                                        mDatabase.child("Posts").child(uid).setValue(0);
                                        mDatabase.child("Favs").child(uid).setValue(0);

                                        HashMap<String,String> userMap = new HashMap<>();
                                        userMap.put("name","Username");
                                        userMap.put("dp","default");
                                        mDatabase.child("Users").child(uid).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    startActivity(new Intent(RegisterActivity.this,EditProfileActivity.class)
                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                }else {
                                                    Toast.makeText(RegisterActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        finish();
                                    }
                                    else{
                                        mProgress.dismiss();
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        }
                                        catch(FirebaseAuthWeakPasswordException e) {
                                            Snackbar.make(view, "Weak Password!", Snackbar.LENGTH_SHORT).show();
                                        } catch(FirebaseAuthInvalidCredentialsException e) {
                                            Snackbar.make(view, "Invalid E-mail or Password", Snackbar.LENGTH_SHORT).show();
                                        } catch(FirebaseAuthUserCollisionException e) {
                                            Snackbar.make(view, "User With This E-mail Already Exists!", Snackbar.LENGTH_SHORT).show();
                                        } catch(FirebaseNetworkException e) {
                                            Snackbar.make(view, "Please Check Your Internet Connection!", Snackbar.LENGTH_SHORT).show();
                                        } catch(Exception e) {
                                            Snackbar.make(view, "Error: "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }
                else {
                    Snackbar.make(view, "Please Enter Credentials", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
    }

}
