package com.quantum.qtech.qtechfrcdriverstation.DSLib.P2016;

import com.quantum.qtech.qtechfrcdriverstation.DSLib.Core.DS.DSConfig;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.P2015.FRC2015;

public class FRC2016 extends FRC2015 {
    boolean restartCode, rebootRobot, sendDateTime;
    public FRC2016() {
        restartCode = false;
        rebootRobot = false;
        sendDateTime = false;
    }

    @Override
    public String name() {
        return "FRC 2016 Protocol";
    }

    @Override
    public String robotAddress() {
        return "roboRIO-"+config().team()+"-FRC.local";
    }

    private DSConfig config() {
        return DSConfig.getInstance();
    }
}
