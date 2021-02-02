package pt.srmeme.app.MainActivities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pt.srmeme.app.Categories.CategoriesPublish;
import pt.srmeme.app.R;

public class Publish extends AppCompatActivity {
    String imageUri;
    String description;
    private StorageReference storageRef;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static ArrayList<String> memeCategories = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        Log.d("MEME_CATS", "onCreate: " + memeCategories);

        enablePublishButton();

        //Hide navigation buttons
        /*View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);*/

        Intent publishIntent = getIntent();
        Bundle extras = publishIntent.getExtras();
        if (extras != null) {
            ImageView publishImg = findViewById(R.id.publishImg);
            EditText descriptionET = findViewById(R.id.description);
            imageUri = extras.getString("imageUri");
            description = extras.getString("description");
            Log.d("TAG", "onCreate: " + imageUri);
            publishImg.setImageURI(Uri.parse(imageUri));
            descriptionET.setText(description);
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();
    }

    public void enablePublishButton() {
        Button btnPublish = findViewById(R.id.btnPublish);
        TextView txtInfo = findViewById(R.id.txtInfo);
        if (memeCategories.equals(new ArrayList<String>())) {
            btnPublish.setEnabled(false);
            txtInfo.setVisibility(View.VISIBLE);
        } else {
            btnPublish.setEnabled(true);
            txtInfo.setVisibility(View.INVISIBLE);
        }
    }

    public void goBack(View v) {
        finish();
    }

    public void Publish(View v) {

        storageRef = FirebaseStorage.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();

        EditText descriptionET = findViewById(R.id.description);
        description = String.valueOf(descriptionET.getText());

        Map<String, Object> meme = new HashMap<>();
        meme.put("description", description);
        meme.put("image", null);
        meme.put("user", uid);
        meme.put("likes", Arrays.asList());
        meme.put("date", new Timestamp(new Date()));
        meme.put("categories", memeCategories);

        db.collection("memes")
                .add(meme)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("MemeDoc", "DocumentSnapshot written with ID: " + documentReference.getId());
                        uploadImage(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MemeDoc", "Error adding document", e);
                    }
                });

        /*Context context = getApplicationContext();
        CharSequence text = "SUCESSO";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();*/
    }

    public void uploadImage(final String idDoc){
        final StorageReference ref = storageRef.child("memes/"+ idDoc);
        UploadTask uploadTask = ref.putFile(Uri.parse(imageUri));
        Log.d("idDoc", "uploadImage: " + idDoc);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();

            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    Log.d("ImageDownload", "onComplete: " + downloadUri);

                    Map<String, Object> meme = new HashMap<>();
                    meme.put("image", downloadUri.toString());
                    meme.put("meme_id", idDoc);

                    db.collection("memes").document(idDoc)
                            .update(meme);
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    final String uid = user.getUid();

                    db.collection("users").document(uid).update("points", FieldValue.increment(10));
                    memeCategories =  new ArrayList<String>();
                    finish();
                    goToMainActivity();
                    /*finish();*/
                } else {
                    // Handle failures
                    // ...
                    db.collection("memes").document(idDoc)
                            .delete();
                }
            }
        });
    }

    public void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("currentFragment", 0);
        startActivity(i);
    }

    public void goToCategoriesPublishActivity(View v) {
        Intent categoriesPublishIntent = new Intent(this, CategoriesPublish.class);
        categoriesPublishIntent.putExtra("imageUri", imageUri);
        EditText descriptionET = findViewById(R.id.description);
        description = String.valueOf(descriptionET.getText());
        categoriesPublishIntent.putExtra("description", description);
        startActivity(categoriesPublishIntent);
        finish();
    }
}
