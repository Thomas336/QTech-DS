package com.quantum.qtech.qtechfrcdriverstation.DSLib.Util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class Lookup {
    Context context;
    NsdHelper mNsdHelper;
    public static final String TAG = "NsdChat";

    public Lookup(Context c) {
        context = c;
        mNsdHelper = new NsdHelper(context);
        mNsdHelper.initializeNsd();
    }

    public void discover() {
        mNsdHelper.discoverServices();
    }

    public void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
    }

    public void onResume() {
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

    public void onDestroy() {
        mNsdHelper.tearDown();
    }

//    public interface LookupListener {
//        void found(String name, String address);
//    }
//
//    static LookupListener listener;
//
//    public void setResolvedListener(LookupListener l) {
//        listener = l;
//    }
//
//    private static class HostFound implements ServiceListener {
//        LookupListener listener;
//
//        public HostFound(LookupListener l) {
//            listener = l;
//        }
//
//        @Override
//        public void serviceAdded(ServiceEvent event) {
//            System.out.println("Service added: " + event.getInfo());
//        }
//
//        @Override
//        public void serviceRemoved(ServiceEvent event) {
//            System.out.println("Service removed: " + event.getInfo());
//        }
//
//        @Override
//        public void serviceResolved(ServiceEvent event) {
//            System.out.println("Service resolved: " + event.getInfo());
//            listener.found(event.getInfo().getQualifiedName(), event.getInfo().getHostAddresses()[0]); //FIXME: This is better...
//        }
//    }
//
//    public static void lookup(String hostname) throws InterruptedException {
//        try {
//            // Create a JmDNS instance
//            JmDNS jmdns = JmDNS.create();//.create(InetAddress.getLocalHost());
//
//            // Add a service listener
//            jmdns.addServiceListener("_" + hostname + "._tcp.local.", new HostFound(listener));
//
//            // Wait a bit
////            Thread.sleep(30000);
//        } catch (UnknownHostException e) {
//            System.out.println(e.getMessage());
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//    }
}