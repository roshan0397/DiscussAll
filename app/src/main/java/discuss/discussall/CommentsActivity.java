package discuss.discussall;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CommentsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    public static final String FB_STORAGE_PATH = "Comments";
    private static final int REQUESTCODE = 10;
    private static final int REQUEST_TAKE_PHOTO = 20;
    private String mCurrentPhotoPath;
    private Uri imageuri = null;
    private ImageView imageView;
    private String downloadUrl = "default";
    private EditText commment;
    private StorageReference mStorage;
    private DatabaseReference commentDatabase;
    private String name, imagee;
    private ProgressDialog mprogress;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private CommentRecyclerAdapter commentRecyclerAdapter;
    private ArrayList<Comments> comments_list;
    private TextView noCommentsAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //************************Getting post Id Through Intent*****************************
        final String blog_post_id = getIntent().getStringExtra("post_id");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //******************************Swipe Refresh Layout******************************
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.comments_swipe);
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
                loadComments();
            }
        });


        //******************Setting Up Toolbar*******************************
        Toolbar mToolbar = (Toolbar)findViewById(R.id.comments_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Answers");


        //******************Setting up references****************************
        noCommentsAlert = (TextView)findViewById(R.id.no_comments_alert);
        commment = (EditText)findViewById(R.id.mention_cmnt);
        imageView = (ImageView)findViewById(R.id.imageView);


        //******************Setting up recyclerView**************************
        recyclerView = (RecyclerView) findViewById(R.id.comments_list);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager mlinearLayoutManager = new LinearLayoutManager(CommentsActivity.this);
        mlinearLayoutManager.setReverseLayout(true);
        mlinearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mlinearLayoutManager);


        //*****************Retrieving name and dp from Database***********************
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String current_user = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                imagee = Objects.requireNonNull(dataSnapshot.child("dp").getValue()).toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //*************************Setting references for Comments Database*********************************
        commentDatabase = FirebaseDatabase.getInstance().getReference().child("Comments").child(blog_post_id);
        final DatabaseReference db = commentDatabase.push();

        //**************************On Post Button Click*************************
        ImageView insertComment = (ImageView) findViewById(R.id.insrt_cmnt);
        insertComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final String comment_message = commment.getText().toString();
                final String current_date = DateFormat.getDateInstance().format(new Date());

                if(!comment_message.isEmpty())
                {
                    final HashMap<String, String> commentsMap = new HashMap<>();
                    commentsMap.put("comment", comment_message);
                    commentsMap.put("date",current_date);
                    commentsMap.put("name",name);
                    commentsMap.put("image",imagee);
                    commentsMap.put("answer",downloadUrl);
                    commentsMap.put("commentId", db.getKey());
                    commentsMap.put("postId", blog_post_id);
                    commentsMap.put("uid", current_user);

                    db.setValue(commentsMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        commment.setText("");
                                        downloadUrl = "default";
                                        imageView.setVisibility(View.INVISIBLE);
                                        Snackbar.make(view, "Answer Added", Snackbar.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Snackbar.make(view, "Error: Answer Not Added", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else
                {
                    Snackbar.make(view, "Oops!!! Empty Field", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    //***************************Loading Comments***********************************
    private void loadComments() {

        commentDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                comments_list = new ArrayList<>();

                if(dataSnapshot.exists()){

                    for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    {
                        Comments comments = postSnapshot.getValue(Comments.class);
                        assert comments != null;
                        comments_list.add(new Comments(comments.getName(), comments.getComment(), comments.getDate(), comments.getImage(), comments.getAnswer(),
                                comments.getCommentId(), comments.getPostId(), comments.getUid()));
                    }
                    commentRecyclerAdapter = new CommentRecyclerAdapter(CommentsActivity.this, comments_list);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(commentRecyclerAdapter);

                    recyclerView.setVisibility(View.VISIBLE);
                    noCommentsAlert.setVisibility(View.GONE);

                    mSwipeRefreshLayout.setRefreshing(false);
                }else {
                    recyclerView.setVisibility(View.INVISIBLE);
                    noCommentsAlert.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    //******************************Menu***********************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_menu, menu);

        return true;
    }

    //****************************ADD ICON ON TOP***************************
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.add_answer_icon)
        {
            AlertDialog.Builder addAlert = new AlertDialog.Builder(CommentsActivity.this);
            addAlert.setTitle("Add Image");
            addAlert.setMessage("Choose Image From:");

            addAlert.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        if (ContextCompat.checkSelfPermission(CommentsActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(CommentsActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                        else {
                            galleryIntent();
                        }
                    }
                    else{
                        galleryIntent();
                    }
                }
            });

            addAlert.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        if (ContextCompat.checkSelfPermission(CommentsActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(CommentsActivity.this, new String[]{Manifest.permission.CAMERA}, 2);
                        }
                        else{
                            dispatchTakePictureIntent();
                        }
                    }
                    else {
                        dispatchTakePictureIntent();
                    }
                }
            });
            addAlert.show();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case 1: if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted, You Can Now Access Gallery", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
                break;

            case 2: if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted, You Can Now Access Camera", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
                break;
        }
    }

    private void galleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUESTCODE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try
            {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error In Creating File.", Toast.LENGTH_SHORT).show();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageuri = FileProvider.getUriForFile(this,
                        "discuss.discussall",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, imageuri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUESTCODE && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            try {
                mprogress = new ProgressDialog(CommentsActivity.this);
                mprogress.setTitle("Uploading Image...");
                mprogress.setMessage("Please wait while we upload");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String path = cursor.getString(columnIndex);
                imageuri = compressImage(Uri.parse(path));
                cursor.close();
                imageView.setImageURI(imageuri);
                imageView.setVisibility(View.VISIBLE);

                mStorage = FirebaseStorage.getInstance().getReference().child("Comments").child(FB_STORAGE_PATH + System.currentTimeMillis() + "." + getImageExt(imageuri));
                mStorage.putFile(Uri.fromFile(new File(String.valueOf(imageuri))))
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                downloadUrl = taskSnapshot.getDownloadUrl().toString();
                                mprogress.dismiss();
                            }
                        });
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Oops! something went wrong"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            galleryAddPic();
            setPic();
        }
    }

    /****************************************Image Compression and stuff********************************************/
    public String getImageExt(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        imageuri = Uri.fromFile(f);
        mediaScanIntent.setData(imageuri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {

        mprogress = new ProgressDialog(CommentsActivity.this);
        mprogress.setTitle("Uploading Image...");
        mprogress.setMessage("Please wait while we upload");
        mprogress.setCanceledOnTouchOutside(false);
        mprogress.show();

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        imageuri=compressImage(imageuri);
        imageView.setImageBitmap(bitmap);

        mStorage = FirebaseStorage.getInstance().getReference().child("Comments").child(FB_STORAGE_PATH + System.currentTimeMillis() + "." + getImageExt(imageuri));
        mStorage.putFile(Uri.fromFile(new File(String.valueOf(imageuri))))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        mprogress.dismiss();
                    }
                });
        // Toast.makeText(this, ""+imageuri, Toast.LENGTH_LONG).show();
    }

    public Uri compressImage(Uri imageUri) {

        Uri filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(String.valueOf(filePath), options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 612.0f;
        float maxWidth = 816.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            }
            else
            {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(String.valueOf(filePath), options);
            // ivImage.setImageBitmap(bmp);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(String.valueOf(filePath));

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);
//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Uri.parse(filename);
    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "DiscussAll/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private Uri getRealPathFromURI(Uri contentURI) {
        Uri contentUri = Uri.parse(String.valueOf(contentURI));
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return Uri.parse(contentUri.getPath());
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return Uri.parse(cursor.getString(index));
        }
    }


    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }


    //****************On Refresh....load comments**************************
    @Override
    public void onRefresh() {
        loadComments();
    }
}
