package com.example.wifidirecttest;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import static com.example.wifidirecttest.MainActivity.TAG;
import static com.example.wifidirecttest.MainActivity.TAG3;
import static com.example.wifidirecttest.MainActivity.TAG4;
import static com.example.wifidirecttest.MainActivity.taskList;
import static com.example.wifidirecttest.MainActivity.thisDeviceName;

public class TaskOverviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    TaskOverviewRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_overview_activity);
        recyclerView = findViewById(R.id.taskList);
        adapter = new TaskOverviewRecyclerViewAdapter(getApplicationContext(),taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.taskToolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Log.d(TAG4,"IN TaskOverview Tasklist has " + taskList.size() + " tasks");
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_overview_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.refresh_tasks:
                adapter.notifyDataSetChanged();
                onRestart();
            case R.id.home:
                Log.d(TAG3, "BACK BUTTON PRESSED!");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    public class TaskOverviewRecyclerViewAdapter  extends RecyclerView.Adapter<TaskOverviewRecyclerViewAdapter.ViewHolder> {

        private ArrayList<CrowdTask> mData;
        private LayoutInflater mInflater;

        // data is passed into the constructor
        TaskOverviewRecyclerViewAdapter(Context context, ArrayList<CrowdTask> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.task_row, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.d(TAG3, "iterating for position " + position);
            CrowdTask task = mData.get(position);
            StringBuilder builder = new StringBuilder();
            builder.append(position+1);
            if (thisDeviceName.startsWith("B")){
                builder.append("\t\t");
            } else {
                builder.append("\t\t\t\t");
            }
            builder.append(task.getSourceNode().substring(0,1));
            if (thisDeviceName.startsWith("B")){
                builder.append("\t\t\t");
            } else {
                builder.append("\t\t\t\t\t");
            }
            for (String node : task.getPiggyPath()){
                builder.append(node);
            }
            if (thisDeviceName.startsWith("B")){
                builder.append("\t\t");
            } else {
                builder.append("\t\t\t\t");
            }
            String temp=null;
            switch(task.getStatus()){
                case 0:
                    temp="READY";
                    break;
                case 1:
                    temp="RUNNING";
                    break;
                case 2:
                    temp="COMPLETE";
                    break;
                case 3:
                    temp="ON HOLD";
                    break;
                case -1:
                    temp="FAILED";
            };
            builder.append(temp);
            builder.append("\t\t\t");
            holder.myTextView.setText(builder.toString());
            holder.myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //onBackPressed();
                    task.run();
                    notifyDataSetChanged();
                }
            });
        }


        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView myTextView;
            Button myButton;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.taskInfo);
                myButton = itemView.findViewById(R.id.startTaskButton);
            }

            @Override
            public void onClick(View view) {
            }
        }
    }
}
