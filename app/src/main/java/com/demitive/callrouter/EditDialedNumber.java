package com.demitive.callrouter;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.demitive.callrouter.R;

public class EditDialedNumber extends AppCompatActivity {
    private CharSequence phoneNumber;
    private ImageButton button;
    private EditText edittext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_called_number);

        Intent intent = getIntent();
        Uri data = intent.getData();
        phoneNumber = data.getSchemeSpecificPart();
        button = (ImageButton) findViewById(R.id.edit_number_button);
        button.setOnClickListener( new ImageButton.OnClickListener(){
            public void onClick(View v){
                handleButtonClick(v);
            }
        });
        edittext = (EditText) findViewById(R.id.editText);
        edittext.setText(phoneNumber);
    }

    private void handleButtonClick(View v){
        String updatedPhoneNumber = edittext.getText().toString();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.fromParts("tel", updatedPhoneNumber, "#"));
        startActivity(intent);
        finish();
    }
}
