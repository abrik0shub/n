package com.example.examen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SignIn extends AppCompatActivity {
    EditText usern;
    EditText passw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        passw = findViewById(R.id.passw);
        usern = findViewById(R.id.usern);
    }



    public void toSignUp(View v){
        startActivity(new Intent(this,SignUp.class));
    }


    public void toStartScreen(View v){
        startActivity(new Intent(this,StartScreenActivity.class));
    }



    public void clc (View v)
    {

        RequestTask requestTask = new RequestTask();
        boolean suc = true;
        if(usern.getText().toString().isEmpty())
        {
            Toast.makeText(this,"Input name", Toast.LENGTH_LONG).show();
            suc=false;
        }
        if(passw.getText().toString().isEmpty())
        {
            Toast.makeText(this,"Input password", Toast.LENGTH_LONG).show();
            suc=false;
        }


        if(suc==true) {
            String[] str = {usern.getText().toString(), passw.getText().toString()};
            requestTask.execute(str);
        }
    }
    public class RequestTask extends AsyncTask<String, Void, Void> {

        String resultString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String myURL = "http://cars.areas.su/login";
                String parammetrs = "username="+params[0]+"&password="+params[1];
                byte[] data = null;
                InputStream is = null;

                try {
                    URL url = new URL(myURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                    OutputStream os = conn.getOutputStream();
                    data = parammetrs.getBytes("UTF-8");
                    os.write(data);
                    data = null;

                    conn.connect();
                    int responseCode= conn.getResponseCode();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    if (responseCode == 200) {
                        is = conn.getInputStream();

                        byte[] buffer = new byte[8192]; // Такого вот размера буфер
                        // Далее, например, вот так читаем ответ
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        data = baos.toByteArray();
                        resultString = new String(data, "UTF-8");
                    } else {
                    }



                } catch (MalformedURLException e) {

                    resultString = "MalformedURLException:" + e.getMessage();
                } catch (IOException e) {

                    resultString = "IOException:" + e.getMessage();
                } catch (Exception e) {

                    resultString = "Exception:" + e.getMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            String answer = null;
            SharedPreferences sp = getSharedPreferences("my_settings",
                    Context.MODE_PRIVATE);
            try {
                JSONObject response = new JSONObject(resultString);
                JSONObject notice = response.getJSONObject("notice");
                answer = notice.getString("answer");
            }
            catch(Exception e) {}

            if (answer.equals("Error username or password"))
            {
                Toast.makeText(SignIn.this, "Error username or password", Toast.LENGTH_SHORT).show();
            }
            else if(answer.equals("User is active"))
            {
                startActivity(new Intent(getApplicationContext(), StartScreenActivity.class));
            }
            else
            {
                SharedPreferences.Editor e = sp.edit();
                e.putInt("token", Integer.valueOf(answer));
                e.commit();

                startActivity(new Intent(getApplicationContext(), StartScreenActivity.class));

            }


        }
    }

}