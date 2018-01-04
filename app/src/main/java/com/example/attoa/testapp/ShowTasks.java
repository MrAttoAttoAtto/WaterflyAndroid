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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShowTasks extends AppCompatActivity {

    private Thread threadGen;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tasks);

        setTitle("Tasks");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //noinspection ConstantConditions
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.activity_show_tasks, null);

        TableLayout tableLayout = view.findViewById(R.id.taskTable);

        // Add text
        for (int iter = 0; iter < DisplayJson.summaryData.tasks.length; iter++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(16, 16, 16, 16);

            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);

            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);

            TableRow.LayoutParams layoutLayoutParams = new TableRow.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);

            TextView titleView = new TextView(this);
            titleView.setText(DisplayJson.summaryData.tasks[iter].get("title"));
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setMaxLines(1);
            //titleView.setMaxEms(22); I do NOT want to set this (but setting it DOES work as expected)
            titleView.setLayoutParams(layoutParams);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setTextColor(Color.parseColor("#404040"));

            TextView classView = new TextView(this);
            classView.setText(DisplayJson.summaryData.tasks[iter].get("class"));
            classView.setLayoutParams(layoutParams);

            TextView teacherView = new TextView(this);
            teacherView.setText(getString(R.string.set_by) + DisplayJson.summaryData.tasks[iter].get("teacher"));
            teacherView.setLayoutParams(layoutParams);

            TextView dueView = new TextView(this);

            dueView.setText(getString(R.string.due) + DisplayJson.summaryData.tasks[iter].get("dueDate"));

            dueView.setLayoutParams(layoutParams);
            dueView.setTypeface(null, Typeface.BOLD);

            Log.d("ITER", "" + iter);
            Log.d("CLASS", DisplayJson.summaryData.tasks[iter].get("class"));

            container.addView(titleView);
            container.addView(classView);
            container.addView(teacherView);
            container.addView(dueView);
            container.setLayoutParams(layoutLayoutParams);

            tableRow.addView(container);

            setOnClick(tableRow, iter);
            tableRow.setLayoutParams(rowParams);

            tableLayout.addView(tableRow);
        }

        tableLayout.addView(new TableRow(this));

        setContentView(view);
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private void inDepthTaskView(final int taskNum) {
        Log.d("NUM", "" + taskNum);
        Log.d("TEACHER", DisplayJson.summaryData.tasks[taskNum].get("teacher"));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        SpannableStringBuilder message = new SpannableStringBuilder();

        message.append("Title: " + DisplayJson.summaryData.tasks[taskNum].get("title"));

        int lenAfterTitle = message.length();

        message.append("\n\nDescription: " + DisplayJson.summaryData.tasks[taskNum].get("description"));
        message.append("\n\nSet " + DisplayJson.summaryData.tasks[taskNum].get("setDate"));

        int lenBeforeDue = message.length();

        message.append("\n\nDue " + DisplayJson.summaryData.tasks[taskNum].get("dueDate"));

        int lenAfterDue = message.length();

        String className = (DisplayJson.summaryData.tasks[taskNum].get("class").equalsIgnoreCase(
                "PERSONAL TASK")) ? "You" : DisplayJson.summaryData.tasks[taskNum].get("class");

        message.append("\n\nSet to " + className);
        message.append(" by " + DisplayJson.summaryData.tasks[taskNum].get("teacher"));

        message.setSpan(new StyleSpan(Typeface.BOLD), lenBeforeDue, lenAfterDue, 0);
        message.setSpan(new StyleSpan(Typeface.BOLD), 0, lenAfterTitle, 0);

        builder.setMessage(message);
        //builder.setTitle(DisplayJson.summaryData.tasks[taskNum].get("title"));

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void refreshTasks(View view) {
        ConstraintLayout layout = (ConstraintLayout) view.getParent();
        ConstraintSet set = new ConstraintSet();
        ProgressBar loadingBar = new ProgressBar(this);
        FloatingActionButton refreshButton = view.findViewById(R.id.floatingActionButton);

        refreshButton.setOnClickListener(null);

        int id = View.generateViewId();

        loadingBar.setId(id);
        layout.addView(loadingBar);

        set.clone(layout);

        set.connect(id, ConstraintSet.RIGHT, R.id.floatingActionButton, ConstraintSet.LEFT, 100);
        set.connect(id, ConstraintSet.BOTTOM, R.id.floatingActionButton, ConstraintSet.BOTTOM, 11);

        set.applyTo(layout);

        try {
            apiConnect(LoadingPage.tasksUrl, LoadingPage.username, LoadingPage.password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void apiConnect(String url, final String username, final String password) throws IOException {

        final URL realUrl = new URL(url);

        threadGen = new Thread(new Runnable() {
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

                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            connection.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder a = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        //noinspection StringConcatenationInsideStringBufferAppend
                        a.append(inputLine + "\n");
                    in.close();

                    String result = a.toString();

                    in.close();

                    DisplayJson.updateJson(result, "tasks");

                    Intent intent = new Intent(ShowTasks.this, ShowTasks.class);
                    startActivity(intent);
                    finish();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadGen.start();
    }

    private void setOnClick(final TableRow tableRow, final int iteration) {
        tableRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inDepthTaskView(iteration);
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // First check if the thread isAlive(). To avoid NullPointerException
        try {
            threadGen.interrupt();
            Intent intent = new Intent(this, DisplayJson.class);
            intent.putExtra(DisplayJson.EXTRA_THREAD_CANCELLED, true);
            startActivity(intent);
            finish();
        } catch (NullPointerException e) {
            e.printStackTrace();
            super.onBackPressed();
        }

    }
}
