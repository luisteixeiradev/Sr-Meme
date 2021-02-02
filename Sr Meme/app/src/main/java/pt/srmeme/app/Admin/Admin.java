package pt.srmeme.app.Admin;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import pt.srmeme.app.Adapters.AdminMemeAdapter;
import pt.srmeme.app.Adapters.AdminSugestedCategoriesAdaper;
import pt.srmeme.app.Classes.Category;
import pt.srmeme.app.Classes.Meme;
import pt.srmeme.app.R;

public class Admin extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference memeRef = db.collection("reported_memes");
    private CollectionReference categoriesRef = db.collection("sugested_categories");
    public static int currentFragment = 0;
    public static int previousFragment;
    private AdminMemeAdapter adaptor;
    private AdminSugestedCategoriesAdaper adapterCategories;
    public static BottomNavigationView bottomNavigation;
    public int reportedMemeCounter;
    public int suggestedCategoriesCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        bottomNavigation = findViewById(R.id.bottom_navigation_admin);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        openFragment(ReportedMemes.newInstance("", ""));
        checkReportedMemesCounter();

    }

    public void checkReportedMemesCounter() {
        db.collection("reported_memes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            reportedMemeCounter = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                reportedMemeCounter++;
                            }
                        } else {
                            Log.d("MY_TAG", "Error getting documents: ", task.getException());
                        }
                        if (reportedMemeCounter == 0) {
                            TextView txtNoReportedMemes = findViewById(R.id.txtNoReportedMemes);
                            txtNoReportedMemes.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void checkSuggestedCategoriesCounter() {
        db.collection("sugested_categories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            suggestedCategoriesCounter = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                suggestedCategoriesCounter++;
                            }
                        } else {
                            Log.d("MY_TAG", "Error getting documents: ", task.getException());
                        }
                        if (suggestedCategoriesCounter == 0) {
                            TextView txtNoSuggestedCategories = findViewById(R.id.txtNoCategorySuggested);
                            txtNoSuggestedCategories.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void goBack(View v) {
        currentFragment = 0;
        finish();
    }

    private void setUpMemeRecyclerView() {
        Query query = memeRef.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Meme> options = new FirestoreRecyclerOptions.Builder<Meme>()
                .setQuery(query, Meme.class)
                .build();

        adaptor = new AdminMemeAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.reportedMemesRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptor);
    }

    private void setUpCategoryRecyclerView() {
        Query query = categoriesRef;

        FirestoreRecyclerOptions<Category> options = new FirestoreRecyclerOptions.Builder<Category>()
                .setQuery(query, Category.class)
                .build();

        adapterCategories = new AdminSugestedCategoriesAdaper(options);

        RecyclerView recyclerView = findViewById(R.id.sugestedCategoriesRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterCategories);
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.reportedMemes:
                            openFragment(ReportedMemes.newInstance("", ""));
                            previousFragment = currentFragment;
                            currentFragment = 0;
                            onStart();
                            return true;
                        case R.id.sugestedCategories:
                            openFragment(SugestedCategories.newInstance("", ""));
                            previousFragment = currentFragment;
                            currentFragment = 1;
                            onStart();
                            return true;
                    }
                    return false;
                }
            };

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainLayoutAdmin, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentFragment == 0) {
            setUpMemeRecyclerView();
            adaptor.startListening();
            checkReportedMemesCounter();
        }
        if (currentFragment == 1) {
            setUpCategoryRecyclerView();
            adapterCategories.startListening();
            checkSuggestedCategoriesCounter();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentFragment == 1) {
            adaptor.stopListening();
        }
        if (currentFragment == 2) {
            adapterCategories.startListening();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}