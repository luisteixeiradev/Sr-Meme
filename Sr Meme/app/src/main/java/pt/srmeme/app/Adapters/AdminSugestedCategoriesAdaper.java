package pt.srmeme.app.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import pt.srmeme.app.Classes.Category;
import pt.srmeme.app.R;

public class AdminSugestedCategoriesAdaper extends FirestoreRecyclerAdapter<Category, AdminSugestedCategoriesAdaper.CategoryHolder>  {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference sugestedCategoriesRef = db.collection("sugested_categories");
    private CollectionReference usersRef = db.collection("users");

    public AdminSugestedCategoriesAdaper(@NonNull FirestoreRecyclerOptions<Category> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final AdminSugestedCategoriesAdaper.CategoryHolder holder, int i, @NonNull final Category model) {
        Log.d("TEST_ADMIN_CATEGORIES", "onBindViewHolder: " + model.getCategory_id());
        holder.sugestedCategoryName.setText(model.getName().substring(0, 1).toUpperCase() + model.getName().substring(1));
        holder.declineCategory.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                       sugestedCategoriesRef.document(model.getCategory_id()).delete();
                    }
                }
        );

        holder.acceptCategory.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        sugestedCategoriesRef.document(model.getCategory_id()).delete();

                        Map<String, Object> newCategory = new HashMap<>();
                        newCategory.put("name", model.getName());

                        db.collection("categories").add(newCategory).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("MemeDoc", "DocumentSnapshot written with ID: " + documentReference.getId());
                                Log.d("CAT_IN", "onSuccess: deu");
                                Map<String, Object> newCategory = new HashMap<>();
                                newCategory.put("category_id", documentReference.getId());
                                db.collection("categories").document(documentReference.getId()).update(newCategory);
                                usersRef.document(model.getUser_id()).update("points", FieldValue.increment(50));
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("MemeDoc", "Error adding document", e);
                                    }
                                });
                    }
                }
        );
    }

    @NonNull
    @Override
    public AdminSugestedCategoriesAdaper.CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sugested_categories_card,
                parent, false);

        return new AdminSugestedCategoriesAdaper.CategoryHolder(v);
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        TextView sugestedCategoryName;
        ImageView declineCategory;
        ImageView acceptCategory;


        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            sugestedCategoryName = itemView.findViewById(R.id.sugestedCategoryName);
            declineCategory = itemView.findViewById(R.id.declineCategory);
            acceptCategory = itemView.findViewById(R.id.acceptCategory);
        }
    }
}
