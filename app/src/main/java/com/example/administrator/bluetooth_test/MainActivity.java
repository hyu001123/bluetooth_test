package com.example.administrator.bluetooth_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.administrator.bluetooth_test.adapter.BTAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private boolean mIsBind;
    private static final int SERVICE_BIND = 1;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
        }
    };
    private Button btnScan;
    private ListView lv_devices;
    private BluetoothAdapter adapterBT;
    private List<Map<String ,String>> listData=new ArrayList<>();
    private List<BluetoothDevice> deviceList=new ArrayList<>();
    private BroadcastReceiver mReceiver;
    private BTAdapter adapter;
    private AcceptThread acceptAdapter;
    private BTFoundReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver=new BTFoundReceiver();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        initBlueTooth();
        initView();
        acceptAdapter=new AcceptThread(adapterBT,mHandler);
        acceptAdapter.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void initBlueTooth() {
        adapterBT = BluetoothAdapter.getDefaultAdapter();
        if (!adapterBT.isEnabled()) {
            Intent BTenableintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BTenableintent, 100);
            Log.i("tag", "bluetooth.name====" + adapterBT.getName());
        }
    }

    private void initView() {
        btnScan = (Button) findViewById(R.id.scan);
        lv_devices = (ListView) findViewById(R.id.lv_device);
        //adapter = new SimpleAdapter(this, listData,android.R.layout.simple_list_item_2, new String[]{"name","Id"},new int[]{android.R.id.text1,android.R.id.text2});
        adapter=new BTAdapter(this,deviceList);
        lv_devices.setAdapter(adapter);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //listData.clear();
                deviceList.clear();
                Set<BluetoothDevice> pairedDevices = adapterBT.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        deviceList.add(device);
                        adapter.notifyDataSetChanged();
                        Log.i("tag","bondeddevice.state==="+device.getBondState());
                    }
                }
                if(adapterBT.isDiscovering()){
                    return;
                }
                adapterBT.startDiscovery();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapterBT.cancelDiscovery();
                    }
                },30000);
            }
        });
       lv_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                try {
                    //第一种配对方法：deviceList.get(position).createBond();
                   //第二种配对方法：Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                   // Object socket = createBondMethod.invoke(deviceList.get(position));
                    deviceList.get(position).createBond();
                    LayoutInflater inflater = getLayoutInflater();
                   View itemView=inflater.inflate(R.layout.item_edittext_dialog,null);
                    final TextInputLayout contentBT = (TextInputLayout) itemView.findViewById(R.id.textInput_name);
                    AlertDialog builder=new AlertDialog.Builder(MainActivity.this)
                            .setTitle("蓝牙发送内容：")
                            .setView(itemView)
                            .setNegativeButton("取消",null)
                            .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapterBT.cancelDiscovery();
                                    String ct = contentBT.getEditText().getText().toString();
                                    if(!ct.isEmpty()){
                                        ConnectThread connectThread = new ConnectThread(MainActivity.this,adapterBT.getRemoteDevice(deviceList.get(position).getAddress()),ct);
                                        connectThread.start();
                                    }
                                }
                            })
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            Log.i("tag", "resultCode===" + resultCode);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    class BTFoundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Map<String ,String> map=new HashMap<>();
                map.put("name",device.getName());
                map.put("Id",device.getAddress());
                listData.add(map);
                deviceList.add(device);
                Log.i("tag", "device===" + device.getName() + "\ndeviceId===" + device.getAddress()+"\ndeviceState==="+device.getBondState());
            }
            adapter.notifyDataSetChanged();
        }
    }
}