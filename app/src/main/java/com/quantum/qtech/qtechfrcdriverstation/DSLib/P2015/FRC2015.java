package com.quantum.qtech.qtechfrcdriverstation.DSLib.P2015;

import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.Alliance;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.DSCommon;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.DSConfig;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.Joystick;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.Position;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.SocketType;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.Protocol;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.DriverStation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FRC2015 extends Protocol {
    boolean restartCode, rebootRobot, sendDateTime;
    public FRC2015() {
        restartCode = false;
        rebootRobot = false;
        sendDateTime = false;
    }

    @Override
    public String name() {
        return "FRC 2015 Protocol";
    }

    @Override
    public int robotFrequency() {
        return 50;
    }

    @Override
    public int robotInputPort() {
        return 1150;
    }

    @Override
    public int robotOutputPort() {
        return 1110;
    }

    @Override
    public int maxPOVCount() {
        return 1;
    }

    @Override
    public int maxAxisCount() {
        return 6;
    }

    @Override
    public int maxButtonCount() {
        return 12;
    }

    @Override
    public int maxJoystickCount() {
        return 6;
    }

    @Override
    public void rebootRobot() {
        rebootRobot = true;
    }

    @Override
    public void restartRobotCode() {
        restartCode = true;
    }

    @Override
    public void onRobotWatchdogExpired() {
        restartCode = false;
        rebootRobot = false;
        sendDateTime = false;
    }

    @Override
    public float nominalBatteryVoltage() {
        return 12.8f;
    }

    @Override
    public float nominalBatteryAmperage() {
        return 17;
    }

    @Override
    public SocketType robotSocketType() {
        return SocketType.UDP;
    }

    @Override
    public String radioAddress() { //FIXME: get team number somehow...
        return DSCommon.getStaticIP(10, DSConfig.getInstance().team(), 1);
    }

    /**
     * Generates a packet that the DSCommon will send to the robot
     */
    @Override
    public byte[] getRobotPacket() {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write((byte)((sentRobotPackets() & 0xFF00) >> 8));
        data.write((byte) (sentRobotPackets() & 0x00FF));
        data.write(PDSPacketTags.TAG_GENERAL);
        data.write(getControlCode());
        data.write(getRequestCode());
        data.write(getTeamStationCode());
        try {
            data.write(sendDateTime ? getTimezoneData() : getJoystickData());
        } catch (IOException e) {
            data.write(0); //TODO: See below
        }

        return data.toByteArray();
    }

    @Override
    public boolean interpretRobotPacket(byte[] data) {
//        System.out.println("Decoding "+new String(DSCommon.bytesToHex(data)));
        if(data.length < 8) {
            System.out.println(name() + "Received an invalid robot packet");
            return false;
        }

        byte control = data[3];
        byte status = data[4];
        byte request = data[7];

        boolean hasCode = status == PRobotData.HAS_CODE;
        boolean eStopped = control == PControlMode.MODE_ESTOP;
        boolean brownout = control == PRobotData.BROWNOUT;

        config().setRobotCode(hasCode);
        config().setBrownout(brownout);
        sendDateTime = request == PRobotData.REQUEST_TIME;

        config().setEmergencyStop(eStopped);

        BatteryVoltage voltage = new BatteryVoltage();
        voltage.update(data[5], data[6]);
        config().updateVoltage(voltage.voltage);

        if(receivedRobotPackets() > 10)
            config().updateSimulated(voltage.voltage == 0);

        if(data.length > 8) {
            String ext = new String(data);
            byte[] tmp = ext.substring(7).getBytes();
            readExtended(tmp);
            tmp = null;
            System.gc();
        }
        System.gc();

        return true;
    }

    public byte[] getTimezoneData() {
        ByteArrayOutputStream data = new ByteArrayOutputStream();

        data.write((byte) 0x0B);

        Calendar cal = Calendar.getInstance();
        Date time = cal.getTime();
        data.write(PDSPacketTags.TAG_DATE);
        data.write((byte) 0); //MSecs
        data.write((byte) 0);
        data.write((byte) time.getSeconds());
        data.write((byte) time.getMinutes());
        data.write((byte) time.getHours());
        data.write((byte) time.getDay());
        data.write((byte) time.getMonth());
        data.write((byte) (time.getYear() - 1900));

        data.write((byte) (DSCommon.timezone().length() + 1));
        data.write(PDSPacketTags.TAG_TIMEZONE);
        try {
            data.write(DSCommon.timezone().getBytes());
        } catch (IOException e) {
            data.write(0); //TODO: This may kill but itl be obvious when it does(you are getting an error arnt you...)
        }

        return data.toByteArray();
    }

    public byte[] getJoystickData() {
        ByteArrayOutputStream data = new ByteArrayOutputStream();

        if(sentRobotPackets() <= 5)
            return data.toByteArray();

        for(int i=0; i < joysticks().size(); i++) {
            int numAxes =       joysticks().get(i).numAxes;
            int numButtons =    joysticks().get(i).numButtons;
            int numPOVs =       joysticks().get(i).numPOVs;

            data.write(getJoystickSize(joysticks().get(i))-1);
            data.write(PDSPacketTags.TAG_JOYSTICK);

            data.write((byte) numAxes);
            for(int axis=0; axis<numAxes; axis++)
                data.write((byte) (joysticks().get(i).axes[axis] * 127));

            int buttonData = 0;
            for(int btn=0; btn<numButtons; btn++)
                buttonData += joysticks().get(i).buttons[btn] ? Math.pow(2, btn) : 0;

            data.write((byte) numButtons);
            data.write((byte) (((byte) buttonData & (byte) 0xff00) >> 8));
            data.write((byte) ((byte) buttonData & (byte) 0xff));

            data.write((byte) numPOVs);
            for(int hat=0; hat<numPOVs; hat++) {
                data.write((byte) (((byte) joysticks().get(i).povs[hat] & (byte) 0xff00) >> 8));
                data.write((byte)((byte)joysticks().get(i).povs[hat] & (byte)0xff));
            }
        }

        System.out.println(DSCommon.bytesToHex(data.toByteArray()));
        return data.toByteArray();
    }

    public Alliance getAlliance(byte station) {
        if(station == PStations.Blue1
                || station == PStations.Blue2
                || station == PStations.Blue3)
            return Alliance.BLUE;
        return Alliance.RED;
    }

    public Position getPosition(byte station) {
        if(station == PStations.Red1 || station == PStations.Blue1)
            return Position.P1;
        if(station == PStations.Red2 || station == PStations.Blue2)
            return Position.P2;
        if(station == PStations.Red3 || station == PStations.Blue3)
            return Position.P3;
        return Position.P1;
    }

    public void readExtended (byte[] data) {
//        System.out.println("Extendng "+new String(DSCommon.bytesToHex(data)));
        if(data.length==0 || data.length < 2)
            return;

        byte tag = data[0];

        if(tag == PRobotExtendedTags.TAG_JOYSTIK) {
            System.out.println("JOY");
            /* TODO */
        }

        else if (tag == PRobotExtendedTags.TAG_CPU) {
            System.out.println("CPU");
            int count = data[1];
            for (int i = 0; i < count; ++i)
                if (data.length > i + 12)
                    config().updateCpuUsage(data[i + 12]);
        }

        else if (tag == PRobotExtendedTags.TAG_MEM) {
            System.out.println("MEM");
            if (data.length > 5)
                config().updateRamUsage(data[5]);
        }

        else if (tag == PRobotExtendedTags.TAG_DISK) {
            System.out.println("CAN");
            if (data.length > 5)
                config().updateDiskUsage(data[5]);
        }
    }

    byte getControlCode() {
        byte code = 0;

        switch (config().controlMode()) {
            case TEST:
                code |= PControlMode.MODE_TEST;
                break;
            case AUTO:
                code |= PControlMode.MODE_AUTO;
                break;
            case TELEOP:
                code |= PControlMode.MODE_TELEOP;
                break;
            default:
                break;
        }

        if (config().isEmergencyStopped())
            code |= PControlMode.MODE_ESTOP;

        if (config().isEnabled())
            code |= PControlMode.MODE_ENABLED;

        return code;
    }

    public byte getRequestCode() {
        byte code = PDSRequests.DS_REQUEST_UNCONNECTED;

        if(config().isConnectedToRobot())
            code = PDSRequests.DS_REQUEST_NORMAL;

        if(config().isConnectedToRobot() && rebootRobot)
            code |= PDSRequests.DS_REQUEST_REBOOT;

        if(config().isConnectedToRobot() && restartCode)
            code |= PDSRequests.DS_REQUEST_RESTART_CODE;

        return code;
    }

    byte getTeamStationCode() {
        if (config().position() == Position.P1)
            if (config().alliance() == Alliance.RED)
                return PStations.Red1;
            else
                return PStations.Blue1;

        if (config().position() == Position.P2)
            if (config().alliance() == Alliance.RED)
                return PStations.Red2;
            else
                return PStations.Blue2;

        if (config().position() == Position.P3)
            if (config().alliance() == Alliance.RED)
                return PStations.Red3;
            else
                return PStations.Blue3;

        return PStations.Red1;
    }

    public byte getJoystickSize(Joystick joystick) {
        return (byte)(5
                + (joystick.numAxes > 0 ? joystick.numAxes : 0)
                + (joystick.numButtons / 8)
                + (joystick.numButtons % 8 == 0 ? 0 : 1)
                + (joystick.numPOVs > 0 ? joystick.numPOVs * 2 : 0));
    }

    private List<Joystick> joysticks() { return DriverStation.getInstance().joysticks(); }
    private DSConfig config() {
        return DSConfig.getInstance();
    }
}
