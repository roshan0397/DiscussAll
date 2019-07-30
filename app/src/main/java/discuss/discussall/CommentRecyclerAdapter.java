package discuss.discussall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Comments> comments_list;

    CommentRecyclerAdapter(Context context, ArrayList<Comments> comments_list)
    {
        this.context = context;
        this.comments_list = comments_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        try {
            if(comments_list.size() > 0) {

                final Comments c = comments_list.get(position);

                //****************Setting up Text Fields**********************************
                holder.name.setText(c.getName());
                holder.date.setText(c.getDate());
                holder.comment.setText(c.getComment());

                //****************Setting up comment answer image**********************************

                final Picasso picasso = Picasso.with(context);
                picasso.setIndicatorsEnabled(false);
                picasso.load(c.getAnswer()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.answer, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        picasso.load(c.getAnswer()).into(holder.answer);
                    }
                });

                //****************Setting up profile image**********************************

                picasso.load(c.getImage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        picasso.load(c.getImage()).placeholder(R.drawable.dp).into(holder.image);
                    }
                });

                //****************Zooming Image**********************************

                Zoomy.Builder builder = new Zoomy.Builder((Activity) context).target(holder.answer);
                builder.register();

                //****************Deleting Comment******************************

                holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View view) {

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser current_user = mAuth.getCurrentUser();
                        assert current_user != null;
                        final String uid = current_user.getUid();

                        final String commentId = comments_list.get(position).getCommentId();
                        final String postId = comments_list.get(position).getPostId();
                        final String UID = comments_list.get(position).getUid();
                        final DatabaseReference commentDb = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);

                        if(UID.equals(uid))
                        {
                            AlertDialog.Builder reportAlert = new AlertDialog.Builder(context);
                            reportAlert.setTitle("Delete Answer");
                            reportAlert.setMessage("Are you sure you want to delete answer?");
                            reportAlert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    commentDb.child(commentId).removeValue();

                                    comments_list.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, comments_list.size());
                                    Snackbar.make(view, "Answer Deleted Successfully", Snackbar.LENGTH_LONG).show();
                                }
                            });
                            reportAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            reportAlert.show();
                        }
                        else {
                            Snackbar.make(view, "Someone else has posted this answer!", Snackbar.LENGTH_LONG).show();
                        }

                        return false;
                    }

                });
            }
        }catch (Exception e)
        {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return comments_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView comment;
        public TextView date;
        public CircleImageView image;
        public ImageView answer;
        public CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);

            name = (TextView)itemView.findViewById(R.id.comment_name);
            comment = (TextView)itemView.findViewById(R.id.final_comment);
            date = (TextView)itemView.findViewById(R.id.comment_date);
            image = (CircleImageView)itemView.findViewById(R.id.comment_img);
            answer = (ImageView)itemView.findViewById(R.id.comment_answerImg);
            cardView = (CardView)itemView.findViewById(R.id.comment_cardView);
        }
    }
}
