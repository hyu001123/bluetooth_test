package com.example.administrator.bluetooth_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectThread extends Thread{
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final Context context;
    private final String text;
    private OutputStream os;// 获取到输出流


    public ConnectThread(Context context, BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.context=context;
        this.text="";

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("7df05018-89f2-4fbc-b755-db1e61041e59"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
        Log.i("tag","tmp=="+mmSocket);
    }

    public ConnectThread(Context context, BluetoothDevice device,String text) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.context=context;
        this.text=text;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("7df05018-89f2-4fbc-b755-db1e61041e59"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
        Log.i("tag","tmp=="+mmSocket);
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        //mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            if(text.isEmpty()) {
                mmSocket.connect();
            }
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            connectException.printStackTrace();
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        if(!text.isEmpty()){
            manageConnectedSocket(mmSocket);
        }
        Log.i("tag","mmsocket===="+mmSocket);
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    private void manageConnectedSocket(final BluetoothSocket mmSocket){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    os=mmSocket.getOutputStream();
                    // 判断是否拿到输出流
                    if (os != null) {
                        // 需要发送的信息
                        // 以utf-8的格式发送出去
                        os.write(text.getBytes("UTF-8"));
                    }
                    // 吐司一下，告诉用户发送成功
                    os.flush();
                    Looper.prepare();
                    Toast.makeText(context, "发送信息成功，请查收", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Looper.prepare();
                    e.printStackTrace();
                    Toast.makeText(context, "发送信息失败", Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        }).start();
    }
}
