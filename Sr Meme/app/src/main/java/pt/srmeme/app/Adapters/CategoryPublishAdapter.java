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

import pt.srmeme.app.Classes.CategoryPublish;
import pt.srmeme.app.MainActivities.Publish;
import pt.srmeme.app.R;

public class CategoryPublishAdapter extends FirestoreRecyclerAdapter<CategoryPublish, CategoryPublishAdapter.CategoryHolder> {

    public CategoryPublishAdapter(@NonNull FirestoreRecyclerOptions<CategoryPublish> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final CategoryHolder holder, final int i, @NonNull final CategoryPublish model) {
        holder.categoryName.setText(model.getName());
        Log.d("MY_CATS", "onBindViewHolder: " + model.getName());
        if (Publish.memeCategories.contains(model.getName())) {
            holder.checkBox.setImageResource(R.drawable.ic_checkbox_checked);
        }
        if (Publish.memeCategories.contains(model.getName()) == false) {
            holder.checkBox.setImageResource(R.drawable.ic_checkbox_unchecked);
        }
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Publish.memeCategories.contains(model.getName())) {
                    Publish.memeCategories.remove(model.getName());
                    holder.checkBox.setImageResource(R.drawable.ic_checkbox_unchecked);
                } else {
                    if (Publish.memeCategories.size() < 3) {
                        Publish.memeCategories.add(model.getName());
                        holder.checkBox.setImageResource(R.drawable.ic_checkbox_checked);
                    } else {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(holder.checkBox.getContext());
                        builder.setTitle("Limite alcançado");
                        builder.setMessage("Apenas podes selecionar 3 categorias por meme");
                        builder.show();
                    }
                }
                Log.d("MEME_CATS", "onClick: " + Publish.memeCategories);
            }
        });
    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card,
                parent, false);

        return new CategoryHolder(v);
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView checkBox;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
