package pt.srmeme.app.Login;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pt.srmeme.app.MainActivities.MainActivity;
import pt.srmeme.app.MainActivities.MemesByCategories;
import pt.srmeme.app.R;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    int key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        /*//Hide navigation buttons
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);*/

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            key = extras.getInt("keyMemesByCategory");
        }
    }

    public void login(View v) {
        EditText emailET = findViewById(R.id.email);
        EditText passwordET = findViewById(R.id.password);
        String email = String.valueOf(emailET.getText());
        String password = String.valueOf(passwordET.getText());
        if (email.equals("") || password.equals("")) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Login.this);
            builder.setTitle("Campos por preencher");
            builder.setMessage("Tens de preencher todos os campos");
            builder.show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("pt/srmeme/srmeme/Login", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                goToMainActivity();

                            } else {
                                // If sign in fails, display a message to the user.
                                String resultTask = task.getException().toString();
                                Log.w("pt/srmeme/srmeme/Login", "signInWithEmail:failure", task.getException());
                                if (resultTask.contains("There is no user record corresponding to this identifier. The user may have been deleted")) {
                                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Login.this);
                                    builder.setTitle("Email inexistente");
                                    builder.setMessage("Esse email não existe");
                                    builder.show();
                                } else if (resultTask.contains("The password is invalid or the user does not have a password")) {
                                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Login.this);
                                    builder.setTitle("Password errada");
                                    builder.setMessage("A password inserida não corresponde ao email");
                                    builder.show();
                                } else if (resultTask.contains("The email address is badly formatted")) {
                                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Login.this);
                                    builder.setTitle("Email mal formatado");
                                    builder.setMessage("Por favor insere um email válido");
                                    builder.show();
                                }
                            }
                            // ...
                        }
                    });
        }

}

    public void goToRegisterActivity(View v) {
        Intent registerIntent = new Intent(this, Register.class);
        startActivity(registerIntent);
    }

    public void goToMainActivity () {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("currentFragment", 0);
        startActivity(mainIntent);
    }

    public void goBack(View v) {
        if (key == 1) {
            Log.d("KEY", "goBack: " + key);
            Intent i = new Intent(this, MemesByCategories.class);
            startActivity(i);
            finish();
        } else {
            FragmentManager fm = getFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                super.onBackPressed();
                MainActivity.bottomNavigation.getMenu().getItem(MainActivity.previousFragment).setChecked(true);
            }
        }
    }

    public void goToForgotPasswordActivity(View v) {
        Intent forgotPasswordIntent = new Intent(this, RecoverPassword.class);
        startActivity(forgotPasswordIntent);
    }
}
