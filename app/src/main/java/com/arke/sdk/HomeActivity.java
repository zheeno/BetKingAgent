package com.arke.sdk;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import com.arke.sdk.Collection;
import com.arke.sdk.util.StringUtils;

import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private String revenue_type;
    private String collector_type;

    private EditText rev_amt;
    private EditText col_ref_id;
    private EditText col_name;
    private Button proc_btn;
    private int PAYMENT_ID = 37748;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rev_amt = (EditText) findViewById(R.id.rev_amt);
//        col_ref_id = (EditText) findViewById(R.id.col_ref_id);
//        col_name = (EditText) findViewById(R.id.col_name);
        proc_btn = (Button) findViewById(R.id.proc_btn);

        // Creating adapter for revenue type spinner
        Spinner rev_type_spinner = (Spinner) findViewById(R.id.rev_type_spinner);
        // Spinner click listener
        rev_type_spinner.setOnItemSelectedListener(this);
        List<String> revenueTypes = new ArrayList<String>();
        revenueTypes.add("Levy");
        revenueTypes.add("Rent");
        revenueTypes.add("Security");
        revenueTypes.add("Sanitation");
        revenueTypes.add("Others");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, revenueTypes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rev_type_spinner.setAdapter(dataAdapter);



        // Creating adapter for collector type spinner
        Spinner col_type_spinner = (Spinner) findViewById(R.id.col_type_spinner);
        // Spinner click listener
        col_type_spinner.setOnItemSelectedListener(this);
        List<String> colTypes = new ArrayList<String>();
        colTypes.add("Staff");
        colTypes.add("Individual");
        ArrayAdapter<String> colDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colTypes);
        colDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        col_type_spinner.setAdapter(colDataAdapter);

        // print receipt
        proc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!rev_amt.getText().toString().equals("")) {
                    if(Double.parseDouble(rev_amt.getText().toString()) > 0) {
//                        Collection collection = new com.arke.sdk.Collection(HomeActivity.this);
//                        collection.setAmount(rev_amt.getText().toString());
//                        collection.setCollectorName("Ayodeji Odesola");
//                        collection.setCollectorRef("6789098763");
//                        collection.setCollectorType(collector_type);
//                        collection.setRevType(revenue_type);
//                        collection.setTranRef("8920039728922");
//                        collection.printSlip();
                        try {
                            Intent intent = new Intent("com.efulltech.efupay.efucard.payment");
                            JSONObject object = new JSONObject();
                            object.put("secret_key", "EFU-SEC-4be682719ffb1411eebc1f1a750ae739");
                            object.put("public_key", "EFU-PUB-f151f9d41243eba87f508283c02d7276");
                            object.put("package_name", getPackageName());
                            object.put("app_name", getString(R.string.app_name));
                            object.put("description", "Test Transaction");
                            object.put("transaction_ref", "");
                            object.put("amount", Double.parseDouble(rev_amt.getText().toString()) * 100);
                            String data = StringUtils.bytesToHex(object.toString().getBytes());
                            intent.putExtra("data", data);
                            startActivityForResult(intent, PAYMENT_ID);
//                            rev_amt.setText(null);
                        }catch (Exception error){
                            error.printStackTrace();
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String value = parent.getItemAtPosition(position).toString();

        switch (parent.getId()){
            case R.id.rev_type_spinner:
                this.revenue_type = value;
                break;
            case R.id.col_type_spinner:
                this.collector_type = value;
                break;
        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onBackPressed(){
            super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_ID) {
            if (resultCode == Activity.RESULT_OK) {
                String response = (String) data.getSerializableExtra("response");
                Log.d("PAYMENT RES", response);
            }
        }
    }
}
