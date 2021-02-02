package pt.srmeme.app.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pt.srmeme.app.Login.Login;
import pt.srmeme.app.Classes.MemeProfile;
import pt.srmeme.app.R;

public class MemeProfileAdapter extends FirestoreRecyclerAdapter<MemeProfile, MemeProfileAdapter.MemeProfileHolder>  {
    String usernameFB;
    String profileImgFB;
    String likeUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference memeRef = db.collection("memes");
    String userStatus;
    ArrayList<String> memeLikes;
    ArrayList<String> memeCategories;

    public MemeProfileAdapter(@NonNull FirestoreRecyclerOptions<MemeProfile> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MemeProfileAdapter.MemeProfileHolder holder, int i, @NonNull final MemeProfile model) {
        Log.d("TEST", "onBindViewHolder: memeProfileAdapter");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        if (model.getDescription().trim().isEmpty()) {
            Log.d("TEST_DESC", "onBindViewHolder: igual");
            holder.memeDescription.setText("");
        } else {
            holder.memeDescription.setText(model.getDescription());
        }
        holder.iconMenu.setImageResource(R.drawable.ic_baseline_delete_24);
        Picasso.get().load(model.getImage()).into(holder.memeImg);
        db.collection("users").document(model.getUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d("TASK", "onComplete: " + task.getResult().getString("username"));
                    if (document.exists()) {
                        usernameFB = document.getString("username");
                        profileImgFB = document.getString("image_profile");
                        holder.username.setText(usernameFB);
                        Picasso.get().load(profileImgFB).into(holder.profileImg);
                    }
                }
            }
        });
        db.collection("memes").document(model.getMeme_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        memeLikes = (ArrayList) document.get("likes");
                        holder.likeAmount.setText(String.valueOf(memeLikes.size()));
                        if (user != null) {
                            likeUserId = user.getUid();
                            Log.d("MY_LIKES", "DocumentSnapshot data: " + document.get("likes"));
                            if (memeLikes.contains(likeUserId)) {
                                holder.iconLikes.setImageResource(R.drawable.ic_like_full);
                            } else {
                                holder.iconLikes.setImageResource(R.drawable.ic_like_border);
                            }
                        } else {
                            Log.d("MY_TAG", "No such document");
                        }
                    } else {
                        Log.d("MY_TAG", "get failed with ", task.getException());
                    }
                }
            }
        });

        holder.iconLikes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("MY_TAG", "onClick: entrou");
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = mAuth.getCurrentUser();
                    likeUserId = user.getUid();
                    Log.d("MY_TAG", "onBindViewHolder: " + model.getMeme_id());
                    db.collection("memes").document(model.getMeme_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("MY_LIKES", "DocumentSnapshot data: " + document.get("likes"));
                                    ArrayList<String> memeLikes = (ArrayList) document.get("likes");
                                    if (memeLikes.contains(likeUserId)) {
                                        db.collection("memes").document(model.getMeme_id()).update("likes", FieldValue.arrayRemove(likeUserId));
                                        holder.iconLikes.setImageResource(R.drawable.ic_like_border);
                                        holder.likeAmount.setText(String.valueOf(memeLikes.size() - 1));
                                        db.collection("users").document(model.getUser()).update("points", FieldValue.increment(-0.1));
                                    } else {
                                        db.collection("memes").document(model.getMeme_id()).update("likes", FieldValue.arrayUnion(likeUserId));
                                        holder.iconLikes.setImageResource(R.drawable.ic_like_full);
                                        holder.likeAmount.setText(String.valueOf(memeLikes.size() + 1));
                                        db.collection("users").document(model.getUser()).update("points", FieldValue.increment(0.1));
                                    }
                                } else {
                                    Log.d("MY_TAG", "No such document");
                                }
                            } else {
                                Log.d("MY_TAG", "get failed with ", task.getException());
                            }
                        }
                    });
            }
        });

        holder.iconMenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    userStatus = document.getString("status");
                                    if (userStatus.equals("admin") || user.getUid().equals(model.getUser())) {
                                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(holder.iconMenu.getContext());
                                        builder.setTitle("Desejas apagar este meme?");
                                        builder.setPositiveButton("Apagar",new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.collection("reported_memes").document(model.getMeme_id()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("TAG", "DocumentSnapshot successfully deleted!");
                                                    }
                                                });
                                                db.collection("memes").document(model.getMeme_id()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("TAG", "DocumentSnapshot successfully deleted!");
                                                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                                                        StorageReference memeRef = storageRef.child("memes/"+ model.getMeme_id());

                                                        memeRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // File deleted successfully
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                // Uh-oh, an error occurred!
                                                            }
                                                        });
                                                        db.collection("users").document(model.getUser()).update("points", FieldValue.increment(-10));
                                                        db.collection("users").document(model.getUser()).update("points", FieldValue.increment(- (memeLikes.size() * 0.1)));
                                                    }
                                                })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("TAG", "Error deleting document", e);
                                                            }
                                                        });

                                            }
                                        });
                                        builder.show();
                                    } else {
                                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(holder.iconMenu.getContext());
                                        builder.setTitle("Desejas denunciar este meme?");
                                        builder.setPositiveButton("Denunciar",new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.collection("memes").document(model.getMeme_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                memeLikes = (ArrayList) document.get("likes");
                                                                memeCategories = (ArrayList) document.get("categories");
                                                            }
                                                        }
                                                    }
                                                });
                                                Map<String, Object> meme = new HashMap<>();
                                                meme.put("description", model.getDescription());
                                                meme.put("image", model.getImage());
                                                meme.put("user", model.getUser());
                                                meme.put("likes", memeLikes);
                                                meme.put("date", new Timestamp(new Date()));
                                                meme.put("categories", memeCategories);
                                                meme.put("meme_id", model.getMeme_id());

                                                db.collection("reported_memes").document(model.getMeme_id())
                                                        .set(meme)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("SaveUSerInfo", "DocumentSnapshot successfully written!");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("SaveUSerInfo", "Error writing document", e);
                                                            }
                                                        });
                                            }
                                        });
                                        builder.show();
                                    }
                                }
                            }
                        }
                    });
                } else {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(holder.iconMenu.getContext());
                    builder.setTitle("Inicie Sessão");
                    builder.setMessage("Para poder interagir com os diferentes memes, tens que iniciar sessão");
                    builder.setPositiveButton("Iniciar Sessão",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Context context = holder.iconMenu.getContext();
                            Intent goToLogin = new Intent(context, Login.class);
                            context.startActivity(goToLogin);
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    @NonNull
    @Override
    public MemeProfileAdapter.MemeProfileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.meme_card,
                parent, false);

        return new MemeProfileAdapter.MemeProfileHolder(v);
    }

    class MemeProfileHolder extends RecyclerView.ViewHolder {
        TextView memeDescription;
        ImageView memeImg;
        TextView username;
        ImageView profileImg;
        ImageView iconLikes;
        TextView likeAmount;
        ImageView iconMenu;

        public MemeProfileHolder(@NonNull View itemView) {
            super(itemView);
            memeDescription = itemView.findViewById(R.id.memeDescription);
            memeImg = itemView.findViewById(R.id.memeImg);
            username = itemView.findViewById(R.id.username);
            profileImg = itemView.findViewById(R.id.userImg);
            iconLikes = itemView.findViewById(R.id.iconLike);
            likeAmount = itemView.findViewById(R.id.likeAmount);
            iconMenu = itemView.findViewById(R.id.iconMenu);
        }
    }
}
