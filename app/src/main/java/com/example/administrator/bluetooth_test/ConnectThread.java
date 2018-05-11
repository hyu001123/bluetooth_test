package com.example.administrator.bluetooth_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConnectThread extends Thread{
    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final Context context;
    private final String text;
    private OutputStream os;// 获取到输出流


    public ConnectThread(Context context, BluetoothDevice device,String text) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        mmDevice = device;
        this.context=context;
        this.text=text;
        StringBuilder sb=new StringBuilder();
        Log.i("info",sb.append(Arrays.toString(device.getUuids())).toString());

        // Get a BluetoothSocket to connect with the given BluetoothDevice

    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        //mBluetoothAdapter.cancelDiscovery();

        try {

            mmSocket=mmDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001105-0000-1000-8000-00805f9b34fb"));
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            //mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
            mmSocket.connect();
        }catch (IOException e) {
            try {
                //mmDevice.createBond();//SDK5.1要加上这个，否则报连接不上
                //BluetoothSocket a = new BluetoothSocket(1, -1, false, false, mmDevice, -1, UUID.fromString("00001105-0000-1000-8000-00805f9b34fb"));
                //mmSocket=mmDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001105-0000-1000-8000-00805f9b34fb"));
                mmSocket.connect();
            } catch (EOFException e1) {
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }catch (Exception connectException) {
            // Unable to connect; close the socket and get out
            connectException.printStackTrace();
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
        Log.i("tag","mmsocket===="+mmSocket);
    }

    private void manageConnectedSocket(final BluetoothSocket mmSocket) {
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
