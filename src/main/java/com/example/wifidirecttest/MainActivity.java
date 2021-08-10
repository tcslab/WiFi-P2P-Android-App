package com.example.wifidirecttest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static com.example.wifidirecttest.CrowdTask.TASK_COMPLETE;
import static com.example.wifidirecttest.CrowdTask.TASK_FAILED;
import static com.example.wifidirecttest.CrowdTask.TASK_RUNNING;
import static java.text.DateFormat.getDateTimeInstance;


public class MainActivity extends AppCompatActivity {
    public static final int SENT_FAILURE = 0;
    public static final int SENT_SUCCESS = 1;
    public static final int REQUEST = 1;
    public static final int RESPONSE = 2;
    public static final int COLLECT = 3;
    static final int MESSAGE_READ = 1;
    static final int MESSAGE_SENT = 2;
    static final int MAX_TRIES = 4;
    public static final String TAG = "LVL1";
    public static final String TAG2 = "LVL2";
    public static final String TAG3 = "LVL3";
    public static final String TAG4 = "LVL4";
    private static final long CONNECTION_TIMEOUT_LIMIT = 60*1000;  //Time margin in msec for connection listener, which is called once connectToPeer function is successful
    private static final long RETRY_SEND_INTERVAL = 60*1000;
    private static final int CHAIN_TOPOLOGY = 1;
    private static final int STAR_TOPOLOGY = 2;
    private static final long CONNECTION_LISTENER_TIMEOUT_LIMIT = 30*1000;
    private static final int MAX_BROADCASTING_TRIES = 3;
    private String requestStartTime;
    private int newtworkTopology;
    private int taskType;

    private static Context context;
    private MyMessage receivedMessage;
    public  MyMessage messageToSend;
    public boolean connectedToPeer = false;
    public static String thisDeviceName = " ";
    private Button btnReset, btnCollect, btnRequest;
    public ImageView discoveryStatusIndicator;
    public ListView listView;

    public TextView read_msg_box, connectionStatus, discoveryStatus;
  //  public EditText writeMsg;
    public static WifiManager wifiManager;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    public List<WifiP2pDevice> peers;
    public String[] peerDeviceNames;
    public WifiP2pDevice[] deviceArray;
    public static ArrayList<CrowdTask> taskList;

    public static HashMap<String, RouteInfoTuple> routingTable;
    //AppCompatActivity THIS = this;

    private boolean isWifiP2pEnabled = false;
    private boolean IamSender = false;
    public boolean WhileSending = false;
    public boolean discoveryActive = false;
    public boolean discoveryEnabled = false;
    private long postResponseStartTime;
    private boolean postResponseTimeout =false;
    private boolean retryConnection;


    private String peerDeviceConnected = " ";
    private String lastSender;
    private int brcstTry=0;
    private static MessageTaskQueue messageTaskQueue;
    private Thread sendTaskQueueThread;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (peerList.getDeviceList().isEmpty()) {
                Log.d(TAG3, "THE LIST IS EMPTY - ALLOW LOCATION PERMISSION");
            }
            ArrayList<WifiP2pDevice> peerList2 = new ArrayList<>(peerList.getDeviceList());
            //TODO HARDCODED TO REMOVE C AS NEIGHBOR FROM A  (TO CHANGE BACK HAVE TO REPLACE PeerList2 to PeerList.getDeviceList() )
            ArrayList<WifiP2pDevice> nodesToRemove = new ArrayList<>();
            switch (newtworkTopology){
                case CHAIN_TOPOLOGY:
                    for (WifiP2pDevice node : peerList2) {
                        if ((!node.deviceName.startsWith("A")) && node.deviceName.length() > 2) {
                            nodesToRemove.add(node);
                        } else {
                            if (thisDeviceName.startsWith("A")) {
                                //Log.d(TAG3, "MY NAME IS A checking for" + node.deviceName);
                                if (node.deviceName.startsWith("C") || node.deviceName.startsWith("D")) {
                                    nodesToRemove.add(node);
                                }
                            }
                            if (thisDeviceName.startsWith("B")) {
                                if (node.deviceName.startsWith("D")) {
                                    nodesToRemove.add(node);
                                }
                            }
                            if (thisDeviceName.startsWith("C")) {
                                //Log.d(TAG3, "MY NAME IS C checking for" + node.deviceName);
                                if (node.deviceName.startsWith("A")) {
                                    nodesToRemove.add(node);
                                }
                            }
                            if (thisDeviceName.startsWith("D")) {
                                if (node.deviceName.startsWith("A") || node.deviceName.startsWith("B")) {
                                    nodesToRemove.add(node);
                                }
                            }
                        }
                    }
                    break;
                case STAR_TOPOLOGY:
                    for (WifiP2pDevice node : peerList2){
                        if (thisDeviceName.startsWith("A")) {
                            if (node.deviceName.startsWith("C") || node.deviceName.startsWith("D")) {
                                nodesToRemove.add(node);
                            }
                        }
                        if (thisDeviceName.startsWith("C")) {
                            if (node.deviceName.startsWith("A") || node.deviceName.startsWith("D")) {
                                nodesToRemove.add(node);
                            }
                        }
                        if (thisDeviceName.startsWith("D")) {
                            if (node.deviceName.startsWith("A") || node.deviceName.startsWith("C")) {
                                nodesToRemove.add(node);
                            }
                        }
                    }
                    break;
            }
            /*Log.d("PEERS","peers before removal: ");
            for (WifiP2pDevice device : peerList2) {
                Log.d("PEERS", device.deviceName);
            }*/

            peerList2.removeAll(nodesToRemove);
            /*Log.d("PEERS","peers after removal: ");
            for (WifiP2pDevice device : peerList2) {
                Log.d("PEERS", device.deviceName);
            }*/
            if (!peerList2.equals(peers)) {
                peers.clear();
                peers.addAll(peerList2);
                updateRoutingTable(peerList2);
                peerDeviceNames = new String[peerList2.size()];
                deviceArray = new WifiP2pDevice[peerList2.size()];
                int index = 0;

                for (WifiP2pDevice device : peerList2) {
                    peerDeviceNames[index] = device.deviceName.substring(0,1);
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, peerDeviceNames);
                listView.setAdapter(adapter);
            }
        }
    };


    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            Log.d(TAG, "CONNECTION LISTENER CALLED");
            WhileSending = true;
            if (wifiP2pInfo.groupFormed) {
                connectedToPeer = true;
                if (wifiP2pInfo.isGroupOwner) {
                    connectionStatus.setText("GO");
                    Log.d(TAG3, "Before server start");
                    serverClass = new ServerClass();
                    serverClass.start();
                    try {
                        serverClass.join();
                    } catch (InterruptedException e) {
                        Log.d(TAG3, e.toString());
                        e.printStackTrace();
                    }
                    Log.d(TAG3, "After server start");
                } else {
                    connectionStatus.setText("GM");
                    Log.d(TAG3, "Before client start");
                    clientClass = new ClientClass(groupOwnerAddress);
                    clientClass.start();
                    try {
                        clientClass.join();
                    } catch (InterruptedException e) {
                        Log.d(TAG3, e.toString());
                        e.printStackTrace();
                    }
                    Log.d(TAG3, "After client start");
                }
                if (sendReceive != null) { //if sendReceive thread started correctly do corresponding sender or receiver actions
                    sendReceive.start();
                    if (IamSender) {
                        //Log.d(TAG2, "message id is: " + messageToSend.getId() + " + with dest: " + messageToSend.getDest());
                        sendReceive.write(messageToSend);
                    }
                } else { //if sendReceive is null restart process
                    if (IamSender) {
                        Log.d(TAG, "Group not formed properly. Restarting sendTask");
                        messageTaskQueue.sendTaskComplete(SENT_FAILURE);
                    } else {  //in the case of the client a simple reset suffices, no send task ongoing
                        resetStatus();
                        Log.d(TAG, "Group not formed properly. Resetting..");
                        Log.d(TAG2,"RESET FROM 212");
                    }
                }
            } else {
                if (IamSender) {
                    Log.d(TAG, "Group not formed properly. Restarting sendTask");
                    messageTaskQueue.sendTaskComplete(SENT_FAILURE);
                } else {
                    Log.d(TAG, "Group not formed properly. Resetting status..");
                    resetStatus();
                }

            }
        }
    };

    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    MyMessage receivedMsg = (MyMessage) msg.obj;
                    Log.d(TAG, "MESSAGE RECEIVED");
                    parseMessage(receivedMsg);
                    break;

                case MESSAGE_SENT:
                    Log.d(TAG, "MESSAGE SENT");
                    messageTaskQueue.sendTaskComplete(SENT_SUCCESS);
                    break;
            }
            return true;
        }
    });



    public static void finishTask(int result) {
        for (CrowdTask task : taskList){
            if (task.getStatus()==TASK_RUNNING){
                task.setStatus(result);
                break;
            }
        }
    }

    private void askDeviceName(boolean askUnique, String name) {
        //Pops up dialog to ask for device name from user
        if (!askUnique) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter your user name:");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setCancelable(true);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        askDeviceName(true, input.getText().toString());
                }
            });
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Do you want to attach a unique id to it?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setDeviceName(name, false, true);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setDeviceName(name, false, false);
                }
            });
            builder.show();
        }
    }

    public void setDeviceName(String name, boolean firstTime, boolean unique) {
        //Sets a unique name for the device, by adding to the given input string the Android Unique Device ID
        if (firstTime) {
            thisDeviceName = name;
        } else {
            String assignedName;
            try {
                if (unique) {
                assignedName = name + "-" + Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                ;} else {
                    assignedName = name;
                }
                Method m = mManager.getClass().getMethod(
                        "setDeviceName",
                        new Class[]{WifiP2pManager.Channel.class, String.class,
                                WifiP2pManager.ActionListener.class});

                m.invoke(mManager, mChannel, assignedName, new WifiP2pManager.ActionListener() {
                    public void onSuccess() {
                        Log.d(TAG2, "Device name changed to " + assignedName);
                    }

                    public void onFailure(int reason) {
                        Log.d(TAG2, "Device name change failed");
                    }
                });
                thisDeviceName = assignedName;
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private void initialize() {
        //Initialization of UI resources
        Log.d(TAG3, "running in thread: " + Thread.currentThread().getName());
        if (thisDeviceName.equals(" ")){
            setDeviceName("D", true, false);
            Log.d(TAG3, "DEVICE D NAME SET");
        }
        btnReset = findViewById(R.id.reset);
        btnCollect = findViewById(R.id.collectButton);
        btnRequest = findViewById(R.id.floodButton);
        listView = findViewById(R.id.peerListView);
        read_msg_box = findViewById(R.id.readMsg);
        discoveryStatus = findViewById(R.id.discoveryStatusText);
        connectionStatus = findViewById(R.id.connectionStatus);
       // writeMsg = findViewById(R.id.writeMsg);
        discoveryStatusIndicator = findViewById(R.id.discoveryStatusIndicator);

        //initialization of networking managers and broadcast receiver intents to listen to, as well as the send task queue
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        //initialization of lists and queues
        peers = new ArrayList<>();
        routingTable = new HashMap<String, RouteInfoTuple>();
        messageTaskQueue = new MessageTaskQueue(this, handler);
        taskList = new ArrayList<>();


        /* TEST FEED FOR RT LIST VIEW
        routingSequence.add("A");
        routingSequence.add("C");
        LinkedList<String> routingSequence2 = new LinkedList<>();
        routingSequence2.add("A");
        routingSequence2.add("B");
        routingSequence2.add("C");
        routeCollection.add(routingSequence);
        routeCollection.add(routingSequence2);
         */

        //EXPERIMENT PARAMETERS
        newtworkTopology= CHAIN_TOPOLOGY;
        taskType = TaskExecutable.SENSING_TASK;

        //Initialization of listeners and reset of app status
        startListeners();
        startDiscovery();
        resetStatus();

        //Start a thread to check if peerdiscovery is active
        Thread WakeUpCheckThread = new Thread(new WakeUp());
        WakeUpCheckThread.start();


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"Permission is granted");
                //File write logic here
            } else {
                Log.d(TAG,"Permission NOT NOT granted");
            }
        }*/
    }

    private void startListeners() {
        //set the button listeners
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hardReset();
                //startDiscovery();
            }
        });
        btnCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dest = "COLLECT"; //TODO HARDCODED FOR NOW
                //Log.d(TAG, "initiating COLLECT with dest " + dest + " and dummy file");
                //CrowdTask responseTask = new CrowdTask(thisDeviceName, RESPONSE, "Hello from " + thisDeviceName);
                //Log.d(TAG, "Collect task created");
                //Log.d(TAG2, "task id: " + responseTask.getId());
                prepMessageAndAddToQueue(dest, COLLECT, null);   //prepare and add a message to queue for dest with type 1 (RESPONSE)
                messageTaskQueue.run();
                requestStartTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                Log.d("METRICS", "COLLECT START TIME: " + requestStartTime);
            }
        });
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Initiating request..");
                CrowdTask requestTask = new CrowdTask(thisDeviceName, taskType);
                Log.d(TAG2,"task id:" + requestTask.getId());
                prepMessageAndAddToQueue("FLOOD", REQUEST, requestTask);   //prepare and add a message to queue with type 2 (REQUEST)
                messageTaskQueue.run();
                requestStartTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                Log.d("METRICS", "REQUEST START TIME: " + requestStartTime);
            }
        });
    }


    public void prepMessageAndAddToQueue(String dest, int type, CrowdTask task) {
        Log.d(TAG2, "Preparing new message task");
        //Gets called when new message is created. Prepares all message fields and then adds to queue.
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        String macAdress = wifiManager.getConnectionInfo().getMacAddress();
        String id = macAdress + ":" + timestamp;  //Unique message id created with MAC address and current timestamp
        LinkedList<String> piggyBackedRoute = new LinkedList<String>();  //Piggy path is initialised with this device name
        piggyBackedRoute.add(thisDeviceName.substring(0,1));
        Log.d(TAG2, "initial piggy route is " + piggyBackedRoute.toString());
        MyMessage msg = new MyMessage(id, thisDeviceName, dest, type, task, piggyBackedRoute);

        if (type == REQUEST || type == COLLECT) {  //If it is a request (flood) forward to all neighboring nodes
            Log.d(TAG2, "Task is request/collect, will broadcast..");
            broadcast(msg);
        } else if (type == RESPONSE) { //If it is a response calculate the next node to transmit to
            msg.setPiggyBackedRoute(task.getPiggyPath()); //put the task piggyPath to the response message
            Log.d(TAG2, "Task is response, looking if destination in neighbors");
            if (!((peerDeviceNames==null) || (peerDeviceNames.length==0))) {
                boolean destFound = false;
                for (String neighbour : peerDeviceNames) {
                    if (neighbour.equals(dest)) {
                        messageTaskQueue.addTask(new MessageTask(msg, neighbour));
                        Log.d(TAG4, "ADD TASK 467");
                        destFound=true;
                        break;
                    }
                }
                if (!destFound) {
                    Log.d(TAG2, "dest " + dest + " not in neighbors, broadcasting");
                    broadcast(msg);
                }
            } else
            {
                Log.d(TAG, "Peerlist is empty, resetting and retrying. Please wait..");
                resetStatus();
                Log.d(TAG2,"RESET FROM 439");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        prepMessageAndAddToQueue(msg);
                    }
                }, 3000);
            }
        }
    }

    private void prepMessageAndAddToQueue(MyMessage msg) {
        Log.d(TAG2, "Retrying message task preparation");
        if (msg.getType() == REQUEST) {  //If it is a request (flood) forward to all neighboring nodes
            broadcast(msg);
        } else if (msg.getType() == RESPONSE) { //If it is a response calculate the next node to transmit to
            if (!((peerDeviceNames==null) || (peerDeviceNames.length==0))) {
                for (String neighbour : peerDeviceNames) {
                    if (neighbour.equals(msg.getDest())) {
                        messageTaskQueue.addTask(new MessageTask(msg, neighbour));
                        Log.d(TAG4, "ADD TASK 500");
                        break;
                    } else {
                        Log.d(TAG2, "dest " + msg.getDest() + " not in neighbors, broadcasting");
                        broadcast(msg);
                    }
                }
                messageTaskQueue.run();
            } else
            {
                Log.d(TAG, "Peerlist is empty, resetting and retrying. Please wait");
                resetStatus();
                Log.d(TAG2,"RESET FROM 469");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        prepMessageAndAddToQueue(msg);
                    }
                }, 10000);
            }
        }
    }


    public String getMyDeviceName() {
        return thisDeviceName;
    }

    private void broadcast(MyMessage msg) {
        //adds N messages to the queue with each of the N neighoboring nodes as next hop respectively"
        Log.d(TAG,"Broadcasting msg of type " + msg.getType() + "..");
        Log.d(TAG3,"BRCST: Piggyback is  " + msg.getPiggyBackedRoute());
        boolean taskAdded = false; //TODO FIX...
        if (peerDeviceNames != null) {
            if (peerDeviceNames.length==0){
                Log.d(TAG, "No peers found for broadcast. Waiting and trying again..");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        broadcast(msg);
                        messageTaskQueue.run();
                    }
                },1000);
            } else {
                for (String node : peerDeviceNames) {
                    Log.d(TAG3, "Checking for neighbor " + node);
                    if (!(msg.getPiggyBackedRoute().contains(node.substring(0, 1)) || node.equals(lastSender))) { //Send only if neighbor node is not on piggyback route
                        messageTaskQueue.addTask(new MessageTask(msg, node));
                        Log.d(TAG4, "ADD TASK 536");
                        taskAdded = true;
                        brcstTry=0;
                    } else {
                        Log.d(TAG3, "Not sending cause in piggypath or is last sender");
                    }
                }
                if (!(taskAdded) && (brcstTry<MAX_BROADCASTING_TRIES)){
                    brcstTry++;
                    Log.d(TAG3, "Retrying cause no sending occured at all");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            broadcast(msg);
                            messageTaskQueue.run();
                        }
                    },2000);
                }
            }
        } else {
            Log.d(TAG, "No peers found for broadcast!");
        }
    }


    public void updateRoutingTable(Collection<WifiP2pDevice> neigbours) {
        //Takes as input the collection of immediate neighbours and adds them in the routing table.
        for (WifiP2pDevice neigbour : neigbours) {
            if (!(routingTable.containsKey(neigbour)) || (routingTable.get(neigbour.deviceName).numberOfHops>1)) {  //Check if node exists already as an immediate neighbour
                routingTable.put(neigbour.deviceName, new RouteInfoTuple(neigbour.deviceName, 1));
            }
        }
        Log.d(TAG2, "routing table updated with immediate neighbours");
    }

    public void updateRoutingTable(LinkedList<String> newRoute) {
        for (String node : newRoute) {
            if (routingTable.containsKey(node)) {     //If node X exists in routing table, check if the received path indicates the same node to forward when sending to X
                if (routingTable.get(node).nextHop.equals(newRoute.getLast())) {  //if yes do nothing
                    Log.d(TAG2, "route info already exists");
                } else {   //If node X exists but nextHop is different check the number of hops and keep the smaller one
                    if (newRoute.size() < routingTable.get(node).numberOfHops) {
                        routingTable.put(node, new RouteInfoTuple(node, newRoute.size()));
                    }
                }
            } else {        //If node X doesn't exist in routing table, add it
                routingTable.put(node, new RouteInfoTuple(node, newRoute.size()));
            }
            newRoute.removeFirst();     //strip first node of path and if not empty repeat the process
            if (!newRoute.isEmpty()) {
                updateRoutingTable(newRoute);
            } else {   //update the view
                Log.d(TAG2, "routing table updated via piggyback path");
                //myRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

    public String getDeviceAddressFromName(String name){
        String addr = "NOT FOUND";
        for (WifiP2pDevice device : peers) {
            Log.d(TAG2, "checking for device " + device.deviceName.substring(0,1));
            if (device.deviceName.substring(0,1).equals(name)) { //TODO FIX HACK FOR Samsung Galaxy name
                addr = device.deviceAddress;
            }
        }
        if (addr == null || addr.equals("NOT FOUND")) {
            Log.d(TAG, "device not found");
            addr = "NOT FOUND";
        }
        return addr;
    }

    private void parseMessage (@NotNull MyMessage msg){
        Log.d(TAG2, "received piggy route is" + msg.getPiggyBackedRoute().toString());
        LinkedList<String> path = (LinkedList<String>) msg.getPiggyBackedRoute().clone();
        updateRoutingTable(path);
        lastSender = msg.getImmediateSender();
        msg.setImmediateSender(thisDeviceName);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"MESSAGE RECEIVED", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, "RECEIVED MSG OF TYPE " + msg.getType() + " FROM " + lastSender + " ORIGINATING FROM " + msg.getSource());
        Log.d("METRICS",  thisDeviceName + ": RECEIVED MSG OF TYPE " + msg.getType() + " ORIGINATING FROM " + msg.getSource() + " at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()));
        int type = msg.getType();
        switch (type) {
            case REQUEST :
                CrowdTask requestTask = msg.getTask();
                if (taskList.contains(requestTask)) {   //If requested task already exists update only the path if shorter
                    if (requestTask.getPiggyPath().size() < taskList.get(taskList.indexOf(requestTask)).getPiggyPath().size()) {
                        taskList.get(taskList.indexOf(requestTask)).setPiggyPath(requestTask.getPiggyPath());
                    }
                } else {   //if new add it to the tasklist
                    requestTask.setPiggyPath(msg.getPiggyBackedRoute());
                    taskList.add(requestTask);
                    Log.d(TAG, "New task received; added to tasklist");
                    requestTask.setActivity(this);
                    //Create task directory
                    /*String pathDir = getFilesDir() + File.separator + "RECEIVED" + File.separator + requestTask.getId();
                    File taskDir = new File (pathDir);
                    if (!taskDir.mkdir()) {
                        Log.d(TAG,"Task directory creation failed!");
                    };*/
                }
                //Forward request to other nodes:
                msg.addToPiggyBackedRoute(thisDeviceName.substring(0, 1));  //add name to piggy path, reset connections, wait 1 sec and broadcast request //TODO FIX HACK TO REMOVE EMPTY CHARACTERS FROM PHONE A..
                resetStatus();
                Log.d(TAG2, "RESET FROM 573");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "Message SRC is : " + msg.getSource());
                        broadcast(msg);
                        messageTaskQueue.run();
                    }
                }, 1000);
                break;
            case RESPONSE:            //If task is response, check if it is for me. If yes run immediately
                //Log.d(TAG, "PASSED AS A RESPONSE msg type " + msg.getType());
                if (msg.getDest().equals(thisDeviceName)) {
                    Log.d(TAG, "Received response for me, running task or collecting data..");
                    if (msg.getTask()==null){
                        String dirPath = getExternalFilesDir(null) + File.separator + msg.getTaskIdCollected() + "data_from_" + msg.getSource(); ;
                        //Log.d(TAG,"Creating file to stream data at: " + dirPath);
                        File file = new File(dirPath);
                        if (!(file.exists())){
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                Log.d(TAG, "Error when creating file for data at " + dirPath);
                                e.printStackTrace();
                            }
                        }
                        /*try {
                            file.createNewFile();
                        } catch (IOException e) {
                            Log.d(TAG,"641 IT WAS YOU ALL ALONG");
                            e.printStackTrace();
                        }*/
                        FileOutputStream stream = null;
                        try {
                            stream = new FileOutputStream(file);
                            Log.d(TAG, "Got data from " + msg.getSource() + " of size " + msg.getData().length + " Written to " + dirPath);
                            Log.d("METRICS", "Got data from " + msg.getSource() + " at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()));
                            stream.write(msg.getData());
                            stream.close();
                        } catch (FileNotFoundException e) {
                            Log.d(TAG, "647 FileNotFound");
                            e.printStackTrace();
                        } catch (IOException e) {
                            Log.d(TAG, "650 IOexception");
                            e.printStackTrace();
                        }

                    } else {
                        msg.getTask().setActivity(this);
                        msg.getTask().run();
                    }
                    resetStatus();
                    Log.d(TAG2,"RESET FROM 583");
                } else {                                       //If not for me forward it according to piggy path
                    Log.d(TAG, "Response not for me, forwarding..");
                    if (msg.getPiggyBackedRoute().getLast().equals(thisDeviceName)){
                        msg.stripFromPiggyBackedRoute();  //remove last node which if me (not the case if it came from a broadcast)
                    }
                    String nextNode = msg.getPiggyBackedRoute().getLast();  //forward to next node in the path
                    resetStatus();   //reset, wait 1 sec and then look if next node in neighbors to forward to him or else broadcast
                    Log.d(TAG2,"RESET FROM 589");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            boolean destFound = false;
                            for (String neighbour : peerDeviceNames) {
                                if (neighbour.equals(nextNode)){
                                    messageTaskQueue.addTask(new MessageTask(msg, neighbour));
                                    Log.d(TAG4, "ADD TASK 681");
                                    destFound = true;
                                    Log.d(TAG2, "Response destination in neighbours, forwarding to it");
                                    handler.post(new AdvanceQueue());
                                    Log.d(TAG2, "POSTED ADVANCE QUEUE");
                                    break;
                                }
                            }
                            if (!destFound) {
                                Log.d(TAG, "Response destination not in neighbours, waiting and trying again..");
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean destFound = false;
                                        for (String neighbour : peerDeviceNames) {
                                            if (neighbour.equals(nextNode)){
                                                messageTaskQueue.addTask(new MessageTask(msg, neighbour));
                                                Log.d(TAG4, "ADD TASK 681");
                                                destFound = true;
                                                Log.d(TAG2, "Response destination in neighbours, forwarding to it");
                                                break;
                                            }
                                        }
                                        if (!destFound) {
                                            Log.d(TAG, "Response destination again not in neighbours, dropping");
                                            //broadcast(msg);
                                        }
                                    }
                                }, 5000);
                            }
                        }
                    }, 1000);
                }
                break;
            case COLLECT:
                //Forward request to other nodes:
                msg.addToPiggyBackedRoute(thisDeviceName.substring(0, 1));  //add name to piggy path, reset connections, wait 1 sec and broadcast request //TODO FIX HACK TO REMOVE EMPTY CHARACTERS FROM PHONE A..

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetStatus();
                        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    broadcast(msg);
                                    messageTaskQueue.run();
                                }
                            },5*1000);
                        }
                    }, 1000);

                //Check for data to collect
                boolean taskFound= false;
                String taskId = null;

                for (CrowdTask task : taskList){
                    Log.d(TAG3, "checking for task " + task.getSourceNode() + " if with source "  + msg.getSource());
                    if (task.getSourceNode().substring(0,1).equals(msg.getSource().substring(0,1))){
                        taskFound=true;
                        taskId = task.getId();
                        break;
                    }
                }
                if (taskFound) {
                    Log.d(TAG2, "looking for the data of task at " +getExternalFilesDir(null) + File.separator +  "data_" + taskId + ".txt");
                    File data = new File(getExternalFilesDir(null) + File.separator +  "data_" + taskId + ".txt");
                    if (data.isFile()){
                        Log.d(TAG, "Data ready for collection, will send response");
                        int size = (int) data.length();
                        byte[] bytes = new byte[size];
                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(data));
                            buf.read(bytes, 0, bytes.length);
                            buf.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                        String macAdress = MainActivity.wifiManager.getConnectionInfo().getMacAddress();
                        String id = macAdress + ":" + timestamp;
                        MyMessage responseMsg = new MyMessage(id, thisDeviceName, msg.getSource(), RESPONSE, bytes, taskId, msg.getPiggyBackedRoute());
                        resetStatus();
                        postResponseStartTime = System.currentTimeMillis();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                boolean destFound=false;
                                if (!((peerDeviceNames == null) || (peerDeviceNames.length == 0))) {
                                    for (String neighbour : peerDeviceNames) {
                                        //Log.d(TAG, "neihbour is : " + neighbour.substring(0,1) + " and dest is " + responseMsg.getDest());
                                        if (neighbour.substring(0,1).equals(responseMsg.getDest())) {
                                            messageTaskQueue.addTask(new MessageTask(responseMsg, neighbour));
                                            Log.d(TAG4, "ADD TASK 736");
                                            destFound=true;
                                            break;
                                        }
                                    }
                                    if (!(destFound)) {
                                            msg.stripFromPiggyBackedRoute();
                                            String nexthop = msg.getPiggyBackedRoute().getLast();
                                            for (String neighbour : peerDeviceNames) {
                                                Log.d(TAG2, "neihbour is : " + neighbour.substring(0,1) + " and dest is " + nexthop);
                                                if (neighbour.substring(0,1).equals(nexthop)) {
                                                    messageTaskQueue.addTask(new MessageTask(responseMsg, neighbour));
                                                    messageTaskQueue.run();
                                                    Log.d(TAG4, "ADD TASK 736");
                                                    destFound=true;
                                                    break;
                                                }
                                            }
                                    }
                                    if (!(destFound)){
                                        Log.d(TAG2, "RSP: destination/piggy not found in neighbors, dropping");
                                        //broadcast(responseMsg);
                                    }
                                    messageTaskQueue.run();
                                } else {
                                    Log.d(TAG3, "Peerlist empty for immediate response will try in a bit");
                                    if (System.currentTimeMillis() - postResponseStartTime > 500000) {
                                        postResponseTimeout = true;
                                    }
                                    if (!(postResponseTimeout)) {
                                        resetStatus();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                boolean destFound=false;
                                                if (!((peerDeviceNames == null) || (peerDeviceNames.length == 0))) {
                                                    for (String neighbour : peerDeviceNames) {
                                                        if (neighbour.equals(responseMsg.getDest())) {
                                                            destFound=true;
                                                            messageTaskQueue.addTask(new MessageTask(responseMsg, neighbour));
                                                            Log.d(TAG4, "ADD TASK 757");
                                                            break;
                                                        }
                                                    }
                                                    if (!(destFound)) {
                                                        Log.d(TAG2, "dest " + responseMsg.getDest() + " not in neighbors, broadcasting");
                                                        broadcast(responseMsg);
                                                    }
                                                    messageTaskQueue.run();
                                                }
                                            }
                                        }, 10000);
                                    } else
                                    {
                                        Log.d(TAG3, "TIMEOUT, Response aborted");
                                    }
                                }
                            }
                        }, 15000);
                    } else{
                        Log.d(TAG,"Data not ready to be collected");
                    }
                } else {
                    Log.d(TAG3, "Task to be collected not found");
                }
        }
    }

    public void hardReset() {
        //messageTaskQueue.messageTaskList.clear();
        //taskList.clear();
        handler.post(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        });
    }

    public void makeDialog(String requester, int type, String responseText){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (type){
            case REQUEST: {
                builder.setTitle("Please write a custom message for device " + requester);
                builder.setCancelable(false);
                EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishTask(TASK_COMPLETE);
                        CrowdTask responseTask = new CrowdTask(getMyDeviceName(), RESPONSE, input.getText().toString());
                        prepMessageAndAddToQueue(requester, RESPONSE, responseTask);
                        messageTaskQueue.run();
                    }
                });
                builder.create().show();
                break;
            }
            case RESPONSE: {
                builder.setTitle("MSG RECEIVED: \n" + responseText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishTask(TASK_COMPLETE);
                    }
                });
                builder.create().show();
            }
        }
    }

    public void clearP2PConnections ( boolean retrySend){
        discoveryEnabled = false;
        if (mManager != null && mChannel != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null) {
                        Log.d(TAG2, "will try to remove group of owner " + group.getOwner().deviceName);
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG2, "removeGroup onSuccess");
                                //deletePersistentGroup(group, mManager, mChannel);
                                connectedToPeer = false;
                                startDiscovery();
                                handler.postDelayed(new AdvanceQueue(), 2000);
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG2, "removeGroup onFailure with reason " + reason);
                                //deletePersistentGroup(group, mManager, mChannel);
                                resetStatus(true);
                                Log.d(TAG2,"RESET FROM 679");
                            }
                        });
                    } else {
                        connectedToPeer = false;
                        Log.d(TAG2, "No group present to remove");
                        if (retrySend) {
                            handler.postDelayed(new AdvanceQueue(), RETRY_SEND_INTERVAL);
                        }

                    }
                }
            });
        } else {
            Log.d(TAG, "wifiP2P manager or chanell is null!");
        }
    }

    public void startDiscovery () {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            Log.d(TAG, "Does not have fine_location permission");
            return;
        }
        if (!wifiManager.isWifiEnabled()) {
            if (!(Looper.myLooper() == Looper.getMainLooper())) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //Toast.makeText(MainActivity.this, "Please turn on WiFi", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                //Toast.makeText(getApplicationContext(), "Please turn on WiFi", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG3, "Initiating discovery...");
            discoveryEnabled = true;
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    connectionStatus.setText("Discovery Started");
                }

                @Override
                public void onFailure(int i) {
                    Log.d(TAG3, "Onfailure startDiscovery() with reason " + i);
                    connectionStatus.setText("Discovery Starting Failed");
                    //startDiscovery();
                }
            });
        }
    }

    public void stopDiscovery(){
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG3,"Stopped Peer discovery successful");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG3,"Stopped Peer discovery failed");
            }
        });
    }

    public void resetStatus ( boolean ifRetrySend){
        stopDiscovery();
        Log.d(TAG2, "RESET CALLED");
        clearNetworkSockets();
        clearP2PConnections(ifRetrySend);

        handler.post(new Runnable() {
            @Override
            public void run() {
                read_msg_box.setText(String.format("   "));
            }
        });
        brcstTry=0;
        peers.clear();
        lastSender = " ";
        IamSender = false;
        WhileSending = false;
        messageToSend = null;
        discoveryEnabled = true;
        postResponseTimeout = false;
        peerDeviceConnected = " ";
        connectedToPeer = false;
        //startDiscovery();
    }

    public void resetStatus () {
        Log.d(TAG2, "RESET CALLED");
        clearNetworkSockets();
        clearP2PConnections(false);
        handler.post(new Runnable() {
            @Override
            public void run() {
                read_msg_box.setText(String.format("   "));
            }
        });
        lastSender = " ";
        brcstTry=0;
        peers.clear();
        IamSender = false;
        WhileSending = false;
        messageToSend = null;
        discoveryEnabled = true;
        postResponseTimeout = false;
        peerDeviceConnected = " ";
        connectedToPeer = false;
        //startDiscovery();
    }


    private void clearNetworkSockets () {
        if (serverClass != null && serverClass.socket != null) {
            try {
                serverClass.socket.close();
                if (serverClass.serverSocket != null) {
                    serverClass.serverSocket.close();
                }
                serverClass = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (clientClass != null && clientClass.socket != null) {
            try {
                clientClass.socket.close();
                clientClass = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (sendReceive != null && sendReceive.socket != null) {
            try {
                sendReceive.socket.close();
                sendReceive = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onCreate (Bundle savedInstanceState){
        Log.d(TAG4,"MAIN ACTIVITY ONCREATE");
        if (savedInstanceState!=null){
            Log.d(TAG4,"Restoring data..");
            Log.d(TAG4,"Tasklist has " + taskList.size() + " tasks");
            taskList = savedInstanceState.getParcelableArrayList("tasklist");
            routingTable = (HashMap<String, RouteInfoTuple>) savedInstanceState.getSerializable("routingTable");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        initialize();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG4,"Saving data..");
        Log.d(TAG4,"Tasklist has " + taskList.size() + " tasks");
        outState.putParcelableArrayList("taskList", taskList);
        outState.putSerializable("routingTable", routingTable);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.routing_table:
                Intent intent = new Intent(this, RoutingTableActivity.class);
                this.startActivity(intent);
                break;
            case R.id.set_device_name:
                askDeviceName(false, null);
                break;
            case R.id.taskOverview:
                Intent intent1 = new Intent(this, TaskOverviewActivity.class);
                this.startActivity(intent1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume () {
        Log.d(TAG4,"MAIN ACTIVITY ONRESUME");
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Does not have fine_location permission");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
/*    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

        @Override
        public void onSuccess() {
            Log.v(TAG, "Discovery process succeeded");
        }

        @Override
        public void onFailure(int reason) {
            Log.v(TAG, "Discovery process failed");
        }
    }); */
    }

    @Override
    public void onPause () {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStop() {
        Log.d(TAG4, "MAIN ACTIVITY ONSTOP");
        super.onStop();
    }

    private void deletePersistentGroup (WifiP2pGroup wifiP2pGroup, WifiP2pManager manager, WifiP2pManager.Channel channel){
        try {

            Method getNetworkId = WifiP2pGroup.class.getMethod("getNetworkId");
            Integer networkId = (Integer) getNetworkId.invoke(wifiP2pGroup);
            Method deletePersistentGroup = WifiP2pManager.class.getMethod("deletePersistentGroup",
                    WifiP2pManager.Channel.class, int.class, WifiP2pManager.ActionListener.class);
            deletePersistentGroup.invoke(manager, channel, networkId, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e(TAG2, "deletePersistentGroup onSuccess");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG2, "deletePersistentGroup failure: " + reason);
                }
            });
        } catch (NoSuchMethodException e) {
            Log.e("WIFI", "Could not delete persistent group", e);
        } catch (InvocationTargetException e) {
            Log.e("WIFI", "Could not delete persistent group", e);
        } catch (IllegalAccessException e) {
            Log.e("WIFI", "Could not delete persistent group", e);
        }
    }



    //NESTED CLASSES

    public class ServerClass extends Thread {

        Socket socket=null;
        ServerSocket serverSocket;
        int i = 1;
  /*  public ServerClass(){
        Log.d(TAG, "On serverclass constructor");
        socket=null;
        serverSocket=null;
    }*/

        @Override
        public void run() {
            socket=null;
            //Log.d(TAG3, "SERVER running in thread: " + Thread.currentThread().getName());
            if (i <= 4) {
                Log.d(TAG3, "Try " + i + " to open socket");
                try {
                    Log.d(TAG3, "before serverSocket build");
                    serverSocket = new ServerSocket(8888);
                    Log.d(TAG3, "serverSocket created");
                    Thread timeoutThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG2, "Running Serversocket listener timeout thread");
                            long startTime = System.currentTimeMillis();
                            boolean timeout = false;
                            while (socket==null && !timeout) {
                                if (System.currentTimeMillis() - startTime > CONNECTION_TIMEOUT_LIMIT) {
                                    timeout = true;
                                }
                            }
                            if (timeout) {
                                try {
                                    serverSocket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "Serversocket timeout, trying again");
                                try {
                                    serverSocket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                serverSocket = null;
                                i++;
                                serverClass.run();
                            }
                        }
                    });
                    timeoutThread.start();
                    socket = serverSocket.accept();
                    Log.d(TAG3, "ServerClass thread started succesfully");
                    sendReceive = new SendReceive(socket);
                    //long startTime = System.currentTimeMillis();
                    //boolean timeout = false;
                    //while (sendReceive==null || timeout) {
                    //    if {System.currentTimeMillis()-startTime
                    //}
                    if (sendReceive == null) {
                        Log.d(TAG3, "Could not instantiate SendReceive thread, socket is:" + socket.toString());
                    }
                    Log.d(TAG3, "SendReceive thread created succesfully");
                    //                   sendReceive.start();
                } catch (IOException e) {
                    Log.d(TAG3, e.toString());
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG3, "Server start failed after" + i + " times, resetting system");
                resetStatus();
                Log.d(TAG2,"RESET FROM 947");
            }
        }
    }

    public class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        boolean sendReceiveTimeout;

        public SendReceive(Socket skt) {
            //Log.d(TAG3, "SENDRCV running in thread: " + Thread.currentThread().getName());
            socket = skt;
            try {
                inputStream = skt.getInputStream();
                outputStream = skt.getOutputStream();
                Log.d(TAG3, "SendReceive thread started succesfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            sendReceiveTimeout=false;
            long startTime = System.currentTimeMillis();
            if (!IamSender) {
                if (socket != null && !socket.isClosed() && (!sendReceiveTimeout)) {//TODO ADD TIMEOUT!!
                    Log.d(TAG3, "Checking for sendreceive timeout");
                    if (System.currentTimeMillis() - startTime > 50000000) {
                        Log.d(TAG3, "SENDRECEIVE TIMEOUT");
                        sendReceiveTimeout= true;
                    }
                    try {
                        ObjectInputStream ois = new ObjectInputStream(new InflaterInputStream(inputStream));
                        Log.d(TAG2, "created INPUTSTREAM");
                        receivedMessage = (MyMessage) ois.readObject();
                        if (receivedMessage.getType()==RESPONSE) {
                            Log.d(TAG2, "Message data received are this big: " + receivedMessage.getData().length);
                        }
                        handler.obtainMessage(MESSAGE_READ, receivedMessage).sendToTarget();
                        //parseMessage(receivedMessage);
                        ois.close();
                        //break;
                    } catch (ClassNotFoundException e) {
                        Log.d(TAG3, "1211: " + e.toString());
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d(TAG3, "1214: " + e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }

        public void write(MyMessage msg) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(outputStream));
                        oos.writeObject(msg);
                        oos.flush();
                        oos.close();
                        Log.d(TAG2, "Written to outputStream");
                        handler.sendEmptyMessage(MESSAGE_SENT);
                    } catch (Exception e) {
                        Log.d(TAG3, "1239: " + e.toString());
                        e.printStackTrace();
                        messageTaskQueue.sendTaskComplete(SENT_FAILURE);
                    }
                }
            });
            thread.start();
        }
    }

    public class ClientClass extends Thread {
        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            //Log.d(TAG3, "CLIENT running in thread: " + Thread.currentThread().getName());
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                //Log.d(TAG, "Client thread started succesfully");
                sendReceive = new SendReceive(socket);
                //             sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ;
        }
    }

    public  class MessageTask implements Runnable {
        MyMessage msg;
        String nextHop;

        public MessageTask(MyMessage msg, String nextNode) {
            this.msg = msg;
            this.nextHop = nextNode;
        }

        @Override

        public void run() {
            //Log.d(TAG3, "TASK running in thread: " + Thread.currentThread().getName());
            WhileSending = true;
            WifiP2pConfig config = new WifiP2pConfig();
            Log.d(TAG2, "MessageTask run (try no." + messageTaskQueue.numTries +  "), trying to connect to " + nextHop);
            config.deviceAddress = getDeviceAddressFromName(nextHop);
            if (config.deviceAddress.equals("NOT FOUND")) {
                Log.d(TAG, "Could not retrieve address from node name");
                messageTaskQueue.sendTaskComplete(SENT_FAILURE);
            } else {
                connectToPeer(config);
            }

        }

        @SuppressLint("MissingPermission")
        public void connectToPeer(WifiP2pConfig nextHopConfig) {
            mManager.connect(mChannel, nextHopConfig, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "OnSuccess for " + nextHop + ". Waiting for ConnectionListener");
                    IamSender = true;
                    messageToSend = msg;
                    Thread timeoutThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d(TAG2, "Running connection listener timeout thread");
                            long startTime = System.currentTimeMillis();
                            boolean timeout = false;
                            Log.d(TAG2, "ConnectedToPeer = " + connectedToPeer);
                            while (!(connectedToPeer || timeout)) {
                                if (System.currentTimeMillis() - startTime > CONNECTION_LISTENER_TIMEOUT_LIMIT) {
                                    timeout = true;
                                }
                            }
                            if (!timeout) {
                                peerDeviceConnected = nextHop;
                                Log.d(TAG, "connected to " + peerDeviceConnected);
                            } else {
                                Log.d(TAG, "Connection listener timeout, trying again");
                                messageTaskQueue.sendTaskComplete(SENT_FAILURE);
                            }
                        }
                    });
                    timeoutThread.start();
                }

                @Override
                public void onFailure(int i) {

                    Log.d(TAG2, "Failed to connect to " + nextHop + " with reason: " + i);
                    Log.d(TAG2, "(0 : error, 1: p2p unsupported 2: busy)");
                    messageTaskQueue.sendTaskComplete(SENT_FAILURE);
                }
            });
        }
    }

    public class WakeUp implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (!WhileSending && discoveryEnabled && (!discoveryActive || (peerDeviceNames == null || peerDeviceNames.length == 0))) {//&& !discoveryActive) {   // &&
                    Log.d(TAG3, "WhileSending=" + WhileSending + " discoveryEnabled="+ discoveryEnabled + " discoveryActive=" +discoveryActive);
                    Log.d(TAG3, "waking up the discovery!");
                    startDiscovery();
                }
                try {
                    Thread.currentThread().sleep(5000);
                } catch (InterruptedException e) {
                    Log.d(TAG3, e.toString());
                    e.printStackTrace();
                }
            }

        }
    }

    public class readLightRunnable extends TaskExecutable implements Serializable {
        public readLightRunnable() {
            super(SENSING_TASK);
        }

        @Override
        public void run() {

            super.run();
        }
    }

    public static class AdvanceQueue implements Runnable {
        @Override
        public void run() {
            messageTaskQueue.run();
        }
    }
}
