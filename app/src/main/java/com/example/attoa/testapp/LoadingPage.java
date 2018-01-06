/*
 * WaterflyAndroid: a Firefly app for android, focusing on and streamlining the basics
 * Copyright (C) 2018  Atto Allas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.attoa.testapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoadingPage extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.JSON";
    public final static String EXTRA_FAILED = "com.example.myfirstapp.ERROR";
    private Thread thread;

    public static String username;
    public static String password;
    public static String tasksUrl;
    public static String noticesUrl;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_page);

        Intent intent = getIntent();

        username = intent.getStringExtra(MainActivity.EXTRA_USERNAME);
        password = intent.getStringExtra(MainActivity.EXTRA_PASSWORD);

        String summaryUrl = "https://firefly-server.herokuapp.com/summary";
        tasksUrl = "https://firefly-server.herokuapp.com/tasks";
        noticesUrl = "https://firefly-server.herokuapp.com/notices";

        try {
            apiConnect(summaryUrl, username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void apiConnect(String url, final String username, final String password) throws IOException {

        final URL realUrl = new URL(url);

        thread = new Thread(new Runnable() {
            public void run() {
                HttpURLConnection connection;
                try {
                    connection = (HttpURLConnection) realUrl.openConnection();
                    connection.setDoOutput(true);

                    String query = String.format("username=%s&password=%s", username, password);

                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(query.getBytes("UTF-8"));
                    }

                    Log.d("STATUS", Integer.toString(connection.getResponseCode()));

                    if (connection.getResponseCode() == 401) {
                        Intent intent = new Intent(LoadingPage.this, MainActivity.class);
                        intent.putExtra(EXTRA_FAILED, true);
                        startActivity(intent);
                        finish();
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            connection.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder a = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        //noinspection StringConcatenationInsideStringBufferAppend
                        a.append(inputLine + "\n");
                    in.close();

                    String result = a.toString();

                    Map<String, String> temp = new HashMap<>();
                    temp.put("username", username);
                    temp.put("password", password);

                    FileOutputStream fio = openFileOutput("creds.ser", MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fio);
                    oos.writeObject(temp);
                    oos.close();
                    fio.close();

                    finishedLoading(result);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    private void finishedLoading(String json) {
        Intent intent = new Intent(this, DisplayJson.class);
        intent.putExtra(EXTRA_MESSAGE, json);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // First check if the thread isAlive(). To avoid NullPointerException
        try {
            thread.interrupt();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        username = null;
        password = null;

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
