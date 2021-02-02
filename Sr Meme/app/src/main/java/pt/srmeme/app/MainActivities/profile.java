package pt.srmeme.app.MainActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import pt.srmeme.app.Admin.Admin;
import pt.srmeme.app.R;
import pt.srmeme.app.Ranking.Ranking;

import static android.app.Activity.RESULT_OK;

public class profile extends Fragment {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String username;
    String profileImage;
    long points;
    String level;
    static ImageView profileIV;
    static TextView memeCounter;
    public int counter;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_PHOTO = 2;
    Uri imageUri;

    public profile() {
        // Required empty public constructor
    }

    public static profile newInstance(String param1, String param2) {
        profile fragment = new profile();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        MaterialAlertDialogBuilder builder;

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();
        Log.d("userUID", "onCreate: " + uid);
        final View v = inflater.inflate(R.layout.fragment_profile, container, false);

        final ImageView btnAdminPanel = v.findViewById(R.id.btnAdminPanel);
        final ImageView ranking = v.findViewById(R.id.btnRanking);
        final ImageView rankingAdmin = v.findViewById(R.id.btnRankingAdmin);
        final Button publishMeme = v.findViewById(R.id.noMemesBtn);
        final RelativeLayout noMemesLayout = v.findViewById(R.id.noMemesLayout);
        final RecyclerView profileMemeRecycler = v.findViewById(R.id.profileMemeRecycler);

        db.collection("memes").whereEqualTo("user", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            counter = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                counter++;
                            }
                        } else {
                            Log.d("MY_TAG", "Error getting documents: ", task.getException());
                        }
                        memeCounter = v.findViewById(R.id.publishesNumber);
                        if (counter > 1) {
                            memeCounter.setText(String.valueOf(counter) + " Publicações");
                        } else if (counter == 1) {
                            memeCounter.setText(1 + " Publicação");
                        } else {
                            profileMemeRecycler.setVisibility(View.INVISIBLE);
                            noMemesLayout.setVisibility(View.VISIBLE);
                            publishMeme.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                        galleryIntent.setType("image/*");
                                        startActivityForResult(galleryIntent, PICK_IMAGE);
                                }
                            });
                        }

                    }
                });

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        username = document.getString("username");
                        profileImage = document.getString("image_profile");
                        points = (long) Math.floor(document.getDouble("points"));
                        String status = document.getString("status");


                        if (status.equals("admin")){
                            btnAdminPanel.setVisibility(v.VISIBLE);
                            rankingAdmin.setVisibility(v.VISIBLE);
                        } else {
                            ranking.setVisibility(v.VISIBLE);
                        }

                        TextView usernameTV = v.findViewById(R.id.username);
                        TextView poinsTV = v.findViewById(R.id.points);
                        profileIV = v.findViewById(R.id.profileImg);
                        usernameTV.setText(username);
                        poinsTV.setText(String.valueOf(points) + " Pontos");
                        Picasso.get().load(profileImage).into(profileIV);
                        // Inflate the layout for this fragment
                        Log.d("VERIFICATION TO SCREEN", "onCreateView: " + uid +" "+username +" "+ profileImage);

                        memeLevel(points);

                        Log.d("Documents", "DocumentSnapshot data: " + username + "\n" + profileImage);
                    } else {
                        Log.d("Documents", "No such document");
                    }
                } else {
                    Log.d("Documents", "get failed with ", task.getException());
                }
            }

            public void memeLevel(long points) {
                TextView memeLevelTV = v.findViewById(R.id.memeLevel);
                if (points <= 100){
                    memeLevelTV.setText("Meme Bebé");
                } else if (points > 100 && points <= 500){
                    memeLevelTV.setText("Meme Criança");
                }else if (points > 500 && points <= 2000){
                    memeLevelTV.setText("Meme Adolescente");
                }else if (points > 2000 && points <= 10000){
                    memeLevelTV.setText("Meme Jovem");
                }else if (points > 10000 && points <= 30000){
                    memeLevelTV.setText("Meme Adulto");
                }else if (points > 30000 && points <= 60000){
                    memeLevelTV.setText("Senhor Meme");
                }else if (points > 60000){
                    memeLevelTV.setText("Deus Meme");
                }
            };

        });

        ranking.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent goToRanking = new Intent(v.getContext(), Ranking.class);
                startActivity(goToRanking);
            }
        });

        btnAdminPanel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent goToAdminPanel = new Intent(v.getContext(), Admin.class);
                startActivity(goToAdminPanel);
            }
        });

        ranking.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent goToRanking = new Intent(v.getContext(), Ranking.class);
                startActivity(goToRanking);
            }
        });

        rankingAdmin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent goToRanking = new Intent(v.getContext(), Ranking.class);
                startActivity(goToRanking);
            }
        });

        return v;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            Log.d("Image", "onCreate: " + imageUri);
            Intent publishIntent = new Intent(getView().getContext(), Publish.class);
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
}
