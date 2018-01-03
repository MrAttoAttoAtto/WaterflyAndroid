package com.example.attoa.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DisplayJson extends AppCompatActivity {

    public final static String EXTRA_THREAD_CANCELLED = "com.example.myfirstapp.THREAD_CANCELLED";
    public final static String EXTRA_LOGOUT = "com.example.myfirstapp.LOGOUT";

    static public String rawJson;

    static public jsonData summaryData;

    static public class jsonData {
        public Map<String, String>[] notices;
        public Map<String, String>[] tasks;
        public Map<String, String>[][] timetable;
    }

    static public class tempTOrN {public Map<String, String>[] data;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_json);

        setTitle("Dashboard");

        Intent intent = getIntent();

        if (intent.hasExtra(DisplayJson.EXTRA_THREAD_CANCELLED)) {
            Snackbar refresher = Snackbar.make(findViewById(R.id.jsonConstraint), "Refresh cancelled. Please do not leave the screen while it is refreshing", Snackbar.LENGTH_LONG);
            refresher.show();
        }

        if (rawJson == null) {
            rawJson = intent.getStringExtra(LoadingPage.EXTRA_MESSAGE);
        }

        if (summaryData == null) {
            updateJson(rawJson);
        }

        Log.d("TEACHER TASK 1", summaryData.tasks[0].get("teacher"));
        Log.d("ROOM MONDAY 3RD PERIOD", summaryData.timetable[0][2].get("room"));
        Log.d("1st NOTICE", summaryData.notices[0].get("title"));
    }

    public static void updateJson(String rawJson, String field) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        if (field == "all") {
            try {
                DisplayJson.summaryData = mapper.readValue(rawJson, jsonData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (field == "tasks") {
            try {
                DisplayJson.summaryData.tasks = mapper.readValue("{\"data\":" + rawJson + "}", tempTOrN.class).data;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (field == "notices") {
            try {
                DisplayJson.summaryData.notices = mapper.readValue("{\"data\":" + rawJson + "}", tempTOrN.class).data;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void updateJson(String rawJson) {
        updateJson(rawJson, "all");
    }

    public void viewTasks(View view) {
        Intent intent = new Intent(this, ShowTasks.class);
        startActivity(intent);
    }

    public void viewNotices(View view) {
        Intent intent = new Intent(this, ShowNotices.class);
        startActivity(intent);
    }

    public void logout(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        DisplayJson.rawJson = null;
        DisplayJson.summaryData = null;
        this.deleteFile("creds.ser");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(DisplayJson.EXTRA_LOGOUT, true);
        startActivity(intent);
        finish();
    }
}

//public class
