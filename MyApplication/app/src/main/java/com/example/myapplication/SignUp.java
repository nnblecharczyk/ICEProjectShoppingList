package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity implements View.OnClickListener{
    private Button moveToLogin;
    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText emailEditText;
    private EditText passwordEdit1;
    private EditText passwordEdit2;
    private Button signUpButton;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup);
        nameEditText = findViewById(R.id.nameEditText);
        surnameEditText = findViewById(R.id.surnameEditText);
        emailEditText = findViewById(R.id.signUpEmailEdit);
        passwordEdit1 = findViewById(R.id.passwordEdit1);
        passwordEdit2 = findViewById(R.id.passwordEdit2);
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);

        // Inicjalizacja przycisku moveToLogin przed użyciem
        moveToLogin = findViewById(R.id.goToLogInButton);
        moveToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
                finish();
            }
        });
    }
    public interface UserExistCallback {
        void onUserExist(boolean userExists);
    }
    public void isUserExist(final String email, final UserExistCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_CHECK_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean userExists = jsonObject.getBoolean("userExists");
                            callback.onUserExist(userExists);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onUserExist(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        callback.onUserExist(false);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void registerUser() {
        final String name = nameEditText.getText().toString();
        final String surname = surnameEditText.getText().toString();
        final String email = emailEditText.getText().toString();
        final String password1 = passwordEdit1.getText().toString();
        final String password2 = passwordEdit2.getText().toString();

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(password1)) {
            Toast.makeText(getApplicationContext(), "Password must contain at least 1 uppercase letter, 1 number and be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password1.equals(password2)) {
            Toast.makeText(getApplicationContext(), "Passwords must be identical", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(email)) {
            Toast.makeText(getApplicationContext(), "Incorrect email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        isUserExist(email, new UserExistCallback() {
            @Override
            public void onUserExist(boolean userExists) {
                if (userExists) {
                    Toast.makeText(getApplicationContext(), "User with provided email address already exists", Toast.LENGTH_SHORT).show();

                } else {
                    progressDialog.setMessage("Registering user...");
                    progressDialog.show();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST,
                            Constants.URL_REGISTER,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    progressDialog.dismiss();
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.hide();
                                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("name",name);
                            params.put("surname", surname);
                            params.put("email", email);
                            params.put("password", password1);
                            return params;
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(SignUp.this);
                    requestQueue.add(stringRequest);
                }
            }
        });
    }


    private boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";
        return password.matches(passwordPattern);
    }

    private boolean isEmailValid(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }



    @Override
    public void onClick(View v) {
        if (v == signUpButton)
            registerUser();
    }
}