package pt.srmeme.app.Ranking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import pt.srmeme.app.Adapters.RankingAdapter;
import pt.srmeme.app.Classes.Users;
import pt.srmeme.app.R;

public class Ranking extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference memeRef = db.collection("users");
    private RankingAdapter adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
    }

    public void goToPointsTable(View v) {
        Intent intentPointsTable = new Intent(this, PointsTableActivity.class);
        startActivity(intentPointsTable);
    }

    private void setUpRecyclerView() {
        Query query = memeRef.orderBy("points", Query.Direction.DESCENDING).limit(10);

        FirestoreRecyclerOptions<Users> options = new FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class)
                .build();

        adaptor = new RankingAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.rankingRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptor);
    }

    public void goBack(View v) {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpRecyclerView();
        adaptor.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adaptor.stopListening();
    }
}