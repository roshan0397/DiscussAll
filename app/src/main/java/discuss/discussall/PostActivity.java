package discuss.discussall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    public static final String FB_STORAGE_PATH = "Questions";
    private static final int SPEECH_REQUEST = 8;
    private static final int REQUESTCODE = 9;
    private static final int REQUEST_TAKE_PHOTO = 10;
    private ImageView postImage;
    private EditText postDescription;
    private Uri imageuri = null;
    private String mCurrentPhotoPath;
    private String name, image;

    private DatabaseReference QuestionDatabase;
    private StorageReference QuestionStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Toolbar mtoolbar = (Toolbar) findViewById(R.id.add_toolbar);
        setSupportActionBar(mtoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Post Question");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postImage = (ImageView)findViewById(R.id.post_qstn_image);
        final ImageView postCamera = (ImageView) findViewById(R.id.post_camera);
        final ImageView postGallery = (ImageView) findViewById(R.id.post_gallery);
        final ImageView postMic = (ImageView) findViewById(R.id.post_mic);
        postDescription = (EditText) findViewById(R.id.post_description);
        final Button submitPostButton = (Button) findViewById(R.id.post_button);

        postDescription.setCursorVisible(false);

        postDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postDescription.setCursorVisible(true);
            }
        });

        if(!isNetworkCheck()){
            Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
        }

        postCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if (ContextCompat.checkSelfPermission(PostActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(PostActivity.this, new String[]{android.Manifest.permission.CAMERA}, 2);
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

        postGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if (ContextCompat.checkSelfPermission(PostActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(PostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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

        postMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if (ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(PostActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 3);
                    }
                    else {
                        recordAudio();
                    }
                }
                else{
                    recordAudio();
                }
            }
        });

        final Spinner dropdown = (Spinner) findViewById(R.id.dropdown_list);
        String[] exams = new String[]{"Engineering Entrance Exams", "Management Entrance Exams", "Commerce Exams",
                "Medical Entrance Exams", "Interior Designing Exams",
                "Law Entrance Exams", "GATE", "GRE", "Bank Exams", "Civil Services Exams"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(PostActivity.this, R.layout.support_simple_spinner_dropdown_item,exams);
        dropdown.setAdapter(arrayAdapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Names.examName = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(PostActivity.this, "Please Select Category", Toast.LENGTH_LONG).show();
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        QuestionDatabase = FirebaseDatabase.getInstance().getReference().child("Questions").push();
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        userDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                name = dataSnapshot.child("name").getValue().toString();
                image = dataSnapshot.child("dp").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            { }
        });

        final ProgressBar progressBar = findViewById(R.id.postbar);
        progressBar.setVisibility(View.INVISIBLE);

        submitPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(!TextUtils.isEmpty(postDescription.getText()))
                {
                    progressBar.setVisibility(View.VISIBLE);

                    postImage.setEnabled(false);
                    postCamera.setEnabled(false);
                    postGallery.setEnabled(false);
                    postMic.setEnabled(false);
                    postDescription.setEnabled(false);
                    dropdown.setEnabled(false);
                    submitPostButton.setEnabled(false);

                    DatabaseReference PostDb = FirebaseDatabase.getInstance().getReference().child("Posts").child(uid);
                    PostDb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long i = (long) dataSnapshot.getValue();
                            long j = i + 1;
                            dataSnapshot.getRef().setValue(j);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    final String description = postDescription.getText().toString();
                    final String current_date = DateFormat.getDateInstance().format(new Date());

                    if(imageuri != null)
                    {
                        QuestionStorage = FirebaseStorage.getInstance().getReference().child("Questions").child(FB_STORAGE_PATH + System.currentTimeMillis() + "." + getImageExt(imageuri));
                        QuestionStorage.putFile(Uri.fromFile(new File(String.valueOf(imageuri))))
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                    {
                                        String downloadUrl = Objects.requireNonNull(taskSnapshot.getDownloadUrl()).toString();
                                        QuestionDatabase.child("question").setValue(downloadUrl);
                                    }
                                });
                    }

                    Map<String, String> map = new HashMap<>();
                    map.put("username", name);
                    map.put("profileimage", image);
                    map.put("currenttime", current_date);
                    map.put("description", description);
                    map.put("uid", uid);
                    map.put("exam", Names.examName);
                    map.put("postId", QuestionDatabase.getKey());

                    QuestionDatabase.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            progressBar.setVisibility(View.INVISIBLE);
                            if(task.isSuccessful())
                            {
                                startActivity(new Intent(PostActivity.this, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                Toast.makeText(PostActivity.this, "Successfully Posted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else {
                                Snackbar.make(view, "There is some error in posting", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else
                {
                    Snackbar.make(view, "Empty Question Field", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void recordAudio() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something!");

        startActivityForResult(i,SPEECH_REQUEST);
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

            case 3: if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted, You Can Now Use Voice Recording", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
                break;
        }
    }

    public String getImageExt(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUESTCODE && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            try {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String path = cursor.getString(columnIndex);
                imageuri = compressImage(Uri.parse(path));
                cursor.close();
                postImage.setImageURI(imageuri);
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

        else if(requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null)
        {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String desc = postDescription.getText().toString();
            postDescription.setText(desc + " " + result.get(0));
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        imageuri = Uri.fromFile(f);
        mediaScanIntent.setData(imageuri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = postImage.getWidth();
        int targetH = postImage.getHeight();
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
        postImage.setImageBitmap(bitmap);
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

    private boolean isNetworkCheck() {

        ConnectivityManager manager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo info=manager.getActiveNetworkInfo();
        return info!=null && info.isConnected();
    }
}
