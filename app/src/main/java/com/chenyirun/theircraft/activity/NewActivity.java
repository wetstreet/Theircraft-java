package com.chenyirun.theircraft.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chenyirun.theircraft.database.DBService;
import com.chenyirun.theircraft.R;

import java.text.SimpleDateFormat;
import java.util.Random;

public class NewActivity extends AppCompatActivity {
    private Button button_create;
    private EditText editText_name;
    private EditText editText_seed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_ui);
        button_create = (Button)findViewById(R.id.button_create);
        button_create.setOnClickListener(configListener);
        editText_name = (EditText)findViewById(R.id.editText_name);
        editText_seed = (EditText)findViewById(R.id.editText_seed);
        int seed = new Random().nextInt();
        editText_seed.setText("" + seed, TextView.BufferType.EDITABLE);
    }

    private View.OnClickListener configListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createSave();
            setResult(RESULT_OK, null);
            finish();
        }
    };

    private void createSave(){
        String name = editText_name.getText().toString();
        int seed = Integer.parseInt(editText_seed.getText().toString());
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        DBService.getInstance().addSave(name, seed, date);
    }
}
