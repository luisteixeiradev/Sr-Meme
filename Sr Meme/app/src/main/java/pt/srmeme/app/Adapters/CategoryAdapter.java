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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import pt.srmeme.app.Classes.Category;
import pt.srmeme.app.MainActivities.MainActivity;
import pt.srmeme.app.R;

public class CategoryAdapter extends FirestoreRecyclerAdapter<Category, CategoryAdapter.CategoryHolder> {

    public CategoryAdapter(@NonNull FirestoreRecyclerOptions<Category> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final CategoryHolder holder, int i, @NonNull final Category model) {
        holder.categoryName.setText(model.getName());
        MainActivity.memeCategories.contains(model.getName());
        if (MainActivity.memeCategories.contains(model.getName())) {
            holder.checkBox.setImageResource(R.drawable.ic_checkbox_checked);
        }
        if (MainActivity.memeCategories.contains(model.getName()) == false) {
            holder.checkBox.setImageResource(R.drawable.ic_checkbox_unchecked);
        }
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (MainActivity.memeCategories.contains(model.getName())) {
                    MainActivity.memeCategories.remove(model.getName());
                    holder.checkBox.setImageResource(R.drawable.ic_checkbox_unchecked);
                } else {
                    if (MainActivity.memeCategories.size() < 5) {
                        MainActivity.memeCategories.add(model.getName());
                        holder.checkBox.setImageResource(R.drawable.ic_checkbox_checked);
                    } else {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(holder.checkBox.getContext());
                        builder.setTitle("Limite alcançado");
                        builder.setMessage("Apenas podes pesquisar por 5 categorias diferentes");
                        builder.show();
                    }
                }
                Log.d("MEME_CATS", "onClick: " + MainActivity.memeCategories);
            }
        });

    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card_normal,
                parent, false);

        return new CategoryHolder(v);
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView checkBox;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryNameNormal);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
