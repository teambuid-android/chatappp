package com.example.wifidirectchat;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener {
    private ListView listViewDevices;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        listViewDevices = findViewById(R.id.listViewDevices);
        userName = getIntent().getStringExtra("USER_NAME");

        if (userName == null) {
            Toast.makeText(this, "Error: No user name", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        discoverPeers();

        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            WifiP2pDevice selectedDevice = (WifiP2pDevice) parent.getItemAtPosition(position);
            Intent intent = new Intent(DeviceListActivity.this, ChatActivity.class);
            intent.putExtra("DEVICE", selectedDevice);
            intent.putExtra("USER_NAME", userName);
            startActivity(intent);
        });
    }

    private void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(DeviceListActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(DeviceListActivity.this, "Discovery Failed: " + reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList deviceList) {
        List<WifiP2pDevice> devices = new ArrayList<>(deviceList.getDeviceList());
        ArrayAdapter<WifiP2pDevice> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices);
        listViewDevices.setAdapter(adapter);
    }
}
