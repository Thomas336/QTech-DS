package com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS;

import android.content.Context;
import android.os.AsyncTask;

import com.quantum.qtech.qtechfrcdriverstation.DSLib.DriverStation;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Util.Lookup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutionException;

public class Sockets {
    public interface SockListener {
        void radioPacketReceived(byte[] data);
        void robotPacketReceived(byte[] data);
    }

    SockListener listener;

    public void setSockListener(SockListener l) { listener = l; }

    private int radioOutputPort;
    private int robotOutputPort;

    private int radioLookupId;
    private int robotLookupId;

    private InetAddress robotAddress;
    private InetAddress radioAddress;

//    private Lookup radioLookup;
    private Lookup robotLookup;
    private DriverStation driverStation;

    private DatagramSocket udpRadioSender;
//    private QTcpSocket tcpRadioSender;
    private DatagramSocket udpRobotSender;
//    private QTcpSocket tcpRobotSender;
    private DatagramSocket udpRadioReceiver;
//    private QTcpSocket tcpRadioReceiver;
    private DatagramSocket udpRobotReceiver;
//    private QTcpSocket tcpRobotReceiver;
//    private QTcpServer tcpRadioServer;
//    private QTcpServer tcpRobotServer;

    byte[] buf;
    DatagramPacket data;

//    public void CONFIGURE_SOCKET(QAbstractSocket socket) {
//        if(socket != null) {
//            socke
//        }
//    }

    public String GET_CONSOLE_IP(String ip) {

        if(ip.isEmpty())
            ip = "Auto";

        return ip;
    }

    public Sockets() {
        String radioAddress = "";
        String robotAddress = "";

        udpRadioSender = null;
//        tcpRadioSender = null;
        udpRobotSender = null;
//        tcpRobotSender = null;
        udpRadioReceiver = null;
//        tcpRadioReceiver = null;
        udpRobotReceiver = null;
//        tcpRobotReceiver = null;

//        radioLookup = new Lookup(c);
//        robotLookup = new Lookup(c);
        driverStation = null;

        radioOutputPort = DSCommon.DS_DISABLED_PORT;
        robotOutputPort = DSCommon.DS_DISABLED_PORT;

//        radioLookup.setResolvedListener(new Lookup.LookupListener() {
//            @Override
//            public void found(String name, String address) {
//                onRadioLookupFinished(name, address);
//            }
//        });
//
//        robotLookup.setResolvedListener(new Lookup.LookupListener() {
//            @Override
//            public void found(String name, String address) {
//                onRobotLookupFinished(name, address);
//            }
//        });
    }

    public InetAddress radioAddress() {
        return radioAddress;
    }

    public InetAddress robotAddress() {
        if(robotAddress==null) {
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return robotAddress;
    }

    public void performLookup() {
        System.out.println("Lookup thread started");
        new Thread(new Runnable() {
            @Override
            public void run() {
//                while(true) {
//                    try {
                        if(driverStation == null)
                            driverStation = DriverStation.getInstance();

                        System.out.println("Lookup start");
                        if(radioAddress==null && !driverStation.isConnectedToRadio()) {
                            System.out.println("Lookup Radio");
//                            radioLookup.discover();//.lookup(driverStation.radioAddress()); }
                        }

                        if(robotAddress==null && !driverStation.isConnectedToRobot()) {
                            System.out.println("Lookup Robot");
                            robotLookup.discover();//.lookup(driverStation.robotAddress()); }
                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }, "mDNS_LOOKUP").start();
    }

    public void setRadioInputPort(int port) {
//        if(tcpRadioReceiver != null) {
//            tcpRadioServer.close();
//            tcpRadioServer.listen(new String("0.0.0.0"), port);
//        }
        /*else*/ if(udpRadioReceiver != null && port != -1) {
            udpRadioReceiver.close();
            InetSocketAddress address = new InetSocketAddress("0.0.0.0", port);
            try {
                udpRadioReceiver.bind(address);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRobotInputPort(int port) {
        System.out.println("Binding on "+port);
//        if(tcpRobotReceiver != null) {
//            tcpRobotServer.close();
//            tcpRobotServer.listen(new String("0.0.0.0"), port);
//        }
        /*else*/ if(!udpRobotReceiver.isBound()) {
//            udpRobotReceiver.disconnect();
//            udpRobotReceiver.close();
            try {
//                udpRobotReceiver = new DatagramSocket(null);
//                udpRobotReceiver.setSoTimeout(100);
//                udpRadioReceiver.setReuseAddress(true);
                udpRobotReceiver.bind (new InetSocketAddress(port));
                System.out.println("Bound");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRadioOutputPort(int port) {
        radioOutputPort = port;
//        udpRadioSender.connect(radioAddress(), port);
    }

    public void setRobotOutputPort(int port) {
        robotOutputPort = port;
    }

    public void sendToRobot(byte[] data) {
        if(data.length == 0)
            return;

//        if(tcpRobotSender != null)
//            tcpRobotSender.write(data);

        /*else*/ if(udpRobotSender!= null) {
            try {
//                System.out.println("Sent "+DSCommon.bytesToHex(data)+" to "+robotAddress()+":"+robotOutputPort);
                udpRobotSender.connect(robotAddress(), robotOutputPort);
                udpRobotSender.send(new DatagramPacket(data, data.length));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendToRadio(byte[] data) {
        if(data.length == 0)
            return;

//        if(tcpRadioSender != null)
//            tcpRadioSender.write(data);
        /*else*/ if(udpRadioSender != null)
            try {
                udpRadioSender.send(new DatagramPacket(data, data.length, robotAddress(), radioOutputPort));
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void setRadioSocketType(SocketType type) {
        udpRadioSender = null;
//        tcpRadioSender = null;
        udpRadioReceiver = null;
//        tcpRadioReceiver = null;
//        tcpRadioServer = null;

//        if(type == SocketType.TCP) {
//            tcpRadioSender = new QTcpSocket(this);
//            tcpRadioServer = new QTcpServer(this);
//            QTcpServer.Result r = tcpRadioServer.waitForNewConnection();
//            if(r == QTcpServer.Result.Success) {
//                tcpRadioReceiver = tcpRadioServer.nextPendingConnection();
//
////                CONFIGURE_SOCKET(tcpRadioSender);
////                CONFIGURE_SOCKET(tcpRadioReceiver);
//
//                tcpRadioReceiver.readyRead.connect(this, "readRadioSocket");
//            }
//        }
//        else
//        {
        try {
            udpRadioSender = new DatagramSocket();
            udpRadioReceiver = new DatagramSocket();

        } catch (SocketException e) {
            e.printStackTrace();
        }

//            CONFIGURE_SOCKET(udpRadioSender);
//            CONFIGURE_SOCKET(udpRadioReceiver);
//        Thread recvRadio = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                byte[] buf = new byte[1024];
//                DatagramPacket data = new DatagramPacket(buf, buf.length);
//                try {
//                    udpRadioReceiver.receive(data); //TODO: Is this blocking?
//                    setRadioAddress(data.getAddress());
//                    listener.radioPacketReceived(data.getData());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        recvRadio.start();
//            udpRadioReceiver.readyRead.connect(this, "readRadioScoket");
//        }
    }

    public void setRobotSocketType(SocketType type) {
        System.out.println("SetSockType");
        udpRobotSender = null;
//        tcpRobotSender = null;
        udpRobotReceiver = null;
//        tcpRobotReceiver = null;
//        tcpRobotServer = null;

//        if(type == SocketType.TCP) {
//            tcpRobotSender = new QTcpSocket(this);
//            tcpRobotServer = new QTcpServer(this);
//            QTcpServer.Result r = tcpRobotServer.waitForNewConnection();
//            if(r == QTcpServer.Result.Success) {
//                tcpRobotReceiver = tcpRobotServer.nextPendingConnection();
//
////                CONFIGURE_SOCKET(tcpRobotSender);
////                CONFIGURE_SOCKET(tcpRobotReceiver);
//
//                tcpRobotReceiver.readyRead.connect(this, "readRobotSocket");
//            }
//        }
//        else
//        {
//        System.out.println("Sender");
        try {
            udpRobotSender = new DatagramSocket();
            DatagramChannel channel = null;
            try {
                channel = DatagramChannel.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            udpRadioReceiver = channel.socket();
            assert channel != null;
            udpRobotReceiver = channel.socket();
            udpRobotReceiver.setReuseAddress(true);
            udpRobotReceiver.setSoTimeout(100);
        } catch (SocketException e) {
            e.printStackTrace();
        }

//            CONFIGURE_SOCKET(udpRobotSender);
//            CONFIGURE_SOCKET(udpRobotReceiver);

        Thread recvRobot = new Thread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("Recver");
                while(true) {
                    if (udpRobotReceiver != null) {
//                        try {
//                            System.out.println("Recving");
                        try {
                            buf = new byte[64];
                            data = new DatagramPacket(buf, buf.length);
                            data.setLength(buf.length);
                            udpRobotReceiver.receive(data); //TODO: Is this blocking?
                            listener.robotPacketReceived(data.getData());
                            buf = null;
                            data = null;
                            System.gc();
                        } catch (IOException e) {
                        }
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "ROBOT_RECV");
        recvRobot.start();
//        }
    }

    public void setRadioAddress(InetAddress address) {
        String ip = address.toString();

        if(ip.isEmpty() && address.isAnyLocalAddress()) //TODO: Wrong
            System.out.println("FMS address "+address+"scheduled for lookup");

        setRadioAddress(ip);
    }

    public void setRobotAddress(InetAddress address) {
        String ip = address.toString();

        if(ip.isEmpty() && address.isAnyLocalAddress()) //TODO: Also wrong
            System.out.println("Robot address "+address+" scheduled for lookup");

        setRobotAddress(ip);
    }

    public void setRadioAddress(final String address) {
        InetAddress addr = null;
        System.out.println(address);
        try {
            addr = new AsyncTask<String, Void, InetAddress>() {
                @Override
                protected InetAddress doInBackground(String... params) {
                    try {
                        return InetAddress.getByAddress(InetAddress.getByName(address).getAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
//            addr = InetAddress.getByName(address);
        if(radioAddress != addr && !address.isEmpty()) {
            radioAddress = addr;
            System.out.println("Radio address set to "+GET_CONSOLE_IP(address));
        }
    }

    public void setRobotAddress(final String address) {
        InetAddress addr = null;
        System.out.println(address);
        try {
            addr = new AsyncTask<String, Void, InetAddress>() {
                @Override
                protected InetAddress doInBackground(String... params) {
                    try {
                        return InetAddress.getByAddress(InetAddress.getByName(address).getAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        return null;
                    }
                }


            }.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if(robotAddress != addr && !address.isEmpty()) {
            robotAddress = addr;
            System.out.println("Robot address set to "+GET_CONSOLE_IP(address));
        }
    }

    public void onRadioLookupFinished(String name, String address) {
        System.out.println(name+", "+address);
        if(radioAddress!=null && !address.isEmpty() && name.toLowerCase().equals(driverStation.radioAddress().toLowerCase()))
            setRadioAddress(address);
    }

    public void onRobotLookupFinished(String name, String address) {
        System.out.println(name + ", " + address);
        if(robotAddress!=null && !address.isEmpty()) // && name.toLowerCase() == driverStation.robotAddress().toLowerCase()
            setRobotAddress(address);
    }
}
