package com.procasy.dubarah_nocker.Activity.SignUpActivities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.procasy.dubarah_nocker.R;
import com.shawnlin.preferencesmanager.PreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainInfoSignUp extends AppCompatActivity implements Validator.ValidationListener{

    LinearLayout next_btn;
    @NotEmpty
    EditText FirstName , Lastname , Country , City , Region , Phonenumber;
Validator validator;
    TextView birthdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info_sign_up);
        next_btn = (LinearLayout) findViewById(R.id.next_btn);
        FirstName = (EditText) findViewById(R.id.Firstname);
        Lastname = (EditText) findViewById(R.id.Lastname);
        Country = (EditText) findViewById(R.id.Country);
        City = (EditText) findViewById(R.id.City);
        Region = (EditText) findViewById(R.id.Province);
        birthdate = (TextView)findViewById(R.id.BirthDate);
        Phonenumber = (EditText)findViewById(R.id.PhoneNumber);

        validator = new Validator(this);
        validator.setValidationListener(this);
        new PreferencesManager(this)
                .setName("user")
                .init();
        birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainInfoSignUp.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        int month = monthOfYear + 1;
                        birthdate.setText(year+"-"+month+"-"+dayOfMonth);
                    }
                }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              validator.validate();
            }
        });
        GetInfoRequest();

    }


    public void GetInfoRequest() {
        final ProgressDialog pDialog = new ProgressDialog(MainInfoSignUp.this);
        pDialog.setMessage("Getting Location Info");
        pDialog.show();
        RequestQueue queue = Volley.newRequestQueue(MainInfoSignUp.this);
        String url = "http://ip-api.com/json";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            JSONObject object = new JSONObject(response);
                            String status = object.optString("status");
                            if(status.equals("success"))
                            {
                                Country.setText(object.getString("country"));
                                City.setText(object.getString("city"));
                                Region.setText(object.getString("regionName"));
                            }
                            else
                            {

                            }
                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        pDialog.hide();

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR","error => "+error.toString());
                        pDialog.hide();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
        };
        queue.add(postRequest);

    }

    @Override
    public void onValidationSucceeded() {
        PreferencesManager.putString("firstName",FirstName.getText().toString());
        PreferencesManager.putString("lastName",Lastname.getText().toString());
        PreferencesManager.putString("country",Country.getText().toString());
        PreferencesManager.putString("city",City.getText().toString());
        PreferencesManager.putString("region",Region.getText().toString());
        PreferencesManager.putString("birthDate",birthdate.getText().toString());
        PreferencesManager.putString("phoneNumber",Phonenumber.getText().toString());

        startActivity(new Intent(getApplicationContext(), LocationInfoSignUp.class));

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}

