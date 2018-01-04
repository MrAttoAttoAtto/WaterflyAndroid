package com.example.attoa.testapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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

public class ShowNotices extends AppCompatActivity {

    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notices);

        setTitle("Noticeboard");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //noinspection ConstantConditions
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.activity_show_notices, null);

        TableLayout tableLayout = view.findViewById(R.id.noticeTable);

        // Add text
        for (int iter = 0; iter < DisplayJson.summaryData.notices.length; iter++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(16, 16, 16, 16);

            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);

            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);

            TextView titleView = new TextView(this);

            titleView.setText(DisplayJson.summaryData.notices[iter].get("title"));
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setMaxLines(1);
            //titleView.setMaxEms(22); I do NOT want to set this (but setting it DOES work as expected)
            titleView.setLayoutParams(layoutParams);
            if (DisplayJson.summaryData.notices[iter].get("title").contains("** New **")) {
                titleView.setTypeface(null, Typeface.BOLD);
            }

            Log.d("ITER", "" + iter);
            Log.d("NOTICE", DisplayJson.summaryData.notices[iter].get("title"));

            tableRow.addView(titleView);

            setOnClick(tableRow, iter);
            tableRow.setLayoutParams(rowParams);

            tableLayout.addView(tableRow);
        }

        tableLayout.addView(new TableRow(this));

        setContentView(view);
    }

    private void inDepthNoticeView(int noticeNum) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(DisplayJson.summaryData.notices[noticeNum].get("description"));
        builder.setTitle(DisplayJson.summaryData.notices[noticeNum].get("title"));

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void refreshNotices(View view) {
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
            apiConnect(LoadingPage.noticesUrl, LoadingPage.username, LoadingPage.password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

                    DisplayJson.updateJson(result, "notices");

                    Intent intent = new Intent(ShowNotices.this, ShowNotices.class);
                    startActivity(intent);
                    finish();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void setOnClick(final TableRow tableRow, final int iteration) {
        tableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inDepthNoticeView(iteration);
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
            thread.interrupt();
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
