package com.quantum.qtech.qtechfrcdriverstation.DSLib.Core;

import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.CommStatus;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.DSCommon;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.DSConfig;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.SocketType;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.DriverStation;

public class Protocol {

    int sentRadioPackets = 0;
    int sentRobotPackets = 0;
    int receivedRadioPackets = 0;
    int receivedRobotPackets = 0;

    int sentRobotPacketsSinceConnect = 0;
    int recvRobotPacketsSinceConnect = 0;

    public String name() {
        return "Generic Protocol";
    }

    public int radioFrequency() {
        return 1;
    }

    public int robotFrequency() {
        return 1;
    }

    public int maxJoystickCount() {
        return 6;
    }

    public int maxPOVCount() {
        return 12;
    }

    public int maxAxisCount() {
        return 12;
    }

    public int maxButtonCount() {
        return 24;
    }

    public int radioInputPort() {
        return DSCommon.DS_DISABLED_PORT;
    }

    public int radioOutputPort() {
        return DSCommon.DS_DISABLED_PORT;
    }

    public int robotInputPort() {
        return DSCommon.DS_DISABLED_PORT;
    }

    public int robotOutputPort() {
        return DSCommon.DS_DISABLED_PORT;
    }

    public int netconsoleInputPort() {
        return DSCommon.DS_DISABLED_PORT;
    }

    public int netconsoleOutputPort() {
        return DSCommon.DS_DISABLED_PORT;
    }

    public float nominalBatteryVoltage() {
        return 12.8f;
    }

    public float nominalBatteryAmperage() {
        return 17;
    }

    public void rebootRobot() {}

    public void restartRobotCode() {}

    public void onRadioWatchdogExpired() {}

    public void onRobotWatchdogExpired() {}

    public SocketType radioSocketType() {
        return SocketType.UDP;
    }

    public SocketType robotSocketType() {
        return SocketType.UDP;
    }

    public String radioAddress() {
        return DSCommon.getStaticIP(10, config().team(), 1);
    }

    public String robotAddress() {
        return DSCommon.getStaticIP(10, config().team(), 2);
    }

    public byte[] generateRadioPacket() {
        sentRadioPackets++;
        return getRadioPacket();
    }

    public byte[] generateRobotpacket() {
        sentRobotPackets++;
        sentRobotPacketsSinceConnect++;
        return getRobotPacket();
    }

    public boolean readRadioPacket(byte[] data) {
        receivedRadioPackets++;

        if(interpretRadioPacket(data)) {
            config().updateRadioCommStatus(CommStatus.RUNNING);
            return true;
        }
        return false;
    }

    public boolean readRobotPacket(byte[] data) {
        receivedRadioPackets++;
        recvRobotPacketsSinceConnect++;

        if(interpretRobotPacket(data)) {
            if(!config().isConnectedToRobot())
                resetLossCounter();

            config().updateRobotCommStatus(CommStatus.RUNNING);
            return true;
        }
        return false;
    }

    public void resetLossCounter() {
        sentRobotPacketsSinceConnect = 0;
        recvRobotPacketsSinceConnect= 0;
    }

    public int sentRadioPackets() {
        return sentRadioPackets;
    }

    public int sentRobotPackets() {
        return sentRobotPackets;
    }

    public void resetRobotPackets() {
        sentRobotPackets = 0;
    }

    public int receivedRadioPackets() {
        return receivedRadioPackets;
    }

    public int receivedRobotPackets() {
        return receivedRobotPackets;
    }

    public int recvRobotPacketsSinceConnect() {
        return recvRobotPacketsSinceConnect;
    }

    public int sentRobotPacketsSinceConnect() {
        return sentRobotPacketsSinceConnect;
    }

    private DSConfig config() {
        return DSConfig.getInstance();
    }

    public byte[] getRadioPacket() {
        return new byte[0];
    }

    public byte[] getRobotPacket() {
        return new byte[0];
    }

    public boolean interpretRadioPacket(byte[] data) {
        return false;
    }

    public boolean interpretRobotPacket(byte[] data) {
        return false;
    }
}
