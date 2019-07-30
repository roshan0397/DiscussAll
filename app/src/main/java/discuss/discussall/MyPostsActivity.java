package discuss.discussall;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MyPostsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private Query mQuery;
    private ArrayList<Blog> blog_list;
    private PostAdapter postAdapter;
    private TextView noMyPostsAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        noMyPostsAlert = (TextView)findViewById(R.id.no_myposts_alert);

        Toolbar mtoolbar = (Toolbar) findViewById(R.id.my_posts_toolbar);
        setSupportActionBar(mtoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("My Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.my_posts_swipe);
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

        mRecyclerView = (RecyclerView) findViewById(R.id.my_posts);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MyPostsActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String current_user = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mQuery = mDatabase.orderByChild("uid").equalTo(current_user);
        mQuery.keepSynced(true);
    }

    private void loadPosts() {

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
                    postAdapter = new PostAdapter(MyPostsActivity.this, blog_list);
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mRecyclerView.setAdapter(postAdapter);

                    mRecyclerView.setVisibility(View.VISIBLE);
                    noMyPostsAlert.setVisibility(View.GONE);

                    mSwipeRefreshLayout.setRefreshing(false);
                }else {
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    noMyPostsAlert.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onRefresh() {
        loadPosts();
    }
}
