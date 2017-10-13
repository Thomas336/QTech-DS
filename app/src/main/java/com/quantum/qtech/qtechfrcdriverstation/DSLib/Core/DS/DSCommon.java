package com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS;

public class DSCommon {
    public static int DS_DISABLED_PORT = -1;

    public static String getStaticIP(int net, int team, int host) {
        String str = Integer.toString(team);
        System.out.println(str);
        switch(str.length()) {
            case 1:
                str = "00.0" + str;
                break;
            case 2:
                str = "00." + str;
                break;
            case 3:
                str = "0"+str.charAt(0)+"."+str.charAt(1)+str.charAt(2);
                break;
            case 4:
                str = str.substring(0, 1)+str.substring(1,2)+"."+str.substring(2, 3)+str.substring(3,4);
                break;
        }
        return net+"."+str+"."+host;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static String timezone() {
        return "EST5EDT"; //I cheated
    }

//    public static QByteArray readSocket(QUdpSocket socket) {
//        byte[] data = new byte[0];
//
//        if(socket != null) {
//            if(socket.hasPendingDatagrams()) {
//                data = new byte[(int) socket.pendingDatagramSize()];
//                while (socket.hasPendingDatagrams()) {
//                    socket.readDatagram(data);
//                }
//            }
//        }
//
//        return new QByteArray(data);
//    }

//    public static QByteArray readSocket(QTcpSocket socket) {
//        if(socket != null)
//            return socket.readAll();
//
//        return new QByteArray("");
//    }
}
