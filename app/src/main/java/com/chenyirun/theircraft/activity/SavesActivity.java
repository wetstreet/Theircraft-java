package com.chenyirun.theircraft.activity;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.chenyirun.theircraft.DBService;
import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.SaveAndConfig;

public class SavesActivity extends AppCompatActivity {
    private static final String TAG = "SavesActivity";
    private Button button_remove;
    private Button button_start;
    private Button button_new;
    private ListView listView;

    private int id;

    private DBService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saves_ui);
        listView = (ListView)findViewById(R.id.listView);

        button_remove = (Button)findViewById(R.id.button_remove);
        button_start = (Button)findViewById(R.id.button_start);
        button_new = (Button)findViewById(R.id.button_new);
        button_remove.setOnClickListener(removeListener);
        button_start.setOnClickListener(startListener);
        button_new.setOnClickListener(newListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
                id = (int)arg3;
                Log.i(TAG, "onItemClick: id="+id);
            }
        });

        DBService.setContext(getApplicationContext());
        dbService = DBService.getInstance();
        UpdateList();
    }

    private void UpdateList(){
        Cursor cursor = dbService.pageCursorQuery();
        String from[] = { "_id", "name", "seed", "date" };
        int to[] = { R.id.textView_list_id, R.id.textView_list_name, R.id.textView_list_seed, R.id.textView_list_date };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_layout, cursor, from, to);
        listView.setAdapter(adapter);
    }

    private View.OnClickListener removeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dbService.removeSave(id);
            UpdateList();
        }
    };

    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (id == 0){
                Toast.makeText(getApplicationContext(), "No save is selected!", Toast.LENGTH_SHORT).show();
                return;
            }
            SaveAndConfig save = dbService.getSave(id);
            Intent intent = new Intent(SavesActivity.this, MainActivity.class);
            intent.putExtra(ConfigureActivity.CHUNK_RADIUS, ConfigureActivity.chunk_radius);
            intent.putExtra(SaveAndConfig.ID, save.id);
            intent.putExtra(SaveAndConfig.SEED, save.seed);
            intent.putExtra(SaveAndConfig.STEVE_X, save.steveBlock.x);
            intent.putExtra(SaveAndConfig.STEVE_Y, save.steveBlock.y);
            intent.putExtra(SaveAndConfig.STEVE_Z, save.steveBlock.z);
            startActivity(intent);
        }
    };

    private View.OnClickListener newListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SavesActivity.this, NewActivity.class);
            startActivityForResult(intent, 0);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (resultCode == RESULT_CANCELED){
            return;
        }
        UpdateList();
    }
}
