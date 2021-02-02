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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import pt.srmeme.app.R;

public class addCategoriesDialog extends AppCompatDialogFragment {
    private EditText addCategory;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    /*private String uppercaseCategory;*/
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_category, null);

        builder.setView(view)
                .setTitle("Adicionar Categoria")
                .setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.d("Sugestion Categories", "onClick: " + addCategory.getText().toString());
                        /*uppercaseCategory = capitalize(addCategory.getText().toString());*/
                        if (addCategory.getText().toString().equals("") == false) {
                            Map<String, Object> sugestCategory = new HashMap<>();
                            sugestCategory.put("name", addCategory.getText().toString());
                            sugestCategory.put("category_id", "");

                            db.collection("categories")
                                    .add(sugestCategory)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("Sugestion Categories", "DocumentSnapshot written with ID: " + documentReference.getId());
                                            db.collection("categories").document(documentReference.getId()).
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
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(addCategory.getContext());
                            builder.setTitle("O texto não pode estar vazio");
                            builder.setMessage("Tens de escrever alguma coisa");
                            builder.show();
                        }
                    }
                });

        addCategory = view.findViewById(R.id.addCategoryET);

        return builder.create();
    }

    /*private String capitalize(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
            Log.d("MATCHER", "capitalize group1: " + capMatcher.group(1));
            Log.d("MATCHER", "capitalize group2: " + capMatcher.group(2));
        }

        return capMatcher.appendTail(capBuffer).toString();
    }*/
}
