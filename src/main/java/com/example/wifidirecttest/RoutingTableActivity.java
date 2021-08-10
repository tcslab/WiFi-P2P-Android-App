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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

import static com.example.wifidirecttest.MainActivity.TAG;
import static com.example.wifidirecttest.MainActivity.TAG2;
import static com.example.wifidirecttest.MainActivity.TAG3;
import static com.example.wifidirecttest.MainActivity.TAG4;

public class RoutingTableActivity extends AppCompatActivity {

    private RoutingTableRecyclerViewAdapter routingTableRecyclerViewAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG4,"ROUTING TABLE ONCREATE");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.routing_table_activity);
        recyclerView = findViewById(R.id.routingTableView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        routingTableRecyclerViewAdapter = new RoutingTableRecyclerViewAdapter(this, com.example.wifidirecttest.MainActivity.routingTable);
        recyclerView.setAdapter(routingTableRecyclerViewAdapter);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.routing_table_menu, menu);
            return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.refresh_routing_table:
                routingTableRecyclerViewAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(TAG4,"ROUTING TABLE ONRESUME");
        routingTableRecyclerViewAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG4,"ROUTING TABLE ONSTOP");
        super.onStop();
    }

    public class RoutingTableRecyclerViewAdapter extends RecyclerView.Adapter<RoutingTableRecyclerViewAdapter.ViewHolder> {

        private HashMap<String, RouteInfoTuple> mData;
        private LayoutInflater mInflater;

        // data is passed into the constructor
        RoutingTableRecyclerViewAdapter(Context context, HashMap<String, RouteInfoTuple> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.routing_table_row, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Object[] keys = mData.keySet().toArray();
            StringBuilder builder = new StringBuilder();
            Log.d(TAG2, "iterating for position " + position);
            String node_key = (String) keys[position];
            Log.d(TAG2, "NODE KEY is " + node_key);
            RouteInfoTuple routeInfoTuple = mData.get(keys[position]);     //TODO HARDCODED MAPPING FOR EASIER DISPLAY
            String node = node_key;
            String nextHop = routeInfoTuple.nextHop;
            if (node_key.startsWith("A") && node_key.endsWith("      ")) {
                node = "A";
            } /*else if (node_key.startsWith("stef")) {
            node = "B";
        } else if (node_key.startsWith("Moto")) {
            node = "C";
        }*/
            if (node_key.startsWith("A") && node_key.endsWith("      ")) {
                nextHop = "A";
            } /* else if (routeInfoTuple.nextHop.startsWith("stef")){
            nextHop = "B";
        } else if (routeInfoTuple.nextHop.startsWith("Moto")) {
            nextHop = "C";
        } */
            builder.append("       ");
            builder.append(node);
            builder.append("        |       ");
            builder.append(nextHop);
            builder.append("        |       ");
            builder.append(String.valueOf(routeInfoTuple.numberOfHops));
            Log.d(TAG2, "string built is " + builder.toString());
            holder.myTextView.setText(builder.toString());
        }

        // total number of rows
        @Override
        public int getItemCount() {
            //Log.d(TAG3,"size of routing table is: "+ mData.size());
            return mData.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView myTextView;
            Button myButton;

            ViewHolder(View itemView) {
                super(itemView);
                Log.d(TAG2, "View is created!");
                myTextView = itemView.findViewById(R.id.sequence);
            }

            @Override
            public void onClick(View view) {
            }
        }
    }
}