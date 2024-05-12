package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Dashboard extends AppCompatActivity {

    private Button addListButton;
    private String userID;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;

    private int lastButtonId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dashboard);

        addListButton = findViewById(R.id.addListButton);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);

        userID = LogIn.getLoggedInUserId();

        addListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddListDialog();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkUserLists();
    }

    private void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_list, null);
        EditText listNameEditText = dialogView.findViewById(R.id.listNameEditText);

        builder.setView(dialogView)
                .setTitle("Create a new list")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String listName = listNameEditText.getText().toString().trim();
                        if (!listName.isEmpty()) {
                            addListToDatabase(listName);
                        } else {
                            Toast.makeText(Dashboard.this, "Please enter a list name", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addListToDatabase(String listName) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_ADD_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            removeExistingButtons();
                            checkUserLists();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Dashboard.this, "JSON Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(Dashboard.this, "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("Nazwa", listName);
                params.put("userid", userID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(Dashboard.this);
        requestQueue.add(stringRequest);
    }

    private void removeExistingButtons() {
        ConstraintLayout layout = findViewById(R.id.main);
        layout.removeViews(4, layout.getChildCount() - 4);
        lastButtonId = -1;
    }

    private void checkUserLists() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_GET_LISTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean("error");
                            if (!error) {
                                JSONArray lists = jsonObject.getJSONArray("lists");
                                if (lists.length() > 0) {
                                    textView2.setVisibility(View.GONE);
                                    textView3.setVisibility(View.GONE);
                                    for (int i = 0; i < lists.length(); i++) {
                                        String listName = lists.getString(i);
                                        addButtonForList(listName);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Dashboard.this, "JSON Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(Dashboard.this, "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userid", userID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(Dashboard.this);
        requestQueue.add(stringRequest);
    }

    private void addButtonForList(String listName) {
        Button button = new Button(this);
        button.setText(listName);
        ConstraintLayout layout = findViewById(R.id.main);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
               350,
                80
        );
        layoutParams.topToBottom = (lastButtonId == -1) ? R.id.textView4 : lastButtonId;
        layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.setMargins(0, 16, 0, 0);
        button.setLayoutParams(layoutParams);
        lastButtonId = ViewCompat.generateViewId();
        button.setId(lastButtonId);
        layout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, List.class);
                intent.putExtra("listName", listName);
                startActivity(intent);
            }
        });
    }
}
