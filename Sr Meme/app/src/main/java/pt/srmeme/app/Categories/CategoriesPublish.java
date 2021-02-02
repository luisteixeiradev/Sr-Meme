package pt.srmeme.app.Categories;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import pt.srmeme.app.Dialogs.addCategoriesDialog;
import pt.srmeme.app.Dialogs.sugestionCategoriesDialog;
import pt.srmeme.app.Adapters.CategoryPublishAdapter;
import pt.srmeme.app.Classes.CategoryPublish;
import pt.srmeme.app.MainActivities.Publish;
import pt.srmeme.app.R;

public class CategoriesPublish extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference categoryRef = db.collection("categories");

    private CategoryPublishAdapter adapter;
    private CollectionReference categoryPublishRef = db.collection("categories");

    String imageUri;
    String description;
    private int searchCategoryToogle = 0;
    Query categoryQuery = categoryPublishRef.orderBy("name");
    public static ArrayList<String> categoriesList = new ArrayList<>();
    ArrayAdapter<String> categoriesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_publish);
        final TextView sugestCategory = findViewById(R.id.sugestCategory);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("users").document(uid);
        final String[] status = new String[1];

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        status[0] = document.getString("status");
                        Log.d("Status", "onComplete: " + status[0]);
                        if(status[0].equals("admin")){
                            sugestCategory.setText("Adicionar");
                        }

                    }
                }
            }


        });

        sugestCategory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(status[0].equals("admin")){
                    addCategory();
                } else {
                    showSugestCategories();
                }
            }

            private void addCategory() {
                addCategoriesDialog addCategoriesDialog = new addCategoriesDialog();
                addCategoriesDialog.show(getSupportFragmentManager(), "Add Categories");
            }

            public void showSugestCategories() {
                sugestionCategoriesDialog sugestionCategoriesDialog = new sugestionCategoriesDialog();
                sugestionCategoriesDialog.show(getSupportFragmentManager(), "Sugestion Categories");
            }
        });

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if (extras != null) {
            imageUri = extras.getString("imageUri");
            description = extras.getString("description");
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
                        showCategoryItem();
                    }
                });
    }

    public void setUpRecyclerView() {

        FirestoreRecyclerOptions<CategoryPublish> options = new FirestoreRecyclerOptions.Builder<CategoryPublish>()
                .setQuery(categoryQuery, CategoryPublish.class)
                .build();

        adapter = new CategoryPublishAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.categoriesPublishRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public void saveCategories(View v) {
        Intent publishIntent = new Intent(this, Publish.class);
        publishIntent.putExtra("imageUri", imageUri);
        publishIntent.putExtra("description", description);
        startActivity(publishIntent);
        finish();
    }

    public void cleanCategories(View v) {
        if (Publish.memeCategories.equals(new ArrayList<String>()) == false) {
            Publish.memeCategories = new ArrayList<>();
            recreate();
            Log.d("MEME_CATS", "cleanCategories: " + Publish.memeCategories);
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
            builder.setTitle("Não é possível limpar");
            builder.setMessage("Não tens nenhuma categoria selecionada");
            builder.show();
        }

    }

    public void showAllCategories(View v) {
        ImageView searchCategory = findViewById(R.id.searchIconPublish);
        ImageView resetCategorySearched = findViewById(R.id.btnResetCategorySearchedPublish);
        if (searchCategoryToogle == 1) {
            categoryQuery = db.collection("categories").orderBy("name");
            EditText editText = findViewById(R.id.searchBarPublish);
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

    public void showCategoryItem() {
        Log.d("CATEGORY_PUBLISH__LIST", "onComplete: " + categoriesList);
        categoriesListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, categoriesList);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.searchBarPublish);
        textView.setAdapter(categoriesListAdapter);
    }

    public void showCategorySelected(View v) {
        ImageView searchCategory = findViewById(R.id.searchIconPublish);
        ImageView resetCategorySearched = findViewById(R.id.btnResetCategorySearchedPublish);
        EditText editText = findViewById(R.id.searchBarPublish);
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

    @Override
    protected void onStart() {
        super.onStart();
        setUpRecyclerView();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
