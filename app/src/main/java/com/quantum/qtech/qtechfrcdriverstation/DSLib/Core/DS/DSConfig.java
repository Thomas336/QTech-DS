package com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS;

import com.quantum.qtech.qtechfrcdriverstation.DSLib.DriverStation;

import java.util.Timer;

public class DSConfig {
    private static DSConfig ourInstance = new DSConfig();

    public static DSConfig getInstance() {
        return ourInstance;
    }

    private int team;
    private float voltage;
    private int cpuUsage;
    private int ramUsage;
    private int diskUsage;

    private String libVersion;
    private String pcmVersion;
    private String pdpVersion;

    private boolean simulated;
    private boolean timerEnabled;

    private Alliance alliance;
    private Position position;
    private CodeStatus codeStatus;
    private DSControlMode controlMode;
    private EnableStatus enableStatus;
    private CommStatus radioCommStatus;
    private CommStatus robotCommStatus;
    private VoltageStatus voltageStatus;
    private OperationStatus operationStatus;

    long timerStart = 0;
//    Logger logger;

    private DSConfig() {
        team = 0;
        voltage = 0;
        cpuUsage = 0;
        ramUsage = 0;
        diskUsage = 0;

        libVersion = "";
        pcmVersion = "";
        pdpVersion = "";

        simulated = false;
        timerEnabled = false;

        alliance = Alliance.RED;
        position = Position.P1;
        codeStatus = CodeStatus.FAILING;
        controlMode = DSControlMode.TELEOP;
        enableStatus = EnableStatus.DISABLED;
        radioCommStatus = CommStatus.FAILING;
        robotCommStatus = CommStatus.FAILING;
        voltageStatus = VoltageStatus.NORMAL;
        operationStatus = OperationStatus.NORMAL;

//        QThread thread = new QThread(this);
//        logger.moveToTread(thread);
//        thread.start();

        updateElapsedTime();
    }

//    public Logger logger() {
//        return logger;
//    }

    public int team() {
        return team;
    }

    public int cpuUsage() {
        return cpuUsage;
    }

    public int ramUsage() {
        return ramUsage;
    }

    public int diskUsage() {
        return diskUsage;
    }

    public float voltage() {
//        if(isConnectedToRobot())
            return voltage;
//        return 0;
    }

    public boolean isEnabled() {
        return enableStatus() == EnableStatus.ENABLED;
    }

    public boolean isSimulated() {
        return simulated;
    }

    public Alliance alliance() {
        return alliance;
    }

    public Position position() {
        return position;
    }

    public String libVersion() {
        return libVersion;
    }

    public String pcmVersion() {
        return pcmVersion;
    }

    public String pdpVersion() {
        return pdpVersion;
    }

    public boolean isEmergencyStopped() {
        return operationStatus() == OperationStatus.ESTOP;
    }

    public boolean isRobotCodeRunning() {
        return robotCodeStatus() == CodeStatus.RUNNING;
    }

    public boolean isConnectedToRadio() {
        return radioCommStatus() == CommStatus.RUNNING;
    }

    public boolean isConnectedToRobot() {
        return robotCommStatus() == CommStatus.RUNNING;
    }

    public DSControlMode controlMode() {
        return controlMode;
    }

    public EnableStatus enableStatus() {
        return enableStatus;
    }

    public CommStatus radioCommStatus() {
        return radioCommStatus;
    }

    public CommStatus robotCommStatus() {
        return robotCommStatus;
    }

    public CodeStatus robotCodeStatus() {
        return codeStatus;
    }

    public VoltageStatus voltageStatus() {
        return voltageStatus;
    }

    public OperationStatus operationStatus() {
        return operationStatus;
    }

    public void updateTeam(int Team) {
        System.out.println("New Team "+Team);
        if(team != Team) {
            team = Team;
            //emit teamChanged(team);

            System.out.println("Team number set to " + Team);
        }
    }

    public void setRobotCode(boolean code) {
        CodeStatus status = CodeStatus.FAILING;
        if(code)
            status = CodeStatus.RUNNING;

        updateRobotCodeStatus(status);
    }

    public void setEnabled(boolean enabled) {
        EnableStatus status = EnableStatus.DISABLED;
        if(enabled)
            status = EnableStatus.DISABLED;

        updateEnabled(status);
    }

    public void updateCpuUsage(int usage) {
        cpuUsage = usage;
        //emit cpuUsageChanged(usage);
    }

    public void updateRamUsage(int usage) {
        ramUsage = usage;
        //emit ramUsageChanged(usage);
    }

    public void updateDiskUsage(int usage) {
        diskUsage = usage;
        //emit diskUsageChanged(usage);
    }

    public void setBrownout(boolean brownout) {
        VoltageStatus status = VoltageStatus.NORMAL;
        if(brownout)
            status = VoltageStatus.BROWNOUT;

        updateVoltageStatus(status);
    }

    public void setEmergencyStop(boolean estop) {
        OperationStatus status = OperationStatus.NORMAL;
        if(estop)
            status = OperationStatus.ESTOP;

        updateOperationStatus(status);
    }

    public void updateVoltage(float voltage) {
    /* Round voltage to two decimal places */
        voltage = Math.round(voltage * 100) / 100;

    /* Avoid this: http://i.imgur.com/iAAi1bX.png */
        if (voltage > DriverStation.getInstance().maxBatteryVoltage())
        voltage = DriverStation.getInstance().maxBatteryVoltage();

    /* Separate voltage into natural and decimal numbers */
        int integer = (int)voltage;
        int decimal = (int)((voltage - integer) * 100);

    /* Convert the obtained numbers into strings */
        String integer_str = Integer.toString(integer);
        String decimal_str = Integer.toString(decimal);

    /* Prepend a 0 to the decimal numbers if required */
        if (decimal < 10)
            decimal_str = "0"+decimal_str;

    /* Emit signals */
        this.voltage = integer+(decimal*.01f);
        //emit voltageChanged (integer_str + "." + decimal_str + " V");

    /* Log robot voltage */
        //m_logger.registerVoltage (m_voltage);
    }

    public void updateSimulated(boolean Simulated) {
        simulated = Simulated;
        //emit simulatedChanged(simulated);
    }

    public void updateAlliance(Alliance a) {
        if(alliance != a) {
            alliance = a;
            //m_logger.registerAlliance (alliance);
        }

        //emit allianceChanged(alliance)
    }

    public void updatePosition(Position p) {
        if(position != p) {
            position = p;
            //logger.registerPosition (position);
        }
    }

    public void updateControlMode(DSControlMode mode) {
        if(controlMode != mode) {
            controlMode = mode;
            //logger.registerControlMode(mode);
        }
    }

    public void updateLibVersion(String version) {
        if (!libVersion.equals(version)) {
            libVersion = version;
            System.out.println("LIB version set to "+version);
        }
    }

    public void updatePcmVersion(String version) {
        if (!pcmVersion.equals(version)) {
            pcmVersion = version;
            System.out.println("PCM Version changed to "+version);
        }
    }

    public void updatePdpVersion(String version) {
        if(!pdpVersion.equals(version)) {
            pdpVersion = version;
            System.out.println("PDP Version changed to " + version);
        }
    }

    public void updateEnabled(EnableStatus statusChanged) {
        if(enableStatus != statusChanged) {
            enableStatus = statusChanged;

            if(statusChanged == EnableStatus.ENABLED) {
                timerStart = System.nanoTime();
                timerEnabled = true;
            }
            else
                timerEnabled = false;

            //logger.registerEnableStatus(statusChanged);
        }

        //emit enabledChanged(enableStatus);
        //emit statusChanged(DriverStation.getInstance().generalStatus());
    }

    public void updateRadioCommStatus(CommStatus statusChanged) {
        if(robotCommStatus != statusChanged) {
            robotCommStatus = statusChanged;
        }
    }

    public void updateRobotCommStatus(CommStatus statusChanged) {

    }

    public void updateRobotCodeStatus(CodeStatus statusChanged) {
        if(codeStatus != statusChanged) {
            codeStatus = statusChanged;
            //logger.registerCodeStatus(statusChanged);
        }
    }

    public void updateVoltageStatus(VoltageStatus statusChanged) {
        if(voltageStatus != statusChanged) {
            voltageStatus = statusChanged;
            //logger.registerVoltageStatus(statusChanged);
        }

        //emit voltageStatusChanged(voltageStatus);
        //emit statusChanged(DriverStation.getInstance().generalStatus());
    }

    public void updateOperationStatus(OperationStatus statusChanged) {
        if(operationStatus != statusChanged) {
            operationStatus = statusChanged;
            updateEnabled(EnableStatus.DISABLED);
            //logger.registerOperationStatus(statusChanged);
        }

        //emit operationStatusChanged(operationStatus);
        //emit statusChanged(DriverStation.getInstance().generalStatus());
    }

    public void updateElapsedTime() {
        if(timerEnabled && isConnectedToRobot() && !isEmergencyStopped()) {
            int msec = (int)((timerStart - System.nanoTime())/1000000);
            int secs = msec/1000;
            int mins = (msec/60) % 60;

            secs = secs % 60;
            msec = msec % 1000;

            //emit elapsedTimeChanged(msec)
            //emit elapsedTimeChanged(mins+":"+secs+"."+msec);
        }

//        /DS_Schedule(100, this, SLOT(updateElapsedTime()));
    }
}
