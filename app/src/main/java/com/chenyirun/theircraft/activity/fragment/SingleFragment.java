package com.chenyirun.theircraft.activity.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.chenyirun.theircraft.database.DBService;
import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.model.SaveAndConfig;
import com.chenyirun.theircraft.activity.LoadingActivity;
import com.chenyirun.theircraft.activity.NewActivity;

import static android.app.Activity.RESULT_CANCELED;

public class SingleFragment extends Fragment {
    private Button button_remove;
    private Button button_start;
    private Button button_new;
    private ListView listView;

    private int id;

    private DBService dbService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saves_ui, container, false);

        listView = (ListView)view.findViewById(R.id.listView);
        button_remove = (Button)view.findViewById(R.id.button_remove);
        button_start = (Button)view.findViewById(R.id.button_start);
        button_new = (Button)view.findViewById(R.id.button_new);
        button_remove.setOnClickListener(removeListener);
        button_start.setOnClickListener(startListener);
        button_new.setOnClickListener(newListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
                id = (int)arg3;
            }
        });

        DBService.setContext(getActivity());
        dbService = DBService.getInstance();
        UpdateList();

        return view;
    }

    private void UpdateList(){
        Cursor cursor = dbService.pageCursorQuery();
        String from[] = { "_id", "name", "seed", "date" };
        int to[] = { R.id.textView_list_id, R.id.textView_list_name, R.id.textView_list_seed, R.id.textView_list_date };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_layout, cursor, from, to);
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
                Toast.makeText(getActivity(), "No save is selected!", Toast.LENGTH_SHORT).show();
                return;
            }
            SaveAndConfig save = dbService.getSave(id);
            Intent intent = new Intent(getActivity(), LoadingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            Intent intent = new Intent(getActivity(), NewActivity.class);
            startActivityForResult(intent, 0);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (resultCode == RESULT_CANCELED){
            return;
        }
        UpdateList();
    }
}
