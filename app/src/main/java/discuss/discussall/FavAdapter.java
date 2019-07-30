package discuss.discussall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ablanco.zoomy.Zoomy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Blog> blog_list;

    FavAdapter(Context context, ArrayList<Blog> blog_list)
    {
        this.context = context;
        this.blog_list = blog_list;
    }

    @Override
    public FavAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        try {
            if (blog_list.size() > 0){

                final Blog b = blog_list.get(position);

                holder.username.setText(b.getUsername());
                holder.description.setText(b.getDescription());
                holder.date.setText(b.getCurrenttime());

                final Picasso picasso = Picasso.with(context);
                picasso.setIndicatorsEnabled(false);
                picasso.load(b.getQuestion()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.post_question_img, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        picasso.load(b.getExam()).into(holder.post_question_img);
                    }
                });

                picasso.load(b.getProfileimage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(holder.dp, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        picasso.load(b.getProfileimage()).placeholder(R.drawable.dp).into(holder.dp);
                    }
                });

                Zoomy.Builder builder = new Zoomy.Builder((Activity) context).target(holder.post_question_img);
                builder.register();

                holder.post_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_SUBJECT,"DiscussAll");
                        i.putExtra(Intent.EXTRA_TEXT, b.getDescription());
                        context.startActivity(Intent.createChooser(i,"Share via"));
                    }
                });

                holder.post_comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String postId = blog_list.get(position).getPostId();
                        context.startActivity(new Intent(context,CommentsActivity.class)
                                .putExtra("post_id",postId));
                    }
                });

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
                final DatabaseReference FavsDb = FirebaseDatabase.getInstance().getReference().child("Favs").child(uid);

                holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View view) {

                        final String postId = blog_list.get(position).getPostId();

                        AlertDialog.Builder reportAlert = new AlertDialog.Builder(context);
                        reportAlert.setTitle("Remove From Favorites?");
                        reportAlert.setMessage("Are you sure you want to remove this post from favorites?");
                        reportAlert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int i) {

                                mDatabase.child(postId).child(uid).removeValue();

                                FavsDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long i = (long) dataSnapshot.getValue();
                                        long j = i - 1;
                                        dataSnapshot.getRef().setValue(j);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                blog_list.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, blog_list.size());

                                Snackbar.make(view, "Removed From Favorites", Snackbar.LENGTH_LONG).show();
                            }
                        });
                        reportAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        reportAlert.show();

                        return false;
                    }
                });

                holder.post_google.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Uri uri = Uri.parse("https://www.google.com/search?q=" + b.getDescription());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    }
                });

                holder.post_option.setVisibility(View.GONE);
            }
        }catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView description;
        public TextView username;
        public TextView date;
        public CircleImageView dp;
        public ImageView post_question_img;
        public ImageView post_comment, post_share, post_google, post_option;
        public CardView cardView;


        ViewHolder(View itemView) {
            super(itemView);

            description = (TextView)itemView.findViewById(R.id.postedDescription);
            username = (TextView)itemView.findViewById(R.id.nameOfAvtar);
            date = (TextView)itemView.findViewById(R.id.dateOfPost);
            post_question_img = (ImageView) itemView.findViewById(R.id.postedImage);
            dp = (CircleImageView)itemView.findViewById(R.id.avtar);
            post_comment = (ImageView)itemView.findViewById(R.id.cmnt_btn);
            post_share = (ImageView)itemView.findViewById(R.id.shre_btn);
            post_google = (ImageView)itemView.findViewById(R.id.google_btn);
            post_option = (ImageView)itemView.findViewById(R.id.postedOptions);
            cardView = (CardView)itemView.findViewById(R.id.cardView);
        }
    }
}
