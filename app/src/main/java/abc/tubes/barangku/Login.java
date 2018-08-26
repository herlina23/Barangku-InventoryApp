package abc.tubes.barangku;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private CheckBox chkRemember;
    private Button btnLogin;
    private EditText txtUsername;
    private EditText txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.checkSavedCredentials();

        this.initComponents();
    }

    private void initComponents()
    {
        this.chkRemember = (CheckBox) this.findViewById(R.id.chk_remember);
        this.btnLogin = (Button) this.findViewById(R.id.btn_login);
        this.txtUsername = (EditText) this.findViewById(R.id.txt_username);
        this.txtPassword = (EditText) this.findViewById(R.id.txt_password);
    }

    public void button_onClick(View view)
    {
        this.login();
    }

    private void checkSavedCredentials()
    {
        SharedPreferences handler = this.getPreferences(Context.MODE_PRIVATE);

        String username = handler.getString("username", "");
        String password = handler.getString("password", "");

        boolean loginCorrect = this.checkCredentials(username, password);

        if(loginCorrect)
            this.openHome(username);
    }

    private void login() {
        String username = this.txtUsername.getText().toString();
        String password = this.txtPassword.getText().toString();

        boolean loginCorrect = this.checkCredentials(username, password);

        if (loginCorrect == true) {
            boolean remember = this.chkRemember.isChecked();

            if (remember) {
                this.saveCredentials();
            }

            this.openHome(username);
        } else {
            Toast.makeText(this.getApplicationContext(), "Invalid username and/or password!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkCredentials(String username, String password)
    {
        if(username.equals("herlina") && password.equals("12345"))
            return true;
        else
            return false;
    }

    private void saveCredentials()
    {
        SharedPreferences handler = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = handler.edit();
        editor.putString("username", this.txtUsername.getText().toString());
        editor.putString("password", this.txtPassword.getText().toString());
        editor.commit();
    }

    private void openHome(String username)
    {
        Intent i = new Intent(this.getApplicationContext(), MainActivity.class);
        i.putExtra("username", username);
        finish();
        this.startActivity(i);
    }
}

