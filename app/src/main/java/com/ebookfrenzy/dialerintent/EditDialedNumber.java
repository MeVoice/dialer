package com.ebookfrenzy.dialerintent;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditDialedNumber extends AppCompatActivity {
    private static final String TAG = "EditCalledNumber";
    private CharSequence phoneNumber;
    private Button button;
    private EditText edittext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_called_number);

        Intent intent = getIntent();
        Uri data = intent.getData();
        phoneNumber = data.getSchemeSpecificPart();
        Log.i(TAG, data.getScheme());
        Log.i(TAG, data.getSchemeSpecificPart());
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener( new Button.OnClickListener(){
            public void onClick(View v){
                handleButtonClick(v);
            }
        });
        edittext = (EditText) findViewById(R.id.editText);
        edittext.setText(phoneNumber);
            /*
        URL url = null;
        try {
            url = new URL(data.getScheme(), data.getHost(), data.getPath());
        } catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+1234567890"));
        startActivity(intent);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.fromParts("tel", "#123456#", "#"));
        startActivity(intent);
        */
    }

    private void handleButtonClick(View v){
        String updatedPhoneNumber = edittext.getText().toString();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.fromParts("tel", updatedPhoneNumber, "#"));
        startActivity(intent);
        finish();
    }
}
