package com.example.attoa.testapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "com.example.myfirstapp.USERNAME";
    public static final String EXTRA_PASSWORD = "com.example.myfirstapp.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        Boolean loginCached = true;

        Map<String, String> creds = new HashMap<>();

        try {
            FileInputStream fio = openFileInput("creds.ser");
            ObjectInputStream oos = new ObjectInputStream(fio);
            //noinspection unchecked
            creds = (Map<String, String>) oos.readObject();
            oos.close();
            fio.close();
        } catch (FileNotFoundException e) {
            loginCached = false;
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (loginCached) {
            Intent newIntent = new Intent(this, LoadingPage.class);
            newIntent.putExtra(EXTRA_USERNAME, creds.get("username"));
            newIntent.putExtra(EXTRA_PASSWORD, creds.get("password"));
            startActivity(newIntent);
            finish();
        }

        if (intent.hasExtra(LoadingPage.EXTRA_FAILED)) {
            Snackbar refresher = Snackbar.make(findViewById(R.id.mainConstraint), "Incorrect username or password, try again", Snackbar.LENGTH_LONG);
            refresher.show();
        } else if (intent.hasExtra(DisplayJson.EXTRA_LOGOUT)) {
            Snackbar refresher = Snackbar.make(findViewById(R.id.mainConstraint), "Successfully logged out", Snackbar.LENGTH_LONG);
            refresher.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Basically, do not think this app is safe in any way, we have neither the " +
                    "time nor resources to make a safe app. \n\nTherefore, we do not accept any liability (" +
                    "as permissible by local law) and any use of the app is at your own risk. Logging in with the " +
                    "app constitutes your agreement to these terms.");
            builder.setTitle("WARNING: This app is not safe!");
            AlertDialog alert = builder.create();
            alert.show();
        }

        EditText password = findViewById(R.id.editPassword);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
    }

    /**
     * Send Button
     */
    public void sendMessage(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, LoadingPage.class);

        EditText editUser = findViewById(R.id.editUser);
        String username = editUser.getText().toString();

        EditText editPass = findViewById(R.id.editPassword);
        String password = editPass.getText().toString();

        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_PASSWORD, password);

        startActivity(intent);
        finish();
    }
}
