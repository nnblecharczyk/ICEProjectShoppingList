package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class List extends AppCompatActivity {

    private TextView listNameTextView;

    private int lastButtonId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.list);

        listNameTextView = findViewById(R.id.textView5);

        String listName = getIntent().getStringExtra("listName");
        listNameTextView.setText(listName);

        Button leaveButton = findViewById(R.id.leaveButton);
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(List.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getListProducts(listName);
    }

    private void getListProducts(String listName) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_GET_LIST_ID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean("error");
                            if (!error) {
                                String listId = jsonObject.getString("ID_Listy");
                                getProductList(listId);
                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(List.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(List.this, "JSON Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(List.this, "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("Nazwa", listName);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(List.this);
        requestQueue.add(stringRequest);
    }

    private void getProductList(String listId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_GET_LIST_PRODUCTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean("error");
                            if (!error) {
                                JSONArray products = jsonObject.getJSONArray("products");
                                for (int i = 0; i < products.length(); i++) {
                                    String productName = products.getString(i);
                                    addProductButtonToList(productName);
                                }
                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(List.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(List.this, "JSON Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(List.this, "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("listId", listId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(List.this);
        requestQueue.add(stringRequest);
    }

    private void addProductButtonToList(String productName) {
        Button productButton = new Button(this);
        productButton.setText(productName);

        ConstraintLayout layout = findViewById(R.id.main);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.setMargins(0, 32, 0, 0);
        layoutParams.setMarginStart(32);
        layoutParams.setMarginEnd(32);

        if (lastButtonId != -1) {
            layoutParams.topToBottom = lastButtonId;
        } else {
            layoutParams.topToBottom = R.id.textView5;
        }

        productButton.setLayoutParams(layoutParams);

        int newButtonId = ViewCompat.generateViewId();
        productButton.setId(newButtonId);
        layout.addView(productButton);

        lastButtonId = newButtonId;
    }
}
