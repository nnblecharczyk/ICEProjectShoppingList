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

    private DatePickerDialog datePickerDialog;
    private Button birthdayButton;
    private Button moveToLogin;
    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText emailEditText;
    private EditText phoneNumberEdit;
    private EditText passwordEdit1;
    private EditText passwordEdit2;
    private Button signUpButton;
    private String selectedDate = "";

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup);
        // Inicjalizacja pól formularza
        nameEditText = findViewById(R.id.nameEditText);
        surnameEditText = findViewById(R.id.surnameEditText);
        emailEditText = findViewById(R.id.signUpEmailEdit);
        phoneNumberEdit = findViewById(R.id.phoneNumberEdit);
        passwordEdit1 = findViewById(R.id.passwordEdit1);
        passwordEdit2 = findViewById(R.id.passwordEdit2);
        signUpButton = findViewById(R.id.signUpButton);

        // Inicjalizacja przycisku birthdayButton
        birthdayButton = findViewById(R.id.birthdateButton);
        initDatePicker();
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

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                selectedDate = makeDateString(day, month, year);
                birthdayButton.setText(selectedDate);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month) {
        if (month == 1) return "JAN";
        else if(month == 2) return "FEB";
        else if(month == 3) return "MAR";
        else if(month == 4) return "APR";
        else if(month == 5) return "MAY";
        else if(month == 6) return "JUN";
        else if(month == 7) return "JUL";
        else if(month == 8) return "AUG";
        else if(month == 9) return "SEP";
        else if(month == 10) return "OCT";
        else if(month == 11) return "NOV";
        else if(month == 12) return "DEC";
        return "JAN";
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
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
        final String birthdate = selectedDate;
        final String phone = phoneNumberEdit.getText().toString();
        final String password1 = passwordEdit1.getText().toString();
        final String password2 = passwordEdit2.getText().toString();

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || birthdate.isEmpty() || phone.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
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
                            params.put("birthdate", birthdate);
                            params.put("phone", phone);
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