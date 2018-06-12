package com.example.user.mywifidirect;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by user on 2017.07.02..
 */

class ServerConnection extends Thread{
final static int JPGQLTY_B=0;
    final static int FRAMERATE_DIVIDER=1;

    String TAG ="ServerConnection";

    DataOutputStream dao;
    ByteArrayOutputStream bao;
    private Handler mHandler;
    DatagramSocket datagramSocket;
    DatagramSocket recievingSocket;
volatile boolean isRuning = false;

    ServerConnection(Handler handler, ByteArrayOutputStream byteArrayOutputStream){
    mHandler = handler;
    bao = byteArrayOutputStream;

}
    public synchronized void sendMessageToMain(String msg, int incomingMsg) {
        Log.e(TAG, "sending message to main act: " + msg);

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);
        messageBundle.putInt(null,incomingMsg);
        Message message = new Message();
        message.setData(messageBundle);
        mHandler.sendMessage(message);

    }

    @Override
public void run(){
        sendDatagram();
    }
void sendDatagram(){
    try {
        Log.e(TAG, "sending thread started");

        /**
         * Create a server socket and wait for client connections. This
         * call blocks until a connection is accepted from a client
         */
        //ServerSocket serverSocket = new ServerSocket(8888);
        //Socket client = serverSocket.accept();
        InetAddress inetAddress = InetAddress.getByName("192.168.49.255");
        datagramSocket = new DatagramSocket(8888);
        sendMessageToMain("SocketInitialized", 0); //to start preview callbacks
        RecievingThread recievingThread = new RecievingThread();
        recievingThread.start();
        while(isRuning){
            synchronized (bao)
            {
            if (bao!=null) {
           // Log.e(TAG, "bao not null");

            if (bao.size() > 0) {
              //  Log.e(TAG, "bao size: " + bao.size());

                byte[] data = bao.toByteArray();
             //   byte[] size = (Integer.toString(data.length)).getBytes();
            //    Log.e(TAG, "size array length: " + size.length);

              // DatagramPacket packetSize = new DatagramPacket(size, size.length, inetAddress, 8888);
//datagramSocket.send(packetSize);
                DatagramPacket packet = new DatagramPacket(data, data.length, inetAddress, 8888);
                datagramSocket.send(packet);
             //   Log.e(TAG, "sending package, length: " + data.length);
                bao.reset();
            }
            }
        } }



        /**
         * If this code is reached, a client has connected and transferred data
         * Save the input stream from the client as a JPEG file
         */
        // dao = new DataOutputStream (client.getOutputStream());
    } catch (IOException e) {
        Log.e(TAG, e.getMessage());

    }

}
public class RecievingThread extends Thread {
    @Override
    public void run(){
       recieveDatagram();
    }
    void recieveDatagram(){

        int recieverPort = 8889;// intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
        int SOCKET_TIMEOUT = 1000;
      //  DatagramSocket clientSocket = null;

try {
    recievingSocket = new DatagramSocket(recieverPort);

}catch (SocketException e){
    e.printStackTrace();
}
        Log.d(TAG, "Opened recieving socket - ");

try {


                InetAddress address = InetAddress.getByName("192.168.49.1");
            // clientSocket.joinGroup(address);

            Log.d(TAG, "Client socket is bound:  " + recievingSocket.isBound());//socket.isConnected());
            byte[] buf = new byte[512];
            byte[] sizeBuf = new byte[20];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            DatagramPacket packetSize = new DatagramPacket(sizeBuf, sizeBuf.length);



            while (isRuning) {
                //  clientSocket.receive(packetSize);
                // byte[] sizeBytes = packetSize.getLength()
                recievingSocket.receive(packet);
                byte[] data = packet.getData();
                Log.d(TAG, "data recieved : "+data.length+"getLength = "+packet.getLength() );


                if (data != null) {

                    int quality = data[JPGQLTY_B];
                    int frames = data[FRAMERATE_DIVIDER];
                     Log.e(TAG, "Recieved package : "+quality);
                    // bitmap = recievedBitmap;
//recievedBitmap.recycle();
                    sendMessageToMain("int", quality);
                    sendMessageToMain("framerate", frames);

                } else {
                    Log.d(TAG, "null recieved");
                    // break;
                }
            }

        }
        catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (recievingSocket != null) {



            }
        }


    }


}
}
