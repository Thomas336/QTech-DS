package com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class NetConsole {
    int outputPort;

    DatagramSocket inputSocket;
    DatagramSocket outputSocket;

    public interface NetConsoleListener {
        void newMessage(String msg);
    }

    static NetConsoleListener listener;

    public void setNewMessageListener(NetConsoleListener l) {
        listener = l;
    }


    public NetConsole() {
        outputPort = 0;

        Thread recvRadio = new Thread(new Runnable() {
            @Override
            public void run() {
                if(inputSocket != null) {
                    byte[] buf = new byte[1024];
                    DatagramPacket data = new DatagramPacket(buf, buf.length);
                    try {
                        inputSocket.receive(data); //TODO: Is this blocking?
                        recv(data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        recvRadio.start();
    }

    private void recv(byte[] data) {
        listener.newMessage(new String(data));
    }

    public void setInputPort(int port) {
        if(port != DSCommon.DS_DISABLED_PORT)
            try {
                inputSocket.setBroadcast(true);
                inputSocket.bind(new InetSocketAddress("255.255.255.255", port));
            } catch (SocketException e) {
                e.printStackTrace();
            }
    }

    public void setOutputPort(int port) {
        outputPort = port;
    }

    public void sendMessage(String message) {
        if(!message.isEmpty() && outputPort != DSCommon.DS_DISABLED_PORT) {
            byte[] msg = message.getBytes();
            try {
                outputSocket.setBroadcast(true);
                outputSocket.send(new DatagramPacket(msg, msg.length, new InetSocketAddress("255.255.255.255", outputPort)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
