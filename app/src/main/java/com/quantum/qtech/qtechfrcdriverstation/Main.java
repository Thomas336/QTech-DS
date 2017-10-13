package com.quantum.qtech.qtechfrcdriverstation;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.quantum.qtech.qtechfrcdriverstation.DSLib.DriverStation;
import com.quantum.qtech.qtechfrcdriverstation.DSLib.ProtocolType;

public class Main extends ActionBarActivity {
    WifiManager.MulticastLock lock;
//    int[] lookup = {
//            4,
//            2,
//            1,
//            3,
//            5,
//            6,
//            -1,
//            -2,
//            7,
//            8
//    };

    int[] lookupL = {
            6,  //Gather
            2,
            1,
            3,
            5,
            -2, //Eject
            -1,
            4,
            7,
            8
    };

    int[] lookupR = {
            6,  //Shoot
            -1,
            -1,
            -1,
            5,  //Batter
            -2, //Spool
            -1,
            -1,
            -1, //Gather
            1   //Cross
    };

    static int NUM_BUTTONS = 10;

    DriverStation ds;

    RelativeLayout ViewJoyLeft, ViewJoyRight, ViewSlideLeft, ViewSlideRight;
    int[] Buttons_L = new int[NUM_BUTTONS];
    int[] Buttons_R = new int[NUM_BUTTONS];
    boolean[] States_L = new boolean[NUM_BUTTONS];
    boolean[] States_R = new boolean[NUM_BUTTONS];
    EditText robotIp;
    ToggleButton enableBtn, modeBtn;
    Button setipBtn;
    ImageButton settings;
    Chronometer timer;
    TextView status;
    Options options;

    Joystick JoyLeft, JoyRight;
    Slider SlideLeft, SlideRight;

    public void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        options = new Options();

//        getFragmentManager().beginTransaction().add(new Options(), "Options");

//        WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
//        lock = wifi.createMulticastLock("FRCDNSLock");
//        lock.setReferenceCounted(true);
//        lock.acquire();

        ViewJoyLeft = (RelativeLayout) findViewById(R.id.L_Joy);
        ViewJoyRight = (RelativeLayout) findViewById(R.id.R_Joy);
        ViewSlideLeft = (RelativeLayout) findViewById(R.id.L_Slide);
        ViewSlideRight = (RelativeLayout) findViewById(R.id.R_Slide);
        for(int i=0;i<NUM_BUTTONS;i++)
        {
            Buttons_L[i] = getResources().getIdentifier("L_"+(i+1), "id", getApplicationContext().getPackageName());
            Buttons_R[i] = getResources().getIdentifier("R_"+(i+1), "id", getApplicationContext().getPackageName());
        }

        JoyLeft = new Joystick(getApplicationContext(), ViewJoyLeft, R.drawable.stick);
        JoyRight = new Joystick(getApplicationContext(), ViewJoyRight, R.drawable.stick);
        SlideLeft = new Slider(getApplicationContext(), ViewSlideLeft, R.drawable.stick);
        SlideRight = new Slider(getApplicationContext(), ViewSlideRight, R.drawable.stick);

        enableBtn = (ToggleButton) findViewById(R.id.btn_enable);
        modeBtn = (ToggleButton) findViewById(R.id.btn_mode);
        timer = (Chronometer) findViewById(R.id.elapsed);

        robotIp = (EditText) findViewById(R.id.ip_robot);
        setipBtn = (Button) findViewById(R.id.btn_setip);
        status = (TextView) findViewById(R.id.status);
        settings = (ImageButton) findViewById(R.id.settings);

        ds = DriverStation.getInstance();
        ds.setProtocolType(ProtocolType.FRC2016);
        ds.setTeam(1592);
        ds.setCustomRobotAddress(robotIp.getText().toString());
        ds.registerJoystick(6, 10, 0);
        ds.registerJoystick(6, 10, 0);
        ds.init();

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), Options.class);
                startActivity(i);
//                getFragmentManager()..show(options); //.replace(android.R.id.content, new Options()).commit();
            }
        });

        JoyLeft.setOnMoveHandler(new Joystick.OnMoveHandler() {
            @Override
            public void onMove(View view, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN
                        || e.getAction() == MotionEvent.ACTION_MOVE) {
                    ds.updateAxis(0, 0, JoyLeft.getX());
                    ds.updateAxis(0, 5, JoyLeft.getY());
//                    textView3.setText("Angle : " + String.valueOf(JoyLeft.getAngle()));
//                    textView4.setText("Distance : " + String.valueOf(JoyLeft.getDistance()));
                } else if (e.getAction() == MotionEvent.ACTION_UP) {
                    ds.updateAxis(0, 0, 0);
                    ds.updateAxis(0, 5, 0);
//                    textView3.setText("Angle :");
//                    textView4.setText("Distance :");
                }
            }
        });

        JoyRight.setOnMoveHandler(new Joystick.OnMoveHandler() {
            @Override
            public void onMove(View view, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN
                        || e.getAction() == MotionEvent.ACTION_MOVE) {
                    ds.updateAxis(1, 0, JoyRight.getX());
                    ds.updateAxis(1, 5, JoyRight.getY());
//                    textView3.setText("Angle : " + String.valueOf(JoyLeft.getAngle()));
//                    textView4.setText("Distance : " + String.valueOf(JoyLeft.getDistance()));
                } else if (e.getAction() == MotionEvent.ACTION_UP) {
                    ds.updateAxis(1, 0, 0);
                    ds.updateAxis(1, 5, 0);
//                    textView3.setText("Angle :");
//                    textView4.setText("Distance :");
                }
            }
        });

        SlideLeft.setOnMoveHandler(new Slider.OnMoveHandler() {
            public void onMove(View view, MotionEvent e) {
                ds.updateAxis(1, 1, SlideLeft.get());
            }
        });

        SlideRight.setOnMoveHandler(new Slider.OnMoveHandler() {
            public void onMove(View view, MotionEvent e) {
                ds.updateAxis(1, 1, SlideLeft.get());
            }
        });

        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enableBtn.isChecked()) {
                    System.out.println("Enabling");
                    ds.enableRobot();
                    timer.setBase(SystemClock.elapsedRealtime());
                    timer.start();
                } else {
                    System.out.println("Disabling");
                    ds.disableRobot();
                    timer.stop();
                }
            }
        });

        modeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modeBtn.isChecked()) {
                    System.out.println("Auto Mode");
                    ds.switchToAutonomous();
                } else {
                    System.out.println("Telop Mode");
                    ds.switchToTeleoperated();
                }
                enableBtn.setChecked(false);
                timer.stop();
            }
        });

        setipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ds.setCustomRobotAddress(robotIp.getText().toString());
                ds.resetRobot();
            }
        });

        for(int i=0;i<NUM_BUTTONS;i++)
        {
            findViewById(Buttons_L[i]).setOnTouchListener(new Button.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent e) {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            for(int i=0;i<Buttons_L.length;i++) {
                                if(Buttons_L[i] == v.getId())
                                {
                                    if(lookupL[i] == -1)
                                        ds.updateAxis(0, 2, 1);
                                    else if(lookupL[i] == -2)
                                        ds.updateAxis(0, 3, 1);
                                    else {
                                        ds.updateButton(0, lookupL[i], true);
                                        States_L[i] = true;
                                    }
                                }
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            for(int i=0;i<Buttons_L.length;i++) {
                                if(Buttons_L[i] == v.getId())
                                {
                                    if(lookupL[i] == -1)
                                        ds.updateAxis(0, 2, 0);
                                    else if(lookupL[i] == -2)
                                        ds.updateAxis(0, 3, 0);
                                    else {
                                        ds.updateButton(0, lookupL[i], false);
                                        States_L[i] = false;
                                    }
                                }
                            }
                            return true;
                    }
                    return false;
                }
            });

            findViewById(Buttons_R[i]).setOnTouchListener(new Button.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent e) {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            for(int i=0;i<Buttons_R.length;i++) {
                                if(Buttons_R[i] == v.getId())
                                {
                                    if(lookupR[i] == -1)
                                        ds.updateAxis(1, 2, 1);
                                    else if(lookupR[i] == -2)
                                        ds.updateAxis(1, 3, 1);
                                    else {
                                        ds.updateButton(1, lookupR[i], true);
                                        States_R[i] = true;
                                    }
                                }
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            for(int i=0;i<Buttons_R.length;i++) {
                                if(Buttons_R[i] == v.getId())
                                {
                                    if(lookupR[i] == -1)
                                        ds.updateAxis(1, 2, 0);
                                    else if(lookupR[i] == -2)
                                        ds.updateAxis(1, 3, 0);
                                    else {
                                        ds.updateButton(1, lookupR[i], false);
                                        States_R[i] = false;
                                    }
                                }
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        startStatusThread();
    }

    public void startStatusThread() {
        Thread statusThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                status.setText(ds.generalStatus()+"\n"+
                                        ds.currentBatteryVoltage()+"\n"+
                                        ds.cpuUsage()+"\n"+
                                        ds.ramUsage()+"\n"+
                                        ds.diskUsage()+"\n"+
                                        ds.team());
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        statusThread.start();
    }

    @Override
    protected void onDestroy() {
        if(lock != null)
            lock.release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        ds.resetJoysticks();
        ds.disableRobot();
        enableBtn.setChecked(false);
        JoyLeft.zeroStick();
        JoyRight.zeroStick();
        SlideLeft.zeroSlide();
        SlideRight.zeroSlide();
        super.onPause();
    }
}