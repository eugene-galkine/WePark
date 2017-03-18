package com.eg.upark;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Eugene Galkine on 10/23/2016.
 */

public class LoginHandler
{
    private SharedPreferences settings;
    private ContentResolver resolver;
    private Dialog login;
    private SharedPreferences.Editor settingseditor;

    public LoginHandler(SharedPreferences settings, ContentResolver resolver)
    {
        this.settings = settings;
        this.resolver = resolver;

        tryLogin();
    }

    private void tryLogin()
    {
        /*//try to get saved login data
        String username = settings.getString("username", "");
        String password = settings.getString("password", "");
        boolean skiplogin = settings.getBoolean("skiplogin", false);

        if ((username.length() <= 0 || password.length() <= 0) && !skiplogin)
            loginPromt();
        else if (skiplogin)
        {*/
            String android_id = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
            MainActivity.getInstance().setClient(android_id);
        /*} else
        {
            MainActivity.getInstance().setClient(username, password);
        }*/
    }

    @Deprecated
    private void loginPromt()
    {
        // Create Object of Dialog class
        login = new Dialog(MainActivity.getInstance());
        login.setCancelable(false);
        // Set GUI of login screen
        login.setContentView(R.layout.login_dialog);
        login.setTitle(R.string.label_login);

        // Init UI items of login GUI
        final Button btnLogin = (Button) login.findViewById(R.id.btnLogin);
        final Button btnSkip = (Button) login.findViewById(R.id.btnSkipLogin);
        final EditText txtUsername = (EditText) login.findViewById(R.id.txtUsername);
        final EditText txtPassword = (EditText) login.findViewById(R.id.txtPassword);

        // Attached listener for login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtUsername.getText().toString().trim().length() > 0 && txtPassword.getText().toString().trim().length() > 0)
                {
                    //try to login
                    //MainActivity.getInstance().setClient(txtUsername.getText().toString(), txtPassword.getText().toString());

                    settingseditor = settings.edit();
                    settingseditor.putString("username", txtUsername.getText().toString());
                    settingseditor.putString("password", txtPassword.getText().toString());
                } else {
                    Toast.makeText(MainActivity.getInstance(), "Please enter a Username and Password", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Button listener to skip login
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //try to login
                String android_id = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
                MainActivity.getInstance().setClient(android_id);

                settingseditor = settings.edit();
                settingseditor.putBoolean("skiplogin", true);
            }
        });

        // Make dialog box visible.
        login.show();
    }

    public void logout()
    {
        if (settingseditor == null)
            settingseditor = settings.edit();

        settingseditor.remove("username");
        settingseditor.remove("password");
        settingseditor.remove("skiplogin");
        settingseditor.commit();

        tryLogin();
    }

    public void loginFailed()
    {
        if (settingseditor != null)
            settingseditor.clear();

        if (login == null || !login.isShowing())
            tryLogin();
    }

    public void loginSuccess()
    {
        if (login != null)
            login.dismiss();
        // Save username and passwords
        if (settingseditor != null)
            settingseditor.commit();
    }

    public void dismissDialog()
    {
        if (login != null)
            login.dismiss();
    }
}
