package discuss.discussall;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView mrecyclerView;
    private ArrayList<Blog> blog_list;
    private AllPostAdapter allPostAdapter;
    private SearchView searchView;
    private TextView textAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        textAlert = (TextView)findViewById(R.id.no_search_alert);

        searchView = (SearchView)findViewById(R.id.search_box);
        searchView.setQueryHint("Search");
        searchView.setIconified(false);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");

        mrecyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        mrecyclerView.setHasFixedSize(true);

        LinearLayoutManager mlinearLayoutManager = new LinearLayoutManager(SearchActivity.this);
        mlinearLayoutManager.setReverseLayout(true);
        mlinearLayoutManager.setStackFromEnd(true);
        mrecyclerView.setLayoutManager(mlinearLayoutManager);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filter(newText);
                return true;
            }
        });

        //Loading posts
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                blog_list = new ArrayList<>();

                if(dataSnapshot.exists()){

                    for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    {
                        Blog blog = postSnapshot.getValue(Blog.class);
                        assert blog != null;
                        blog_list.add(new Blog(blog.getDescription(), blog.getProfileimage(), blog.getUsername(), blog.getQuestion(), blog.getUid(), blog.getCurrenttime(), blog.getExam(), blog.getPostId()));
                    }
                    allPostAdapter = new AllPostAdapter(SearchActivity.this, blog_list);
                    mrecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mrecyclerView.setAdapter(allPostAdapter);
                }else {
                    searchView.setVisibility(View.GONE);
                    textAlert.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        mrecyclerView.setVisibility(View.GONE);
    }

    void filter(String text){
        ArrayList<Blog> temp = new ArrayList();
        for(Blog d: blog_list){
            if(d.getDescription().toLowerCase().contains(text)){
                temp.add(d);
            }
        }

        allPostAdapter.updateList(temp);
        mrecyclerView.setVisibility(View.VISIBLE);
    }
}
