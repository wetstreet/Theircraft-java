package com.chenyirun.theircraft.activity.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.chenyirun.theircraft.activity.DetailActivity;
import com.chenyirun.theircraft.database.DBService;
import com.chenyirun.theircraft.R;
import com.chenyirun.theircraft.model.SaveAndConfig;
import com.chenyirun.theircraft.activity.LoadingActivity;
import com.chenyirun.theircraft.activity.NewActivity;

import static android.app.Activity.RESULT_CANCELED;

public class SingleFragment extends Fragment {

    private static final String TAG = "SingleFragment";
    private ListView listView;
    private int id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saves_ui, container, false);

        listView = (ListView)view.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
                id = (int)arg3;

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(SaveAndConfig.ID, id);
                startActivity(intent);
            }
        });

        DBService.setContext(getActivity());
        UpdateList();

        return view;
    }

    public void UpdateList(){
        Cursor cursor = DBService.getInstance().pageCursorQuery();
        String from[] = { "_id", "name" };
        int to[] = { R.id.textView_list_id, R.id.textView_list_name };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_layout, cursor, from, to);
        listView.setAdapter(adapter);
    }

    @Override
    public void onResume(){
        UpdateList();
        super.onResume();
    }
}
