package com.quantum.qtech.qtechfrcdriverstation.DSLib;

import android.content.Context;
import android.os.Handler;

import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.Alliance;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.CodeStatus;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.CommStatus;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.DSConfig;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.DSControlMode;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.EnableStatus;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.Joystick;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.NetConsole;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.OperationStatus;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.Position;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.Sockets;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.VoltageStatus;
//import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.Watchdog;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.Protocol;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.P2015.FRC2015;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.P2016.FRC2016;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class DriverStation {
    private static DriverStation ourInstance = new DriverStation();

    public static DriverStation getInstance() {
        return ourInstance;
    }

    public interface DsListener {
        void resetted();
        void initialized();
        void logFileChanged();
        void protocolChanged();
        void joystickCountChanged(int count);
        void newMessage(String msg);
        void statusChanged(String status);
    }

    DsListener listener;

    public void setDSListener(DsListener l) { listener = l; }

    boolean init;
    boolean running;

    int packetLoss;
    int radioInterval;
    int robotInterval;

    String logPath;

    List<Joystick> joysticks = new ArrayList<Joystick>();
    File log;
    String customRadioAddress;
    String customRobotAddress;

    Sockets sockets;
    Protocol protocol;
    NetConsole console;

    Watchdog radioWatchdog;
    Watchdog robotWatchdog;

    DSConfig config;

    public DriverStation() {
        System.out.println("Initializing DriverStation...");

        init = false;
        running = false;
        protocol = null;

        packetLoss = 0;
        radioInterval = 1000;
        robotInterval = 1000;

        customRadioAddress = "";
        customRobotAddress = "";

        console = new NetConsole();
        sockets = new Sockets();

        //Connect shit?

        radioWatchdog = new Watchdog(1000);
        robotWatchdog = new Watchdog(1000);

        //More connect shit?
    }

    public static String consoleMessage(String in) {
        return "<font color='#888'>**"+in+"</font>";
    }

    public float range(float in, float max, float min) {
        if(in > max)
            return max;
        if(in < min)
            return min;
        return in;
    }

    public boolean canBeEnabled() {
        return isConnectedToRobot() && isRobotCodeRunning() && !isEmergencyStopped();
    }

    public boolean running() {
        return running;
    }

    public boolean isInTest() {
        return controlMode() == DSControlMode.TEST;
    }

    public boolean isEnabled() {
        return enableStatus() == EnableStatus.ENABLED;
    }

    public boolean isSimulated() {
        return config().isSimulated();
    }

    public boolean inInAutonomous() {
        return controlMode() == DSControlMode.AUTO;
    }

    public boolean inInTeleoperated() {
        return controlMode() == DSControlMode.TELEOP;
    }

    public boolean isVoltageBrownout() {
        return voltageStatus() == VoltageStatus.BROWNOUT;
    }

    public boolean isEmergencyStopped() {
        return operationStatus() == OperationStatus.ESTOP;
    }

    public boolean isConnectedToRobot() {
        return robotCommStatus() == CommStatus.RUNNING;
    }

    public boolean isConnectedToRadio() {
        return radioCommStatus() == CommStatus.RUNNING;
    }

    public boolean isRobotCodeRunning() {
        return robotCodeStatus() == CodeStatus.RUNNING;
    }

//    public String logPath() {
//        return config().logger().logsPath();
//    }

//    public

//    public List<String>

    public float maxBatteryVoltage() {
        if(protocol() != null)
            return protocol().nominalBatteryVoltage();

        return 0;
    }

    public float currentBatteryVoltage() {
        return config().voltage();
    }

    public float nominalBatteryAmperage() {
        if(protocol() != null)
            return protocol().nominalBatteryAmperage();

        return 0;
    }

    public int team() {
        return config().team();
    }

    public int cpuUsage() {
        return config().cpuUsage();
    }

    public int ramUsage() {
        return config().ramUsage();
    }

    public int diskUsage() {
        return config().diskUsage();
    }

    public int packetLoss() {
        return packetLoss;
    }

    public int maxPOVCount() {
        if(protocol() != null)
            return protocol().maxPOVCount();

        return 0;
    }

    public int maxAxisCount() {
        if(protocol() != null)
            return protocol().maxAxisCount();

        return 0;
    }

    public int maxButtonCount() {
        if(protocol() != null)
            return protocol().maxButtonCount();

        return 0;
    }

    public int maxJoystickCount() {
        if(protocol() != null)
            return protocol().maxJoystickCount();

        return 0;
    }

    public int getNumAxes(int joystick) {
        if(joysticks().size() > joystick)
            return joysticks().get(joystick).numAxes;

        return 0;
    }

    public int getNumPOVs(int joystick) {
        if(joysticks().size() > joystick)
            return joysticks().get(joystick).numPOVs;

        return 0;
    }

    public int getNumButtons(int joystick) {
        if(joysticks().size() > joystick)
            return joysticks().get(joystick).numButtons;

        return 0;
    }

    public int getRealNumAxes(int joystick) {
        if(joysticks().size() > joystick)
            return joysticks().get(joystick).realNumAxes;

        return 0;
    }

    public int getRealNumPOVs(int joystick) {
        if(joysticks().size() > joystick)
            return joysticks().get(joystick).realNumPOVs;

        return 0;
    }

    public int getRealNumButtons(int joystick) {
        if(joysticks().size() > joystick)
            return joysticks().get(joystick).realNumButtons;

        return 0;
    }

    public int joystickCount() {
        return joysticks().size();
    }

    public List<Joystick> joysticks() {
        return joysticks;
    }

    public Alliance alliance() {
        return config().alliance();
    }

    public Position position() {
        return config().position();
    }

    public DSControlMode controlMode() {
        return config().controlMode();
    }

    public EnableStatus enableStatus() {
        return config().enableStatus();
    }

    public CommStatus radioCommStatus() {
        return config().radioCommStatus();
    }

    public CommStatus robotCommStatus() {
        return config().robotCommStatus();
    }

    public CodeStatus robotCodeStatus() {
        return config().robotCodeStatus();
    }

    public VoltageStatus voltageStatus() {
        return config().voltageStatus();
    }

    public String radioAddress() {
        if(customRadioAddress().isEmpty())
            return defaultRadioAddress();

        return customRadioAddress();
    }

    public String robotAddress() {
        if(customRobotAddress().isEmpty())
            return defaultRobotAddress();
        System.out.println("Using custom addr");
        return customRobotAddress();
    }

    public String generalStatus() {
//        if(!isConnectedToRobot())
//            return "No Robot Communication";
//        else if(!isRobotCodeRunning())
//            return "No Robot Code";
//        else if(!isVoltageBrownout())
//            return "VoltageBrownout";

        String mode = "";
        String enabled = "";

        switch(controlMode()) {
            case TEST:
                mode = "Test";
                break;
            case AUTO:
                mode = "Auto";
                break;
            case TELEOP:
                mode = "Teleop";
                break;
        }

        switch(enableStatus()) {
            case ENABLED:
                enabled = "Enabled";
                break;
            case DISABLED:
                enabled = "Disabled";
                break;
        }

        return mode + " " + enabled;
    }

    public String customRadioAddress() {
        return customRadioAddress;
    }

    public String customRobotAddress() {
        return customRobotAddress;
    }

    public String defaultRadioAddress() {
        if(protocol() != null)
            return protocol().radioAddress();

        return "";
    }

    public String defaultRobotAddress(){
        if(protocol() != null)
            return protocol().robotAddress();

        return "";
    }

    public OperationStatus operationStatus() {
        return config().operationStatus();
    }

    public List<String> protocols() {
        List<String> list = new ArrayList<String>();
        list.add("FRC 2016");
        list.add("FRC 2015");
        list.add("FRC 2014");
        return list;
    }

    public List<String> teamStations(){
        List<String> list = new ArrayList<String>();
        list.add("Red 1");
        list.add("Red 2");
        list.add("Red 3");
        list.add("Blue 1");
        list.add("Blue 2");
        list.add("Blue 3");
        return list;
    }

    public boolean registerJoystick(int axes,
                                    int buttons,
                                    int povs) {
        System.out.println("Trying to register joystick with " + axes + " axes, " + buttons + " buttons and, " + povs + " POVs...");

        if(axes <= 0 && buttons <= 0 && povs <= 0) {
            System.err.println("How do you expect me to add nothing... bastard!");
            return false;
        }
        else if(joystickCount()+1 > maxJoystickCount()) {
            System.err.println("The number of joysticks is TO DAMN HIGH!");
            return false;
        }
        else
        {
            Joystick joystick = new Joystick();

            joystick.realNumAxes = axes;
            joystick.realNumButtons = buttons;
            joystick.realNumPOVs = povs;

            joystick.numAxes = Math.min(axes, maxAxisCount());
            joystick.numButtons = Math.min(buttons, maxButtonCount());
            joystick.numPOVs = Math.min(povs, maxPOVCount());

            joystick.axes = new float[joystick.numAxes];
            joystick.buttons = new boolean[joystick.numButtons];
            joystick.povs = new int[joystick.numPOVs];

            for(int i=0; i < joystick.numAxes; i++) {
                joystick.axes[i] = 0;
            }
            for(int i=0; i < joystick.numButtons; i++) {
                joystick.buttons[i] = false;
            }
            for(int i=0; i < joystick.numPOVs; i++) {
                joystick.povs[i] = -1;
            }

            System.out.println("Registered joystick with " + joystick.numAxes + " axes, " + joystick.numButtons + " buttons and, " + joystick.numPOVs + " POVs...");
            joysticks().add(joystick);

//            listener.joystickCountChanged(joystickCount());
            return true;

        }
    }

    public void init() {
        if(!init) {
            init = true;

//            config().logger().registerInitialEvents();

            resetRadio();
            resetRobot();
            sendRadioPacket();
            sendRobotPacket();
            updatePacketLoss();

            //DSSchedule(250, this, SLOT finishInit());???

            System.out.println("DS Started");
        }
    }

    public void rebootRobot() {
        if(protocol() != null) {
            protocol().rebootRobot();
            System.out.println("Robot reboot triggered by DS...");
        }
    }

    public void enableRobot() {
        setEnabled(EnableStatus.ENABLED);
    }

    public void disableRobot() {
        setEnabled(EnableStatus.DISABLED);
    }

    public void resetJoysticks() {
        System.out.println("Resetting all joysticks");
        for(Joystick stick : joysticks()) {
            for(int i=0; i < stick.numAxes; i++) {
                stick.axes[i] = 0;
            }
            for(int i=0; i < stick.numButtons; i++) {
                stick.buttons[i] = false;
            }
            for(int i=0; i < stick.numPOVs; i++) {
                stick.povs[i] = 0;
            }
        }
//        listener.joystickCountChanged(joystickCount());
    }

    public void setTeam(int team) {
        System.out.println("DS Team" + team);
        config().updateTeam(team);
    }

    public void restartRobotCode() {
        if(protocol() != null) {
            protocol().restartRobotCode();
            System.out.println("Robot code restart triggered by DS...");
        }
    }

    public void switchToTestMode() {
        setEnabled(EnableStatus.DISABLED);
        setControlMode(DSControlMode.TEST);
    }

    public void switchToAutonomous() {
        setEnabled(EnableStatus.DISABLED);
        setControlMode(DSControlMode.AUTO);
    }

    public void switchToTeleoperated() {
        setEnabled(EnableStatus.DISABLED);
        setControlMode(DSControlMode.TELEOP);
    }

    public void triggerEmergencyStop() {
        setEnabled(EnableStatus.DISABLED);
        setOperationStatus(OperationStatus.ESTOP);
    }

    public void reconfigureJoysticks() {
        List<Joystick> list = joysticks;
//        resetJoysticks();

//        for (Joystick joystick:list) {
//            registerJoystick(joystick.realNumAxes,
//                            joystick.realNumButtons,
//                            joystick.realNumPOVs);
//        }
    }

    public void removeJoystick(int id) {
        if(joystickCount() > id) {
            joysticks().remove(id);
        }

//        listener.joystickCountChanged(joystickCount());
    }

    public void setEnabled(boolean enabled) {
        setEnabled(enabled ? EnableStatus.ENABLED : EnableStatus.DISABLED);
    }

    public void setTeamStation(TeamStation station) {
        switch(station) {
            case RED1:
                setPosition(Position.P1);
                setAlliance(Alliance.RED);
                break;
            case RED2:
                setPosition(Position.P2);
                setAlliance(Alliance.RED);
                break;
            case RED3:
                setPosition(Position.P3);
                setAlliance(Alliance.RED);
                break;
            case BLUE1:
                setPosition(Position.P1);
                setAlliance(Alliance.BLUE);
                break;
            case BLUE2:
                setPosition(Position.P2);
                setAlliance(Alliance.BLUE);
                break;
            case BLUE3:
                setPosition(Position.P3);
                setAlliance(Alliance.BLUE);
                break;
        }
    }

    public void setPosition(Position position) {
        config().updatePosition(position);
    }

    public void setAlliance(Alliance alliance) {
        config().updateAlliance(alliance);
    }

    public void setProtocolType(ProtocolType protocol) {
        if(protocol == ProtocolType.FRC2016)
            setProtocol(new FRC2016());
        if(protocol == ProtocolType.FRC2015)
            setProtocol(new FRC2015());
//        if(protocol == ProtocolType.FRC2014)
//            setProtocol(new FRC2014());
    }

    public void setProtocol(Protocol proto) {
        if (protocol == proto) {
            System.out.println("Protocol " + protocol.name() + " unset");
            listener.newMessage(consoleMessage("DS: " + protocol.name() + " terminated"));
            protocol = null;
        }

        stop();
        protocol = proto;

        if (protocol != null) {
            System.out.println("Configuring new protocol: " + protocol.name());

            sockets.setRadioSocketType(protocol.radioSocketType());
            sockets.setRobotSocketType(protocol.robotSocketType());

            sockets.setRadioInputPort(protocol.radioInputPort());
            sockets.setRadioOutputPort(protocol.radioOutputPort());
            sockets.setRobotInputPort(protocol.robotInputPort());
            sockets.setRobotOutputPort(protocol.robotOutputPort());

//            sockets.performLookup();

            sockets.setSockListener(new Sockets.SockListener() {
                @Override
                public void radioPacketReceived(byte[] data) {
                    protocol().readRadioPacket(data);
                }

                @Override
                public void robotPacketReceived(byte[] data) {
                    protocol().readRobotPacket(data);
                }
            });

            console.setInputPort(protocol.netconsoleInputPort());
            console.setOutputPort(protocol.netconsoleOutputPort());

            radioInterval = 1000 / protocol.radioFrequency();
            robotInterval = 1000 / protocol.robotFrequency();

            radioWatchdog = new Watchdog(radioInterval * 50);
            robotWatchdog = new Watchdog(robotInterval * 50);

            radioInterval -= (float) radioInterval * 0.1;
            robotInterval -= (float) robotInterval * 0.1;

            reconfigureJoysticks();

            updateAddresses();

            start();
            resetRadio();
            resetRobot();

//            listener.protocolChanged();
//            listener.newMessage(consoleMessage("DS: " + protocol.name() + " initialized"));

            System.out.println("Protocol " + protocol.name() + " ready for use");
        }
    }

    public void updatePOV(int id, int pov, int angle) {
        if(joysticks().size() > Math.abs(id))
            if(joysticks().get(id).numPOVs > pov)
                joysticks().get(id).povs[Math.abs(pov)] = angle;
    }

    public void updateAxis(int id, int axis, float value) {
        System.out.println("Btn "+id+" "+axis+" "+value);
        if(joysticks().size() > Math.abs(id)) {
            if(joysticks().get(id).numAxes > axis)
                joysticks().get(id).axes[Math.abs(axis)] = range(value, 1, -1);
        }
    }

    public void updateButton(int id, int button, boolean state) {
        System.out.println("Btn "+id+" "+button+" "+state);
        if(joysticks().size() > Math.abs(id)) {
            if(joysticks().get(id).numButtons > button)
                joysticks().get(id).buttons[Math.abs(button)] = state;
        }
    }

    public void setCustomRadioAddress(String address) {
        customRadioAddress = address;
        sockets.setRadioAddress(radioAddress());
    }

    public void setCustomRobotAddress(String address) {
        customRobotAddress = address;
        sockets.setRobotAddress(address);
    }

    public void setControlMode(DSControlMode mode) {
        config().updateControlMode(mode);
    }

    public void setEnabled(EnableStatus status) {
        config().updateEnabled(status);
    }

    public void setOperationStatus(OperationStatus status) {
        config().updateOperationStatus(status);
    }

    public void stop() {
        running = false;
        System.out.println("DS networking operations stopped");
    }

    public void start() {
        running = true;
        System.out.println("DS networking operations resumed");
    }

    public void resetRadio() {
        if(protocol() != null)
            protocol().onRadioWatchdogExpired();

        config().updateRadioCommStatus(CommStatus.FAILING);
    }

    public void resetRobot() {
        if(protocol() != null) {
            protocol().resetLossCounter();
            protocol.onRobotWatchdogExpired();
        }

        config().updateVoltage(0);
        config().updateSimulated(false);
        config().updateEnabled(EnableStatus.DISABLED);
        config().updateOperationStatus(OperationStatus.NORMAL);
        config().updateVoltageStatus(VoltageStatus.NORMAL);
        config().updateRobotCodeStatus(CodeStatus.FAILING);
        config().updateRobotCommStatus(CommStatus.FAILING);
        protocol().resetRobotPackets();

//        listener.statusChanged(generalStatus());
    }

    public void finishedInit() {
//        listener.initialized();;
//        listener.statusChanged(generalStatus());
    }

    public void updateAddresses() {
        sockets.setRadioAddress(radioAddress());
        sockets.setRobotAddress(robotAddress());
    }

    public void sendRadioPacket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (protocol() != null && running())
                        sockets.sendToRadio(protocol().generateRadioPacket());
                    try {
                        Thread.sleep(radioInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "RADIO_SENDER").start();

//        final Handler h = new Handler();
//        h.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                sendRadioPacket();
//                h.postDelayed(this, radioInterval);
//            }
//        }, radioInterval);
//        QTimer.singleShot(radioInterval, this, "sendRobotPacket");
    }

    public void sendRobotPacket() {

        final Handler h = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (protocol() != null && running())
                    sockets.sendToRobot(protocol().generateRobotpacket());
                    try {
                        Thread.sleep(robotInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "ROBOT_SENDER").start();
//        h.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                sendRobotPacket();
//                h.postDelayed(this, robotInterval);
//            }
//        }, robotInterval);
//        QTimer.singleShot(robotInterval, this, "sendRobotPacket");
    }

    public void updatePacketLoss() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    float loss = 0;
                    float sentPackets = 0;
                    float recvPackets = 0;

                    if (protocol() != null) {
                        recvPackets = protocol().recvRobotPacketsSinceConnect();
                        sentPackets = protocol().sentRobotPacketsSinceConnect();
                    }

                    if (recvPackets > 0 && sentPackets > 0)
                        loss = (1 - (recvPackets / sentPackets)) * 100;
                    else if (!isConnectedToRobot())
                        loss = 100;

                    packetLoss = (int) loss;
                }
            }
        }, "PACKET_LOSS");//.start();
    }

    public void updateAddress(int unused) {
        updateAddresses();
    }

    public void updateLogs(String file) {
//        if(!logPath.isEmpty() || file == logPath)
//            openLog(file);
    }

    public void readRadioPacket(byte[] data) {
        if(protocol() != null && running())
            if(protocol().readRadioPacket(data))
                radioWatchdog.stop();
                radioWatchdog.start();
    }

    public void readRobotPacket(byte[] data) {
        if (protocol() != null && running())
            if (protocol().readRobotPacket(data))
                robotWatchdog.stop();
                robotWatchdog.start();
    }

    public DSConfig config() {
        return DSConfig.getInstance();
    }

    public Protocol protocol() {
        return protocol;
    }
}

class Watchdog implements Runnable {

    private Vector observers = new Vector(1);

    private final long timeout;

    private boolean stopped = false;

    public Watchdog(final long timeout) {
        if (timeout < 1) {
            throw new IllegalArgumentException("timeout must not be less than 1.");
        }
        this.timeout = timeout;
    }

    public void addTimeoutObserver(final TimeoutObserver to) {
        observers.addElement(to);
    }

    public void removeTimeoutObserver(final TimeoutObserver to) {
        observers.removeElement(to);
    }

    protected final void fireTimeoutOccured() {
        Enumeration e = observers.elements();
        while (e.hasMoreElements()) {
            ((TimeoutObserver) e.nextElement()).timeoutOccured(this);
        }
    }

    public synchronized void start() {
        stopped = false;
        Thread t = new Thread(this, "WATCHDOG");
        t.setDaemon(true);
        t.start();
    }

    public synchronized void stop() {
        stopped = true;
        notifyAll();
    }

    public synchronized void run() {
        final long until = System.currentTimeMillis() + timeout;
        long now;
        while (!stopped && until > (now = System.currentTimeMillis())) {
            try {
                wait(until - now);
            } catch (InterruptedException e) {
            }
        }
        if (!stopped) {
            fireTimeoutOccured();
        }
    }
}

interface TimeoutObserver {
    void timeoutOccured(Watchdog w);
}