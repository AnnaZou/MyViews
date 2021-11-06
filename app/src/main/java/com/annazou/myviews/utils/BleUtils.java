package com.annazou.myviews.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.HashMap;

public class BleUtils {

    public static class BleDeviceScanCallback implements BluetoothAdapter.LeScanCallback {
        private ScanCallback mScanCallback;

        public BleDeviceScanCallback(ScanCallback scanCallback) {
            this.mScanCallback = scanCallback;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (null != mScanCallback) {
                //每次扫描到设备会回调此方法,这里一般做些过滤在添加进list列表
                mScanCallback.onScanning(device, rssi, scanRecord);
            }
        }
    }

    public interface ScanCallback  {
        /**
         * 扫描完成回调
         */
        void onSuccess();

        /**
         * 扫描过程中,每扫描到一个设备回调一次
         *
         * @param device     扫描到的设备
         * @param rssi       设备的信息强度
         * @param scanRecord
         */
        void onScanning(final BluetoothDevice device, int rssi, byte[] scanRecord);
    }

    public interface ConnectCallback {
        /**
         *  获得通知之后
         */

        void onConnSuccess();

        /**
         * 断开或连接失败
         */
        void onConnFailed();
    }

    public interface OnReceiverCallback {
        void onReceiver(byte[] value);
    }

    /**
     * 描述:写操作回调接口
     */
    public interface OnWriteCallback {

        /**
         * 蓝牙未开启
         */
        int FAILED_BLUETOOTH_DISABLE = 1;
        /**
         * 服务无效
         */
        int FAILED_INVALID_SERVICE = 2;
        /**
         * 特征无效
         */
        int FAILED_INVALID_CHARACTER = 3;
        /**
         * 操作失败
         */
        int FAILED_OPERATION = 5;

        /**
         * 写入成功
         */
        void onSuccess();

        /**
         * 写入失败
         *
         * @param state
         */
        void onFailed(int state);
    }

    public interface IRequestQueue<T> {
        void set(String key, T t);

        T get(String key);
    }

    public static class ReceiverRequestQueue implements IRequestQueue<OnReceiverCallback> {
        private static final String TAG = "ReceiverRequestQueue";
        HashMap<String, OnReceiverCallback> map = new HashMap<>();

        @Override
        public void set(String key, OnReceiverCallback onReceiver) {
            map.put(key, onReceiver);
        }

        @Override
        public OnReceiverCallback get(String key) {
            return map.get(key);
        }

        public HashMap<String, OnReceiverCallback> getMap() {
            return map;
        }

        /**
         * 移除一个元素
         *
         * @param key
         */
        public boolean removeRequest(String key) {
            Log.d(TAG, "ReceiverRequestQueue before:" + map.size());
            OnReceiverCallback onReceiverCallback = map.remove(key);
            Log.d(TAG, "ReceiverRequestQueue after:" + map.size());
            return null == onReceiverCallback;
        }
    }

}
