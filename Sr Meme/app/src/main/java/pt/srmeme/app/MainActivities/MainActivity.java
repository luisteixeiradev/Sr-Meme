package pt.srmeme.app.MainActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

import pt.srmeme.app.Adapters.CategoryAdapter;
import pt.srmeme.app.Adapters.MemeAdapter;
import pt.srmeme.app.Adapters.MemeProfileAdapter;
import pt.srmeme.app.Categories.categories;
import pt.srmeme.app.Classes.Category;
import pt.srmeme.app.Classes.Meme;
import pt.srmeme.app.Classes.MemeProfile;
import pt.srmeme.app.Login.Login;
import pt.srmeme.app.R;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference memeRef = db.collection("memes");
    private CollectionReference categoryRef = db.collection("categories");

    private MemeAdapter adaptor;
    private CategoryAdapter categoryAdaptor;
    private MemeProfileAdapter profileAdapter;

    Query categoryQuery = categoryRef.orderBy("name");

    public static BottomNavigationView bottomNavigation;
    private FirebaseAuth mAuth;
    public static int currentFragment = 0;
    public static int previousFragment;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_PHOTO = 2;
    Uri imageUri;
    private int searchCategoryToogle = 0;

    public static ArrayList<String> categoriesList = new ArrayList<>();
    ArrayAdapter<String> categoriesListAdapter;
    public static ArrayList<String> memeCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        openFragment(home.newInstance("", ""));
        mAuth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if (extras != null) {
            currentFragment = extras.getInt("currentFragment");
        }

        db.collection("categories").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            categoriesList = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                if ( categoriesList.contains(document.getString("name")) == false) {
                                    categoriesList.add(document.getString("name"));
                                }
                            }
                        } else {
                            Log.d("MY_TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void setUpRecyclerView() {
        Query query = memeRef.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Meme> options = new FirestoreRecyclerOptions.Builder<Meme>()
                .setQuery(query, Meme.class)
                .build();

        adaptor = new MemeAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptor);
    }

    private void setUpProfileRecyclerView() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        Query query = memeRef.whereEqualTo("user", user.getUid()).orderBy("date", Query.Direction.DESCENDING);
        Log.d("TEST", "setUpCategoryRecyclerView: " + query);

        FirestoreRecyclerOptions<MemeProfile> options = new FirestoreRecyclerOptions.Builder<MemeProfile>()
                .setQuery(query, MemeProfile.class)
                .build();

        profileAdapter = new MemeProfileAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.profileMemeRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(profileAdapter);
    }

    public void showCategorySelected(View v) {
        EditText editText = findViewById(R.id.searchBar);
        ImageView searchCategory = findViewById(R.id.searchIcon);
        ImageView resetCategorySearched = findViewById(R.id.btnResetCategorySearched);
        if (categoriesList.contains(editText.getText().toString())) {
            categoryQuery = db.collection("categories").whereEqualTo("name", editText.getText().toString());
            searchCategoryToogle = 1;
            searchCategory.setVisibility(v.INVISIBLE);
            resetCategorySearched.setVisibility(v.VISIBLE);
            onStart();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
            builder.setTitle("Categoria inexistente");
            builder.setMessage("A categoria pela qual pesquisaste não existe");
            builder.show();
        }
    }

    public void showAllCategories(View v) {
        ImageView searchCategory = findViewById(R.id.searchIcon);
        ImageView resetCategorySearched = findViewById(R.id.btnResetCategorySearched);
        if (searchCategoryToogle == 1) {
            categoryQuery = db.collection("categories").orderBy("name");
            EditText editText = findViewById(R.id.searchBar);
            editText.setText("");
            searchCategoryToogle = 0;
            searchCategory.setVisibility(v.VISIBLE);
            resetCategorySearched.setVisibility(v.INVISIBLE);
            onStart();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
            builder.setTitle("Pesquisa não efetuada");
            builder.setMessage("Para voltares atrás tens de pesquisar por uma categoria primeiro");
            builder.show();
        }
    }

    private void setUpCategoryRecyclerView() {
        Log.d("TEST", "setUpCategoryRecyclerView: " + categoryQuery);

        FirestoreRecyclerOptions<Category> options = new FirestoreRecyclerOptions.Builder<Category>()
                .setQuery(categoryQuery, Category.class)
                .build();

        categoryAdaptor = new CategoryAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.categoriesRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoryAdaptor);
    }

    public void showCategoryItem() {
        categoriesListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, categoriesList);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.searchBar);
        textView.setAdapter(categoriesListAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("TEST", "onStart: current fragment" + currentFragment);
        if (currentFragment == 0) {
            setUpRecyclerView();
            adaptor.startListening();
        }
        if (currentFragment == 1) {
            Log.d("TEST", "onStart: entrou");
            setUpCategoryRecyclerView();
            categoryAdaptor.startListening();
            showCategoryItem();
        }
        if (currentFragment == 2) {
            setUpProfileRecyclerView();
            profileAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adaptor.stopListening();
        if (previousFragment == 1) {
            categoryAdaptor.stopListening();
        }
        if (currentFragment == 2) {
            profileAdapter.stopListening();
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            openFragment(profile.newInstance("", ""));
            currentFragment = 2;
        }
        if (currentUser == null) {
            Intent loginIntent = new Intent(this, Login.class);
            startActivity(loginIntent);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("OBP", "onBackPressed: " );
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.home:
                            openFragment(home.newInstance("", ""));
                            previousFragment = currentFragment;
                            currentFragment = 0;
                            onStart();
                            return true;
                        case R.id.categories:
                            openFragment(categories.newInstance("", ""));
                            previousFragment = currentFragment;
                            currentFragment = 1;
                            onStart();
                            return true;
                        case R.id.profile:
                            previousFragment = currentFragment;
                            goToProfile();
                            onStart();
                            return true;
                    }
                    return false;
                }
            };

    public void goToProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // Método utilitário para abrir um fragmento
    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void logout(View v) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
        builder.setTitle("Confirmas que queres sair da tua conta?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                openFragment(home.newInstance("", ""));
                currentFragment = 0;
                onStart();
                bottomNavigation.getMenu().getItem(0).setChecked(true);
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public void hideSugestCategory(View v) {
        RelativeLayout sugestionCard = findViewById(R.id.sugestionCategoryCard);
        sugestionCard.setVisibility(View.INVISIBLE);
    }

    public void goToGallery(View v) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, PICK_IMAGE);
        } else {
            goToProfile();
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

    public void showMemesByCategories(View v) {
        if (memeCategories.equals(new ArrayList<String>())) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Categorias insuficientes");
            builder.setMessage("Tens de selecionar pelo menos uma categoria");
            builder.show();
        } else {
            Intent i = new Intent(this, MemesByCategories.class);
            startActivity(i);
        }
    }

    public void editPhoto(View v) {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_PHOTO);
    }

    public void cleanCategories(View v) {
        if (memeCategories.equals(new ArrayList<>()) == false) {
            memeCategories = new ArrayList<>();
            openFragment(categories.newInstance("", ""));
            previousFragment = currentFragment;
            currentFragment = 1;
            onStart();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
            builder.setTitle("Não é possível limpar");
            builder.setMessage("Não tens nenhuma categoria selecionada");
            builder.show();
        }
    }
}
