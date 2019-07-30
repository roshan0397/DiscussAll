package discuss.discussall;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AllPostActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabase;
    private ArrayList<Blog> blog_list;
    private AllPostAdapter allPostAdapter;
    private RecyclerView mrecyclerView;
    private Query mQuery;
    private TextView no_post_textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_post);

        Toolbar mtoolbar = (Toolbar) findViewById(R.id.all_posts_toolbar);
        setSupportActionBar(mtoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        no_post_textView = (TextView)findViewById(R.id.no_posts_alert);

        if(!isNetworkCheck()){
            Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
        }

        mrecyclerView = (RecyclerView) findViewById(R.id.questions_list);
        mrecyclerView.setHasFixedSize(true);

        LinearLayoutManager mlinearLayoutManager = new LinearLayoutManager(AllPostActivity.this);
        mlinearLayoutManager.setReverseLayout(true);
        mlinearLayoutManager.setStackFromEnd(true);
        mrecyclerView.setLayoutManager(mlinearLayoutManager);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mDatabase.keepSynced(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.all_posts_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
                // Fetching data from server
                loadPosts();
            }
        });

        Button b1 = (Button) findViewById(R.id.b1);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadPosts();
            }
        });

        b1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Discuss All");
                reportAlert.setMessage("Discuss All is a community of many students, just like you, helping each other. " +
                        "You can post and answer questions and queries according to your education field.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();

                return false;
            }
        });

        Button b2 = (Button) findViewById(R.id.b2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("Engineering Entrance Exams");
                mQuery.keepSynced(true);
                update();
            }
        });

        b2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Engineering Entrances");
                reportAlert.setMessage("Students preparing for exams like JEE Mains, JEE Advance, BITSAT, VITEEE, IPUCET" +
                        " and many more can post and answer competitive questions of Physics, Chemistry and Mathematics.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b3 = (Button) findViewById(R.id.b3);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("Management Entrance Exams");
                mQuery.keepSynced(true);
                update();
            }
        });

        b3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Management Entrances");
                reportAlert.setMessage("Students preparing for exams like CAT, XAT, CMAT, NMAT, IIFT, SNAP, GBO, MAT" +
                        " and many more can post and answer competitive questions of Quantative Aptitude, Logical Reasoning, Data Interpretation, Verbal Ability, etc.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b4 = (Button) findViewById(R.id.b4);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("Commerce Exams");
                mQuery.keepSynced(true);
                update();
            }
        });

        b4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Commerce Entrances");
                reportAlert.setMessage("Students preparing and pursuing CA, CS, CMA, CWA, ICWA" +
                        " and many more can post and answer questions of entrance exams of all levels.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b5 = (Button) findViewById(R.id.b5);
        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("Medical Entrance Exams");
                mQuery.keepSynced(true);
                update();
            }
        });

        b5.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Medical Entrances");
                reportAlert.setMessage("Students preparing for exams like AIPMT, AIIMS, AFMC, NEET, CMC Vellore, JIPMER" +
                        " and many more can post and answer competitive questions of Physics, Chemistry and Biology.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b6 = (Button) findViewById(R.id.b6);
        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("Interior Designing Exams");
                mQuery.keepSynced(true);
                update();
            }
        });

        b6.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Fashion & Designing Entrances");
                reportAlert.setMessage("Students preparing for exams like NIFT, NID, AIEED, CEED, ICD" +
                        " and many more can post and answer questions of General knowledge and Current Affairs, Quantitative Ability, etc. that are generally asked in these exams.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b7 = (Button) findViewById(R.id.b7);
        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("Law Entrance Exams");
                mQuery.keepSynced(true);
                update();
            }
        });

        b7.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Law Entrances");
                reportAlert.setMessage("Students preparing for exams like CLAT, LSAT, AIBE, AILET" +
                        " and many more can post and answer competitive questions of English, Aptitue, Mathematics, General Knowledge and Current Affairs.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b8 = (Button) findViewById(R.id.b8);
        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("GATE");
                mQuery.keepSynced(true);
                update();
            }
        });

        b8.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("GATE");
                reportAlert.setMessage("Students preparing for GATE (any branch)" +
                        " can post and answer competitive questions of their respective branches.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b9 = (Button) findViewById(R.id.b9);
        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("GRE");
                mQuery.keepSynced(true);
                update();
            }
        });

        b9.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("GRE");
                reportAlert.setMessage("Students preparing for GRE" +
                        " can post and answer competitive questions of English, Aptitude and Mathematics.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b10 = (Button) findViewById(R.id.b10);
        b10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("Bank Exams");
                mQuery.keepSynced(true);
                update();
            }
        });

        b10.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Bank Entrances");
                reportAlert.setMessage("Students preparing for exams like Bank PO's" +
                        " can post and answer competitive questions of English, Aptitude, Mathematics, Current Affairs and General Knowledge.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });

        Button b11 = (Button) findViewById(R.id.b11);
        b11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuery = mDatabase.orderByChild("exam").equalTo("Civil Services Exams");
                mQuery.keepSynced(true);
                update();
            }
        });

        b11.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder reportAlert = new AlertDialog.Builder(AllPostActivity.this);
                reportAlert.setTitle("Civil Services Exams");
                reportAlert.setMessage("Students preparing for Civil Services Examinations(CSE)" +
                        " can post and discuss any query or questions of their respective exams.");
                reportAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                    }
                });
                reportAlert.show();
                return false;
            }
        });
    }

    private void loadPosts() {

        mQuery = mDatabase;
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                blog_list = new ArrayList<>();

                if(dataSnapshot.exists()){

                    for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    {
                        Blog blog = postSnapshot.getValue(Blog.class);
                        assert blog != null;
                        blog_list.add(new Blog(blog.getDescription(), blog.getProfileimage(), blog.getUsername(),
                                blog.getQuestion(), blog.getUid(), blog.getCurrenttime(), blog.getExam(), blog.getPostId()));
                    }
                    allPostAdapter = new AllPostAdapter(AllPostActivity.this, blog_list);
                    mrecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mrecyclerView.setAdapter(allPostAdapter);

                    mrecyclerView.setVisibility(View.VISIBLE);
                    no_post_textView.setVisibility(View.INVISIBLE);

                    mSwipeRefreshLayout.setRefreshing(false);
                }else {
                    mrecyclerView.setVisibility(View.INVISIBLE);
                    no_post_textView.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void update() {

        mQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                blog_list = new ArrayList<>();

                if(dataSnapshot.exists()){

                    for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    {
                        Blog blog = postSnapshot.getValue(Blog.class);
                        assert blog != null;
                        blog_list.add(new Blog(blog.getDescription(), blog.getProfileimage(), blog.getUsername(),
                                blog.getQuestion(), blog.getUid(), blog.getCurrenttime(), blog.getExam(), blog.getPostId()));
                    }
                    allPostAdapter = new AllPostAdapter(AllPostActivity.this, blog_list);
                    mrecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mrecyclerView.setAdapter(allPostAdapter);

                    mrecyclerView.setVisibility(View.VISIBLE);
                    no_post_textView.setVisibility(View.GONE);

                    mSwipeRefreshLayout.setRefreshing(false);
                }else {
                    mrecyclerView.setVisibility(View.INVISIBLE);
                    no_post_textView.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRefresh() {
        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.search_btn)
        {
            startActivity(new Intent(AllPostActivity.this, SearchActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkCheck() {

        ConnectivityManager manager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo info=manager.getActiveNetworkInfo();
        return info!=null && info.isConnected();
    }
}
