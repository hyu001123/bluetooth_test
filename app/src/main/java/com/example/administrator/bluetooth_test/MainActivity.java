package com.example.administrator.bluetooth_test;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.icu.text.UnicodeSetSpanner;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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

import java.lang.reflect.Method;
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
            Toast.makeText(MainActivity.this, ""+ msg.obj, Toast.LENGTH_LONG).show();
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
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private ScanCallback scanCallback;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothProfile a2dpProfile;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver=new BTFoundReceiver();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != getPackageManager().PERMISSION_GRANTED
                    ) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
            }
        }
        initBlueTooth();
        initView();
        //经典蓝牙开启服务端
        acceptAdapter=new AcceptThread(adapterBT,mHandler);
        acceptAdapter.start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 200:
                if (grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                    Log.i("tag","grantResult=="+grantResults);
                    Toast.makeText(this,"已获得权限！",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void initBlueTooth() {
        check();
        /**
         * <bluetooth4.0
         */
        adapterBT = BluetoothAdapter.getDefaultAdapter();
        adapterBT.getProfileProxy(this, new ProfileListener(), BluetoothProfile.A2DP);
        if (!adapterBT.isEnabled()) {
            Intent BTenableintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BTenableintent, 100);
            Log.i("tag", "bluetooth.name====" + adapterBT.getName());
        }
        /***
         * bluetooth smart ready
         */
        /*BluetoothManager BTManager=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter=BTManager.getAdapter();
        leScanner=bluetoothAdapter.getBluetoothLeScanner();
        Check();
        if(bluetoothAdapter==null||!bluetoothAdapter.isEnabled()){
            Intent BTenableintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BTenableintent, 100);
        }*/

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
                /**
                 * <bluetooth4.0
                 */
                if(adapterBT.isDiscovering()){
                    return;
                }
                deviceList.clear();
                Set<BluetoothDevice> pairedDevices = adapterBT.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        deviceList.add(device);
                        Log.i("tag","bondeddevice.state==="+device.getBondState());
                    }
                    adapter.notifyDataSetChanged();
                }
                adapterBT.startDiscovery();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapterBT.cancelDiscovery();
                    }
                },30000);
                /**
                 * bluetooth4.0
                 */
                //deviceList.clear();
                /*leScanner.startScan(scanCallback);
               // bluetoothAdapter.startLeScan(mLeScanCallback);
                mBluetoothLeAdvertiser=bluetoothAdapter.getBluetoothLeAdvertiser();
                //if(mBluetoothLeAdvertiser==null){
                if(!bluetoothAdapter.isMultipleAdvertisementSupported()){
                    Toast.makeText(MainActivity.this,"the device not support peripheral",Toast.LENGTH_LONG).show();
                }
                mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 0), createAdvertiseData(), mAdvertiseCallback);*/
            }
        });

        lv_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                try {
                    //第一种配对方法：deviceList.get(position).createBond();
                    //第二种配对方法：Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    // Object socket = createBondMethod.invoke(deviceList.get(position));
                    //deviceList.get(position).createBond();
                    a2dpConnect(deviceList.get(position));
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
                                        ConnectThread connectThread = new ConnectThread(MainActivity.this,deviceList.get(position),ct);
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

    /***
     * android bluetooth4.0
     */
       /* scanCallback=new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.i("tag","device"+result.getDevice().getName());
            }


            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.i("tag","errorCode==="+errorCode);
            }
        };

    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            if (settingsInEffect != null) {
                Log.i("tag", "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel()	 + " mode=" + settingsInEffect.getMode()
                        + " timeout=" + settingsInEffect.getTimeout());
            } else {
                Log.i("tag", "onStartSuccess, settingInEffect is null");
            }
            Log.i("tag","onStartSuccess settingsInEffect" + settingsInEffect);

        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            if(true) 	Log.i("tag","onStartFailure errorCode" + errorCode);

            if(errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE){
                if(true){
                    Toast.makeText(MainActivity.this,"advertise_failed_data_too_large", Toast.LENGTH_LONG).show();
                    Log.i("tag","Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
                }
            }else if(errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS){
                if(true){
                    Toast.makeText(MainActivity.this,"advertise_failed_too_many_advertises", Toast.LENGTH_LONG).show();
                    Log.i("tag","Failed to start advertising because no advertising instance is available.");
                }
            }else if(errorCode == ADVERTISE_FAILED_ALREADY_STARTED){
                if(true){
                    Toast.makeText(MainActivity.this, "advertise_failed_already_started", Toast.LENGTH_LONG).show();
                    Log.i("tag","Failed to start advertising as the advertising is already started");
                }
            }else if(errorCode == ADVERTISE_FAILED_INTERNAL_ERROR){
                if(true){
                    Toast.makeText(MainActivity.this, "advertise_failed_internal_error", Toast.LENGTH_LONG).show();
                    Log.i("tag","Operation failed due to an internal error");
                }
            }else if(errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED){
                if(true){
                    Toast.makeText(MainActivity.this, "advertise_failed_feature_unsupported", Toast.LENGTH_LONG).show();
                    Log.i("tag","This feature is not supported on this platform");
                }
            }
        }
    };


    private AdvertiseData createAdvertiseData() {
        AdvertiseData.Builder    mDataBuilder = new AdvertiseData.Builder();
        mDataBuilder.addServiceUuid(ParcelUuid.fromString("00001105-0000-1000-8000-00805f9b34fb"));
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        if(mAdvertiseData==null){
            if(true){
                Toast.makeText(this, "mAdvertiseSettings == null", Toast.LENGTH_LONG).show();
                Log.i("tag","mAdvertiseSettings == null");
            }
        }

        return mAdvertiseData;
    }

    private AdvertiseSettings createAdvSettings(boolean b, int i) {
        AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        mSettingsbuilder.setConnectable(true);
        mSettingsbuilder.setTimeout(15000);
        mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();
        if(mAdvertiseSettings == null){
            if(true){
                Toast.makeText(this, "mAdvertiseSettings == null", Toast.LENGTH_LONG).show();
                Log.i("tag","mAdvertiseSettings == null");
            }
        }
        return mAdvertiseSettings;
    }*/


    private void check() {
        if (!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "支持BLE", Toast.LENGTH_SHORT).show();
        }
    }

    public class ProfileListener implements BluetoothProfile.ServiceListener{


        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.i("tag","all ready connected");
            a2dpProfile=proxy;
        }


        @Override
        public void onServiceDisconnected(int profile) {
            Log.i("tag","device disconnected");
            a2dpProfile=null;
        }
    }

    //判断A2DO的连接状态
    public boolean getA2dpState(BluetoothDevice device){
        if(a2dpProfile != null && a2dpProfile.getConnectionState(device) ==     BluetoothProfile.STATE_CONNECTED){
            return true;
        }
        return false;
    }

    //A2DP与设备连接
    public void a2dpConnect(BluetoothDevice device){
        if(a2dpProfile != null){
            BluetoothA2dp a2dp = (BluetoothA2dp) a2dpProfile;
            Class<? extends BluetoothA2dp> clazz = a2dp.getClass();
            Method m2;
            try {
                Log.i("tag","use reflect to connect a2dp");
                m2 = clazz.getMethod("connect",BluetoothDevice.class);
                m2.invoke(a2dp, device);
            } catch (Exception e) {
                Log.e("tag","error:" + e.toString());
            }
            Log.i("tag","连接设备类型;"+String.valueOf((device.getBluetoothClass().getDeviceClass())& 0x1F00));
            //BluetoothClass.Device.Major.PHONE
        }
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
                deviceList.add(device);
                Log.i("tag", "device===" + device.getName() + "\ndeviceId===" + device.getAddress()+"\ndeviceState==="+device.getBondState());
            }
            adapter.notifyDataSetChanged();
        }
    }
}