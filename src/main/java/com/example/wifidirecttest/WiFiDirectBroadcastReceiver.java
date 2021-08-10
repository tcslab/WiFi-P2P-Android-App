package com.example.wifidirecttest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.net.NetworkInfo;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import static android.graphics.Color.parseColor;
import static com.example.wifidirecttest.MainActivity.TAG;
import static com.example.wifidirecttest.MainActivity.TAG2;
import static com.example.wifidirecttest.MainActivity.TAG3;

/*
This class implements a broadcast receiver to listen to the various changes of Wifi and Wifi P2P state and variables.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private MainActivity mActivity;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private boolean firstTime = true;

    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mainActivity) {
        this.mActivity = mainActivity;
        this.mManager = mManager;
        this.mChannel = mChannel;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, "ON_RECEIVE");
        //get intent that triggered the BR
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {     //If peers have changed get the new peers and trigger the peerListListener in MainActivity
            //Log.d(TAG3, "PEERS CHANGED ACTION INTENT CAUGHT");
            if (mManager!=null){
                mManager.requestPeers(mChannel, mActivity.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {    //This gets called If a P2P connection is established or dropped
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {   //If new connection, trigger connectionListener
                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
            } else {
                mActivity.connectionStatus.setText("Device disonnected");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {  //This gets called if the device name changes, the first time is just to get the initial value
            Log.d(TAG3, "THIS DEVICE CHANGED ACTION CALLED");
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            if (firstTime) {
                mActivity.setDeviceName(device.deviceName.substring(0,1), firstTime, false);
                Log.d(TAG3, "DEVICE NAME SET");
                firstTime = false;
            }
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){   //This gets called if P2P discovery state changes from active to inactive and vice versa
            int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if (discoveryState==WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
                Log.d(TAG2, "Discovery started");
                mActivity.discoveryActive = true;
                mActivity.discoveryStatusIndicator.setImageResource(R.drawable.green_box);
            } else if (discoveryState==WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                Log.d(TAG2, "Discovery stopped");
                mActivity.discoveryActive=false;
                mActivity.discoveryStatusIndicator.setImageResource(R.drawable.red_box);
            } else{
                Log.d(TAG, "EXTRA DISCOVERY STATE COULD NOT BE READ IN BR");
            }
        }
    }
}