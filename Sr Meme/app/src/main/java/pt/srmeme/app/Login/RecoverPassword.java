package pt.srmeme.app.Login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import pt.srmeme.app.R;

public class RecoverPassword extends AppCompatActivity {

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        //Hide navigation buttons
        /*View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);*/
    }

    public void recoverPassword(View v){

        EditText emailET = findViewById(R.id.email);
        String email = String.valueOf(emailET.getText());

        if (!email.equals("")) {
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", "Email sent.");
                                finish();
                            }
                        }
                    });
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RecoverPassword.this);
            builder.setTitle("Campos por preencher");
            builder.setMessage("Tens de preencher todos os campos");
            builder.show();
        }

    }

    public void goBack(View v) {
        finish();
    }
}
