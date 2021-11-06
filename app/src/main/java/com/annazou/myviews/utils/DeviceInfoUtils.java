package com.annazou.myviews.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.Set;

public class DeviceInfoUtils {

    public static String getImei(Context context, int id) throws SecurityException {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId(id) + "";
    }

    static public BluetoothDevice getPairedDevice(BluetoothAdapter adapter) {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String str = device.getName() + "|" + device.getAddress();
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    return device;
                }
            }
        }
        return null;
    }
}
