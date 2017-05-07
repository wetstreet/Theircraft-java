package com.chenyirun.theircraft;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_ui);
    }

    public void startGame(View view){
        Button button = (Button)findViewById(R.id.button);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
