package com.example.administrator.bluetooth_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class AcceptThread extends Thread{

    private final BluetoothAdapter BTadapter;
    private BluetoothServerSocket serverSocket;
    private final Handler mhandler;
    private BluetoothSocket socket;// 获取到客户端的接口
    private InputStream is;// 获取到输入流

    public AcceptThread(BluetoothAdapter BTadapter, Handler mhandler){
        this.BTadapter=BTadapter;
        this.mhandler=mhandler;
    }

    public void run() {
        try {
            // 接收其客户端的接口
            serverSocket=BTadapter.listenUsingRfcommWithServiceRecord("com.example.administrator.bluetooth_test",
                    UUID.fromString("00001105-0000-1000-8000-00805f9b34fb"));
            while (true) {
                socket = serverSocket.accept();
               // progressSocket(socket);
            }
        }
        catch (Exception e) {
            // TODO: handle exception  
            e.printStackTrace();
        }


    }

    private void progressSocket(final BluetoothSocket socket) {

        try {
            is = socket.getInputStream();
            // 获取到输入流

            // 获取到输出流
            int len=0;
            // 无线循环来接收数据

            byte[] buffer = new byte[128];
            // 每次读取128字节，并保存其读取的角标
            int count = is.read(buffer);
            // 创建Message类，向handler发送数据
            Message msg = new Message();
            // 发送一个String的数据，让他向上转型为obj类型
            msg.obj = new String(buffer, 0, count, "utf-8");
            // 发送数据
            mhandler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
