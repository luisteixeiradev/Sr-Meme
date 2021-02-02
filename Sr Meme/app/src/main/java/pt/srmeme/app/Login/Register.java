package pt.srmeme.app.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import pt.srmeme.app.MainActivities.MainActivity;
import pt.srmeme.app.R;

public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        //Hide navigation buttons
        /*View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);*/
    }

    public void Register (View v) {

        EditText emailET = findViewById(R.id.email);
        EditText passwordET = findViewById(R.id.password);
        String email = String.valueOf(emailET.getText());
        String password = String.valueOf(passwordET.getText());
        EditText repeatPasswordET = findViewById(R.id.repeatPassword);
        String repeatPAssword = String.valueOf(repeatPasswordET.getText());

        if (password.equals(repeatPAssword)){
            if (password.length() >= 8){
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("TV", "onComplete: ");
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAG", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Log.d("GETUID", "onComplete: " + user.getUid());
                                    String uid = user.getUid();
                                    Log.d("GETUID", "onComplete2: " + uid);
                                    registerUserDB(uid);
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("EmailSent", "Email sent.");
                                                    }
                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "createUserWithEmail:failure", task.getException());

                                }

                            }
                        });
            } else {
                Context context = getApplicationContext();
                CharSequence text = "Insira uma password com pelo menos 8 caracteres";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            }

        } else {
            Context context = getApplicationContext();
            CharSequence text = "Passwords Diferentes";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }

    }

    public void registerUserDB(String uid) {
        EditText nameET = findViewById(R.id.name);
        EditText usernameET = findViewById(R.id.username);
        String name = String.valueOf(nameET.getText());
        String username = String.valueOf(usernameET.getText());
        Log.d("UID", "registerUserDB: " + uid);


        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("username", username);
        user.put("points", 0);
        user.put("status", "user");
        user.put("image_profile", "https://firebasestorage.googleapis.com/v0/b/sgbd-ficha-4.appspot.com/o/users%2Fperson.png?alt=media&token=cdb3b48e-0b27-4548-a24e-fa1d00e5d6b1");

        db.collection("users").document(uid)
                .set(user)
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
        mainActivity();
    }

    public void mainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("currentFragment", 0);
        startActivity(mainIntent);
    }

    public void goBack(View v) {
        finish();
    }

}
