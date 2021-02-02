package pt.srmeme.app.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import pt.srmeme.app.R;

public class sugestionCategoriesDialog extends AppCompatDialogFragment {
    private EditText sugestionCategory;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String sugestionCategoryUppercased;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.sugestion_categories_dialog, null);

        builder.setView(view)
                .setTitle("Sugerir Categoria")
                .setPositiveButton("Sugerir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();

                        Log.d("Sugestion Categories", "onClick: " + sugestionCategory.getText().toString());
                        /*sugestionCategoryUppercased = capitalize(sugestionCategory.getText().toString());*/
                        if (sugestionCategory.getText().toString().equals("") == false) {
                            Map<String, Object> sugestCategory = new HashMap<>();
                            sugestCategory.put("name", sugestionCategory.getText().toString());
                            sugestCategory.put("category_id", "");
                            sugestCategory.put("user_id", user.getUid());

                            db.collection("sugested_categories")
                                    .add(sugestCategory)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("Sugestion Categories", "DocumentSnapshot written with ID: " + documentReference.getId());
                                            db.collection("sugested_categories").document(documentReference.getId()).
                                                    update("category_id", documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Sugestion Categories", "Error adding document", e);
                                        }
                                    });
                        } else {
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(sugestionCategory.getContext());
                            builder.setTitle("O texto não pode estar vazio");
                            builder.setMessage("Tens de escrever alguma coisa");
                            builder.show();
                        }
                    }
                });

        sugestionCategory = view.findViewById(R.id.sugestCategoryET);

        return builder.create();
    }

    /*private String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }*/
}
