package com.chenyirun.theircraft.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chenyirun.theircraft.R;

public class NewActivity extends AppCompatActivity {
    public static final String ACTIVITY_NAME = "NewActivity";
    private Button button_create;
    private EditText editText_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_ui);
        button_create = (Button)findViewById(R.id.button_create);
        button_create.setOnClickListener(configListener);
        editText_name = (EditText)findViewById(R.id.editText_name);
    }

    private View.OnClickListener configListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(NewActivity.this, ConfigureActivity.class);
            intent.putExtra(ConfigureActivity.SAVE_NAME, editText_name.getText());
            intent.putExtra(ConfigureActivity.ACTIVITY_NAME_KEY, ACTIVITY_NAME);
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}
