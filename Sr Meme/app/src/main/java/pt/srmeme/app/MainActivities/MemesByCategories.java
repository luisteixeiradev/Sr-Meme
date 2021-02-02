package pt.srmeme.app.MainActivities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import pt.srmeme.app.Adapters.MemeByCategoriesAdapter;
import pt.srmeme.app.Classes.Meme;
import pt.srmeme.app.Login.Login;
import pt.srmeme.app.R;

public class MemesByCategories extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference memeRef = db.collection("memes");
    private CollectionReference categoryRef = db.collection("categories");
    public static ArrayList<String> memeCat = new ArrayList<String>();
    private int memeCounter;
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_PHOTO = 2;
    Uri imageUri;
    public static Activity fa;

    private MemeByCategoriesAdapter adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meme_by_categories);
        mAuth = FirebaseAuth.getInstance();
        fa = this;

        Log.d("MEME_CAT", "onCreate: " + MainActivity.memeCategories);
        memeRef.whereArrayContainsAny("categories", MainActivity.memeCategories).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            memeCounter = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                memeCounter++;
                            }
                        } else {
                            Log.d("MY_TAG", "Error getting documents: ", task.getException());
                        }

                        if (memeCounter != 0) {
                            RecyclerView recyclerView = findViewById(R.id.memeByCategoryRecyclerView);
                            recyclerView.setVisibility(View.VISIBLE);
                            showMemesByCategories();
                            adaptor.startListening();
                        } else {
                            if (MainActivity.memeCategories.size() > 1) {
                                TextView noResultsText = findViewById(R.id.noResultsText);
                                noResultsText.setText("Estas categorias ainda não tem nenhum meme, queres ser o/a primeiro/a?");
                            }
                            RelativeLayout viewForNoResults = findViewById(R.id.viewForNoResults);
                            viewForNoResults.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void goToGallery(View v) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, PICK_IMAGE);
        } else {
            Intent i = new Intent(this, Login.class);
            startActivity(i);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            Log.d("Image", "onCreate: " + imageUri);
            Intent publishIntent = new Intent(this, Publish.class);
            publishIntent.putExtra("imageUri", imageUri.toString());
            startActivity(publishIntent);
        }
        if (resultCode == RESULT_OK && requestCode == PICK_PHOTO) {
            imageUri = data.getData();
            Log.d("Image", "onCreate: " + imageUri);

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            final FirebaseUser user = mAuth.getCurrentUser();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            final StorageReference ref = storageRef.child("profiles_pics/"+ user.getUid() +".jpg");
            UploadTask uploadTask = ref.putFile(Uri.parse(String.valueOf(imageUri)));
            Log.d("idDoc", "uploadImage: " + user.getUid());

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
                        db.collection("users").document(user.getUid()).update("image_profile", String.valueOf(downloadUri));
                        Picasso.get().load(String.valueOf(downloadUri)).into(profile.profileIV);
                    } else {

                    }
                }
            });
        }
    }

    public void showMemesByCategories() {
        memeCat = MainActivity.memeCategories;
        Query query = memeRef.whereArrayContainsAny("categories", memeCat).orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Meme> options = new FirestoreRecyclerOptions.Builder<Meme>()
                .setQuery(query, Meme.class)
                .build();

        adaptor = new MemeByCategoriesAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.memeByCategoryRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptor);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (memeCounter != 0) {
            adaptor.stopListening();
        }
    }

    public void goBack(View v){
        finish();
    }
}