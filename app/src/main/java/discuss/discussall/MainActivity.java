package discuss.discussall;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView v1 = (CardView) findViewById(R.id.main_questions);
        CardView v2 = (CardView) findViewById(R.id.main_add);
        CardView v3 = (CardView) findViewById(R.id.main_my_posts);
        CardView v4 = (CardView) findViewById(R.id.main_fav);
        CardView v6 = (CardView) findViewById(R.id.main_profile);

        v1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AllPostActivity.class));
            }
        });

        v2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PostActivity.class));
            }
        });

        v3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MyPostsActivity.class));
            }
        });

        v4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
            }
        });

        v6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        if(!isNetworkCheck()){

            final Dialog[] busyDialog = new Dialog[1];
            busyDialog[0] = new Dialog(MainActivity.this, R.style.lightbox_dialog);
            busyDialog[0].setContentView(R.layout.lightbox_dialog);
            ((TextView) busyDialog[0].findViewById(R.id.mydialog)).setText("No Internet Connection Found. Wall Cannot Be Updated.");
            ImageView closedialog= (ImageView) busyDialog[0].findViewById(R.id.close);

            closedialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (busyDialog[0] != null)
                        busyDialog[0].dismiss();

                    busyDialog[0] = null;
                }
            });
            busyDialog[0].show();
        }
    }

    private boolean isNetworkCheck() {

        ConnectivityManager manager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo info=manager.getActiveNetworkInfo();
        return info!=null && info.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser current_user = mAuth.getCurrentUser();
        if(current_user == null)
        {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
    }
}
