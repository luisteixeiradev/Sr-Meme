package pt.srmeme.app.Categories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import pt.srmeme.app.Dialogs.addCategoriesDialog;
import pt.srmeme.app.Dialogs.sugestionCategoriesDialog;
import pt.srmeme.app.Login.Login;
import pt.srmeme.app.R;

public class categories extends Fragment {

    public categories() {
        // Required empty public constructor
    }

    public static categories newInstance(String param1, String param2) {
        categories fragment = new categories();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_categories, container, false);
        final TextView sugestCategories = v.findViewById(R.id.sugestCategory);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        String uid;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef;
        final String[] status = new String[1];
        if (user != null){
            uid = user.getUid();
            docRef = db.collection("users").document(uid);

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
                                sugestCategories.setText("Adicionar");
                            }


                        }
                    }
                }


            });
        } else {
            status[0] = "notAuth";
        }

                    sugestCategories.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if(status[0].equals("admin")){
                                addCategory();
                            } else if(status[0].equals("user")){
                                showSugestCategories();
                            } else if (status[0].equals("notAuth")) {
                                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
                                builder.setTitle("Inicie Sessão");
                                builder.setMessage("Para poderes sugerir categorias tens que iniciar sessão");
                                builder.setPositiveButton("Iniciar Sessão",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Context context = sugestCategories.getContext();
                                        Intent goToLogin = new Intent(context, Login.class);
                                        context.startActivity(goToLogin);
                                    }
                                });
                                builder.show();
                            }
                        }

                        private void addCategory() {
                            addCategoriesDialog addCategoriesDialog = new addCategoriesDialog();
                            addCategoriesDialog.show(getFragmentManager(), "Add Categories");
                        }

                        public void showSugestCategories() {
                            sugestionCategoriesDialog sugestionCategoriesDialog = new sugestionCategoriesDialog();
                            sugestionCategoriesDialog.show(getFragmentManager(), "Sugestion Categories");
                        }


                    });

        // Inflate the layout for this fragment
        return v;
    }

}
