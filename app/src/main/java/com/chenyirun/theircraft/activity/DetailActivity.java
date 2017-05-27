package com.chenyirun.theircraft.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.database.DBService;
import com.chenyirun.theircraft.model.Point3Int;
import com.chenyirun.theircraft.model.SaveAndConfig;

public class DetailActivity extends TitleActivity {

    private static SaveAndConfig save;

    private TextView textView_id;
    private TextView textView_name;
    private TextView textView_seed;
    private TextView textView_time;
    private Button button_start;
    private Button button_delete;

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("Save Detail");
        showBackwardView(R.string.button_back, true);
        showForwardView(R.string.button_new, false);

        Intent intent = getIntent();
        id = intent.getIntExtra(SaveAndConfig.ID, 1);
        save = DBService.getInstance().getSave(id);

        button_delete = (Button)findViewById(R.id.button_delete);
        button_delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                DBService.getInstance().removeSave(id);
                finish();
            }
        });

        button_start = (Button)findViewById(R.id.button_start);
        button_start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (id == 0){
                    Toast.makeText(getApplication(), "No save is selected!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(DetailActivity.this, LoadingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(SaveAndConfig.ID, save.id);
                intent.putExtra(SaveAndConfig.SEED, save.seed);
                intent.putExtra(SaveAndConfig.STEVE_X, save.steveBlock.x);
                intent.putExtra(SaveAndConfig.STEVE_Y, save.steveBlock.y);
                intent.putExtra(SaveAndConfig.STEVE_Z, save.steveBlock.z);
                startActivity(intent);
            }
        });

        textView_id = (TextView) findViewById(R.id.textView_id);
        textView_id.setText(save.id + "");

        textView_name = (TextView) findViewById(R.id.textView_name);
        textView_name.setText(save.name);

        textView_seed = (TextView) findViewById(R.id.textView_seed);
        textView_seed.setText(save.seed + "");

        textView_time = (TextView) findViewById(R.id.textView_create_time);
        textView_time.setText(save.date);
    }
}
