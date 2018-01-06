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
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("unused")
public class DisplayJson extends AppCompatActivity {

    public final static String EXTRA_THREAD_CANCELLED = "com.example.myfirstapp.THREAD_CANCELLED";
    public final static String EXTRA_LOGOUT = "com.example.myfirstapp.LOGOUT";

    static String rawJson;

    static public jsonData summaryData;

    static class jsonData {
        Map<String, String>[] notices;
        Map<String, String>[] tasks;
        @SuppressWarnings("unused")
        Map<String, String>[][] timetable;
    }

    static public class tempTOrN {
        @SuppressWarnings("unused")
        public Map<String, String>[] data;}

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

        try {
            Log.d("TEACHER TASK 1", summaryData.tasks[0].get("teacher"));
            Log.d("ROOM MONDAY 3RD PERIOD", summaryData.timetable[0][2].get("room"));
            Log.d("1st NOTICE", summaryData.notices[0].get("title"));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public static void updateJson(String rawJson, String field) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        if (field.equalsIgnoreCase("all")) {
            try {
                DisplayJson.summaryData = mapper.readValue(rawJson, jsonData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (field.equalsIgnoreCase("tasks")) {
            try {
                DisplayJson.summaryData.tasks = mapper.readValue("{\"data\":" + rawJson + "}", tempTOrN.class).data;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (field.equalsIgnoreCase("notices")) {
            try {
                DisplayJson.summaryData.notices = mapper.readValue("{\"data\":" + rawJson + "}", tempTOrN.class).data;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void updateJson(String rawJson) {
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(DisplayJson.EXTRA_LOGOUT, true);
        startActivity(intent);
        finish();
    }
}

//public class
