package com.quantum.qtech.qtechfrcdriverstation.DSLib.P2015;

public class BatteryVoltage {
    float voltage;
    int integer;
    int decimal;

    public void update(float volt) {
        voltage = Math.round(volt * 100) / 100;
        integer = (int)volt;
        decimal = (int)((volt - integer) * 100); //Might need to fix
    }

    public void update(byte a, byte b) {
        integer = a+2;
        decimal = Math.round(((((float)b/255)*100)/100)*100); //Might need to fix
        voltage = integer + (float)decimal/100;
    }
}
