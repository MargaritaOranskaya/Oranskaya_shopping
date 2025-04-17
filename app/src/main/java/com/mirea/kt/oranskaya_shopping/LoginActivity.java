package com.mirea.kt.oranskaya_shopping;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Button btnLogin;
    private CheckBox cbShowPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        cbShowPassword = findViewById(R.id.cbShowPassword);

        cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = etLogin.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (login.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
                    return;
                }
                new LoginTask().execute(login, password);
            }
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        private String errorMessage = null;

        @Override
        protected String doInBackground(String... params) {
            String login = params[0];
            String password = params[1];
            String urlString = "https://android-for-students.ru/coursework/login.php";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postParameters = "lgn=" + URLEncoder.encode(login, "UTF-8") +
                        "&pwd=" + URLEncoder.encode(password, "UTF-8") +
                        "&g=" + URLEncoder.encode("RIBO-05-22", "UTF-8");

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(postParameters.getBytes());
                out.flush();
                out.close();

                int responseCode = urlConnection.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode == HttpURLConnection.HTTP_OK ?
                                urlConnection.getInputStream() :
                                urlConnection.getErrorStream()));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                errorMessage = "Ошибка: " + e.getMessage();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    int resultCode = jsonResponse.optInt("result_code", 0);

                    if (resultCode == 1) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Неверный логин/пароль", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Ошибка парсинга ответа", Toast.LENGTH_LONG).show();
                }
            } else if (errorMessage != null) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "Неверный логин/пароль", Toast.LENGTH_LONG).show();
            }
        }
    }
}
