package pt.srmeme.app.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import pt.srmeme.app.Classes.Users;
import pt.srmeme.app.R;

public class RankingAdapter extends FirestoreRecyclerAdapter<Users, RankingAdapter.UsersHolder> {
    public int counter = 1;

    public RankingAdapter(@NonNull FirestoreRecyclerOptions<Users> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final RankingAdapter.UsersHolder holder, int i, @NonNull final Users model) {
        Log.d("RANKING", "onBindViewHolder: "+ model.getUsername());
        holder.username.setText(model.getUsername());
        holder.points.setText(String.valueOf(model.getPoints()));
        holder.position.setText("#" + String.valueOf(counter));
        counter++;
    }

    @NonNull
    @Override
    public RankingAdapter.UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_ranking_card,
                parent, false);

        return new RankingAdapter.UsersHolder(v);
    }

    class UsersHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView points;
        TextView position;

        public UsersHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.usernameRecycler);
            points = itemView.findViewById(R.id.pointsRecycler);
            position = itemView.findViewById(R.id.positionRecycler);
        }
    }
}
