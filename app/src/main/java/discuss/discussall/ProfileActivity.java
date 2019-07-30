package discuss.discussall;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser Currentuser;
    private TextView username;
    private CircleImageView dp;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar mtoolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mtoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final TextView t1 = (TextView) findViewById(R.id.Zero_posts);
        final LinearLayout m = (LinearLayout)findViewById(R.id.ll2);
        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this,MyPostsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        final LinearLayout n = (LinearLayout)findViewById(R.id.ll1);
        n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        final TextView t2 = (TextView) findViewById(R.id.zero_favorites);

        username = (TextView)findViewById(R.id.profile_username);
        dp = (CircleImageView)findViewById(R.id.profile_image);

        if(!isNetworkCheck()){
            Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
        }

        final ProgressBar progressBar = findViewById(R.id.profile_bar);

        mAuth = FirebaseAuth.getInstance();
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        final DatabaseReference userFeedback = FirebaseDatabase.getInstance().getReference().child("FeedBack").child(uid);
        mDatabase.keepSynced(true);

        DatabaseReference PostsDb = FirebaseDatabase.getInstance().getReference().child("Posts").child(uid);
        DatabaseReference FavsDb = FirebaseDatabase.getInstance().getReference().child("Favs").child(uid);
        PostsDb.keepSynced(true);
        FavsDb.keepSynced(true);

        PostsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                t1.setText(Objects.requireNonNull(dataSnapshot.getValue()).toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FavsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                t2.setText(Objects.requireNonNull(dataSnapshot.getValue()).toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                username.setText(Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());

                final Picasso picasso = Picasso.with(ProfileActivity.this);
                picasso.setIndicatorsEnabled(false);
                picasso.load(Objects.requireNonNull(dataSnapshot.child("dp").getValue()).toString()).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.dp).into(dp, new Callback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onError() {
                        picasso.load(Objects.requireNonNull(dataSnapshot.child("dp").getValue()).toString()).placeholder(R.drawable.dp).into(dp);
                    }
                });
                Picasso.with(ProfileActivity.this).load(Objects.requireNonNull(dataSnapshot.child("dp").getValue()).toString()).placeholder(R.drawable.dp).into(dp);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Button logout_button = (Button) findViewById(R.id.logout_btn);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
                finish();
            }
        });


        Button editProfileButton = (Button) findViewById(R.id.edit_profile_btn);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this,EditProfileActivity.class));
            }
        });

        TextView shareApp = (TextView) findViewById(R.id.share_app);
        shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT,"DiscussAll");
                i.putExtra(Intent.EXTRA_TEXT,"https://play.google.com/store/apps/details?id=discuss.discussall");
                startActivity(Intent.createChooser(i,"Share via"));
            }
        });

        TextView feedBack = (TextView)findViewById(R.id.feedback);
        feedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RatingDialog ratingDialog = new RatingDialog.Builder(ProfileActivity.this)
                        .threshold(3)
                        .title("How was your experience with DiscussAll?")
                        .titleTextColor(R.color.black)
                        .formHint("Suggest us what went wrong and we'll work on it!")
                        .formSubmitText("Submit")
                        .formCancelText("Cancel")
                        .ratingBarColor(R.color.materialYellow)
                        .playstoreUrl("https://play.google.com/store/apps/details?id=discuss.discussall")
                        .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                            @Override
                            public void onFormSubmitted(String feedback) {
                                userFeedback.push().setValue(feedback);
                                Toast.makeText(ProfileActivity.this, "Your feedback successfully submitted", Toast.LENGTH_SHORT).show();
                            }
                        }).build();

                ratingDialog.show();
            }
        });

        TextView del_account = (TextView)findViewById(R.id.delete_account);
        del_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (UserInfo user: Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getProviderData()) {
                    if (user.getProviderId().equals("google.com")) {

                        AlertDialog.Builder deleteAccountAlert = new AlertDialog.Builder(ProfileActivity.this);
                        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
                        final TextView delAccountButton = (TextView) mView.findViewById(R.id.del_acc_btn);

                        deleteAccountAlert.setView(mView);
                        final AlertDialog dialog = deleteAccountAlert.create();
                        dialog.show();

                        delAccountButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mProgress = new ProgressDialog(ProfileActivity.this);
                                mProgress.setTitle("Deleting Account");
                                mProgress.setMessage("Please wait while we delete your account");
                                mProgress.setCanceledOnTouchOutside(false);
                                mProgress.show();

                                Currentuser = FirebaseAuth.getInstance().getCurrentUser();

                                assert Currentuser != null;
                                Currentuser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(ProfileActivity.this, "Account Successfully Deleted", Toast.LENGTH_SHORT).show();
                                            mProgress.dismiss();
                                            dialog.dismiss();
                                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(ProfileActivity.this, "Error in deleting account", Toast.LENGTH_SHORT).show();
                                            mProgress.dismiss();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                    else if (user.getProviderId().equals("password")) {

                        AlertDialog.Builder deleteAccountAlert = new AlertDialog.Builder(ProfileActivity.this);
                        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
                        final TextView delAccountButton = (TextView) mView.findViewById(R.id.del_acc_btn);
                        final EditText delAccountPassword = (EditText)mView.findViewById(R.id.delete_account_password);
                        delAccountPassword.setVisibility(View.VISIBLE);

                        deleteAccountAlert.setView(mView);
                        final AlertDialog dialog = deleteAccountAlert.create();
                        dialog.show();

                        delAccountButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {
                                if(TextUtils.isEmpty(delAccountPassword.getText().toString()))
                                {
                                    Snackbar.make(view, "Please Enter Password", Snackbar.LENGTH_LONG).show();
                                }
                                else {
                                    mProgress = new ProgressDialog(ProfileActivity.this);
                                    mProgress.setTitle("Deleting Account");
                                    mProgress.setMessage("Please wait while we delete your account");
                                    mProgress.setCanceledOnTouchOutside(false);
                                    mProgress.show();

                                    Currentuser = FirebaseAuth.getInstance().getCurrentUser();
                                    assert Currentuser != null;
                                    AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(Currentuser.getEmail()), delAccountPassword.getText().toString());

                                    Currentuser.reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Currentuser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    Toast.makeText(ProfileActivity.this, "Account Successfully Deleted", Toast.LENGTH_SHORT).show();
                                                                    mProgress.dismiss();
                                                                    dialog.dismiss();
                                                                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                                    finish();
                                                                }
                                                                else {
                                                                    Toast.makeText(ProfileActivity.this, "Error in deleting account", Toast.LENGTH_SHORT).show();
                                                                    mProgress.dismiss();
                                                                    dialog.dismiss();
                                                                }
                                                            }
                                                        });
                                                    }else {
                                                        Snackbar.make(view, "Please Enter Correct Password", Snackbar.LENGTH_LONG).show();
                                                        mProgress.dismiss();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                    }
                }
            }
        });

        TextView TnC = (TextView)findViewById(R.id.terms_and_conditions);
        TnC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://sites.google.com/s/17c7Djrp1wtvkaN8D-yfN-rB4OoqQ9LJ1/p/16yV9RODCpyMQAaoaXPCgY3ArTa8LrGIJ/edit");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        TextView chngePass = (TextView)findViewById(R.id.change_password);
        chngePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                for (UserInfo user: Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getProviderData()) {
                    if (user.getProviderId().equals("google.com")) {

                        Snackbar.make(view, "You Are Logged In Via Google. Please Visit Google Account To Change Your Password.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                    else if (user.getProviderId().equals("password")) {

                        AlertDialog.Builder changePasswordAlert = new AlertDialog.Builder(ProfileActivity.this);
                        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
                        final EditText oldPassword = (EditText)mView.findViewById(R.id.old_pass);
                        final EditText newPassword = (EditText)mView.findViewById(R.id.new_pass);
                        TextView changePassword = (TextView) mView.findViewById(R.id.change_pass_btn);

                        changePasswordAlert.setView(mView);
                        final AlertDialog dialog = changePasswordAlert.create();
                        dialog.show();

                        changePassword.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {

                                if(TextUtils.isEmpty(oldPassword.getText().toString()) || TextUtils.isEmpty(newPassword.getText().toString()))
                                {
                                    Snackbar.make(view, "Please Fill Empty Fields", Snackbar.LENGTH_LONG).show();
                                }
                                else {

                                    mProgress = new ProgressDialog(ProfileActivity.this);
                                    mProgress.setTitle("Re-authenticating");
                                    mProgress.setMessage("Please wait while we change password");
                                    mProgress.setCanceledOnTouchOutside(false);
                                    mProgress.show();

                                    Currentuser = FirebaseAuth.getInstance().getCurrentUser();
                                    assert Currentuser != null;
                                    AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(Currentuser.getEmail()), oldPassword.getText().toString());

                                    Currentuser.reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Currentuser.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    Toast.makeText(ProfileActivity.this, "Password Successfully Changed", Toast.LENGTH_SHORT).show();
                                                                    mProgress.dismiss();
                                                                    dialog.dismiss();
                                                                }else {
                                                                    Toast.makeText(ProfileActivity.this, "Error(Maybe Weak Password)!", Toast.LENGTH_SHORT).show();
                                                                    mProgress.dismiss();
                                                                    dialog.dismiss();
                                                                }
                                                            }
                                                        });
                                                    }else {
                                                        Snackbar.make(view, "Please Enter Correct Current Password", Snackbar.LENGTH_LONG).show();
                                                        mProgress.dismiss();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private boolean isNetworkCheck() {

        ConnectivityManager manager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo info=manager.getActiveNetworkInfo();
        return info!=null && info.isConnected();
    }
}
