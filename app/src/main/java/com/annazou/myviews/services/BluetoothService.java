package com.annazou.myviews.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.annazou.myviews.utils.BleUtils;
import com.annazou.myviews.utils.BleUtils.ConnectCallback;
import com.annazou.myviews.utils.BleUtils.OnWriteCallback;
import com.annazou.myviews.utils.BleUtils.ReceiverRequestQueue;
import com.annazou.myviews.utils.BleUtils.OnReceiverCallback;
import com.annazou.myviews.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {

    private static final String TAG = "mytest";
    // UUID-->通用唯一识别码，能唯一地辨识咨询
    private static final String NAME = "BlueToothService";

    private static final String SERVICE_UUID = "00001800-0000-1000-8000-00805f9b34fb";
    //            "00001101-0000-1000-8000-00805F9B34FB");//串口
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NOTIFY_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String WRITE_UUID = "00001812-0000-1000-8000-00805f9b34fb";
    private static final String BLUETOOTH_NOTIFY_D = "00002902-0000-1000-8000-00805f9b34fb";


    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";

    private BluetoothAdapter mAdapter;
    private int mState;
    private String mConnectedDeviceName;
    private Mybinder mBinder;

    private List<MessageItem> mMessage;
    private BluetoothDevice mDevice;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case STATE_CONNECTED:
                            break;
                        case STATE_CONNECTING:
                            break;
                        case STATE_LISTEN:
                        case STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    MessageItem write = new MessageItem();
                    write.content = writeMessage;
                    write.name = "me";
                    mMessage.add(write);
                    if (mCallback != null) {
                        mCallback.onMessageSent();
                    }
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    MessageItem read = new MessageItem();
                    read.content = readMessage;
                    read.name = "they";
                    mMessage.add(read);
                    if (mCallback != null) {
                        mCallback.onMessageReceived();
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "连接到" + mConnectedDeviceName, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString("msg"), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // 得到本地蓝牙适配器
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        mBinder = new Mybinder();
        mMessage = new ArrayList<>();

        registReciveListener(TAG, new OnReceiverCallback() {
            @Override
            public void onReceiver(byte[] value) {
                Log.e("mytest", "receive " + NetworkUtils.bytesToHexString(value));
            }
        });
        scanLeDevice(true);

    }

    @Override
    public void onDestroy() {
        unregistReciveListener(TAG);
        if (mBleGatt != null) {
            mBleGatt.close();
            mBleGatt = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    public class Mybinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public List<MessageItem> getMessageList() {
        return mMessage;
    }

    private boolean mScanning;
    private BluetoothGatt mBleGatt;
    private BluetoothGattCharacteristic mBleGattCharacteristic;
    private OnWriteCallback mWriteCallback;
    private HashMap<String, Map<String, BluetoothGattCharacteristic>> mServicesMap = new HashMap<>();
    //连接请求是否ok
    private boolean isConnectok = false;
    //是否是用户手动断开
    private boolean isMybreak = false;
    //连接结果的回调
    private ConnectCallback connectCallback;
    //读操作请求队列
    private ReceiverRequestQueue mReceiverRequestQueue = new ReceiverRequestQueue();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.e("mytest", "device address = " + device.getAddress());
                    Log.e("mytest", "device uuid = " + device.getUuids());
//                    if (device.getAddress().equals("C1:C4:46:21:B1:95")) {
//                        mDevice = device;
//                        mBleGatt = mDevice.connectGatt(getApplicationContext(), false, mGattCallback);
//                        mAdapter.stopLeScan(mLeScanCallback);
//                    }

                }
            };

    private void sendTest() {
        int b1 = 0x03;
        int b2 = 0x00;
        String send = String.valueOf(b1);
        writeBuffer(send, new OnWriteCallback() {
            @Override
            public void onSuccess() {
                Log.e("mytest", "send success");
            }

            @Override
            public void onFailed(int state) {
                Log.e("mytest", "send failed");
            }
        });

    }

    // BLE API定义的各种回调方法
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    Log.e("mytest", "onConnectionStateChange " + status + "  " + newState);

                    if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
                        Log.e("mytest", "connected");
                        isMybreak = false;
                        isConnectok = true;
                        boolean result = mBleGatt.discoverServices();
                        Log.e("mytest", "result = " + result);
                        // connSuccess();
                        sendTest();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {   //断开连接
                        if (!isMybreak) {
                            //      reConnect();
                        }
                        //  reset();
                    }
                }

                //发现新服务
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Log.e(TAG, "onServicesDiscovered = " + status);
                    if (null != mBleGatt && status == BluetoothGatt.GATT_SUCCESS) {
                        List<BluetoothGattService> services = mBleGatt.getServices();
                        for (int i = 0; i < services.size(); i++) {
                            HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
                            BluetoothGattService bluetoothGattService = services.get(i);
                            String serviceUuid = bluetoothGattService.getUuid().toString();
                            Log.e("mytest","serviceUuid = " + serviceUuid);
                            List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                            for (int j = 0; j < characteristics.size(); j++) {
                                charMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
                            }
                            mServicesMap.put(serviceUuid, charMap);
                        }
                        BluetoothGattCharacteristic NotificationCharacteristic = getBluetoothGattCharacteristic(SERVICE_UUID, NOTIFY_UUID);
                        if (NotificationCharacteristic == null)
                            return;
                        enableNotification(true, NotificationCharacteristic);
                        sendTest();
                    }
                }

                //读数据
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                }

                //写数据
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    if (null != mWriteCallback) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    mWriteCallback.onSuccess();
                                }
                            });
                            Log.e("mytest", "Send data success!");
                        } else {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    mWriteCallback.onFailed(BleUtils.OnWriteCallback.FAILED_OPERATION);
                                }
                            });
                            Log.e("mytest", "Send data failed!");
                        }
                    }
                }

                //通知数据
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    if (null != mReceiverRequestQueue) {
                        HashMap<String, BleUtils.OnReceiverCallback> map = mReceiverRequestQueue.getMap();
                        final byte[] rec = characteristic.getValue();
                        for (String key : mReceiverRequestQueue.getMap().keySet()) {
                            final OnReceiverCallback onReceiverCallback = map.get(key);
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    onReceiverCallback.onReceiver(rec);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                }
            };

    /**
     * 设置读取数据的监听
     *
     * @param requestKey
     * @param onReceiverCallback
     */
    public void registReciveListener(String requestKey, BleUtils.OnReceiverCallback onReceiverCallback) {
        mReceiverRequestQueue.set(requestKey, onReceiverCallback);
    }

    /**
     * 移除读取数据的监听
     *
     * @param requestKey
     */
    public void unregistReciveListener(String requestKey) {
        mReceiverRequestQueue.removeRequest(requestKey);
    }

    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {
        if (mBleGatt == null || characteristic == null)
            return false;
        if (!mBleGatt.setCharacteristicNotification(characteristic, enable))
            return false;
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_NOTIFY_D));
        if (clientConfig == null)
            return false;

        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBleGatt.writeDescriptor(clientConfig);
    }

    public void writeBuffer(String value, OnWriteCallback writeCallback) {
        mWriteCallback = writeCallback;

        if (mBleGattCharacteristic == null) {
            mBleGattCharacteristic = getBluetoothGattCharacteristic(SERVICE_UUID, WRITE_UUID);
        }

        if (null == mBleGattCharacteristic) {
            writeCallback.onFailed(OnWriteCallback.FAILED_INVALID_CHARACTER);
            Log.e(TAG, "FAILED_INVALID_CHARACTER");
            return;
        }

        //设置数组进去
        mBleGattCharacteristic.setValue(NetworkUtils.hexStringToBytes(value));
        //发送

        boolean b = mBleGatt.writeCharacteristic(mBleGattCharacteristic);

        Log.e(TAG, "send:" + b + "data：" + value);
    }

    public void Connect(final int connectionTimeOut, final String address, ConnectCallback connectCallback) {

        if (mAdapter == null || address == null) {
            Log.e(TAG, "No device found at this address：" + address);
            return;
        }
        BluetoothDevice remoteDevice = mAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return;
        }
        this.connectCallback = connectCallback;
        mBleGatt = remoteDevice.connectGatt(getApplicationContext(), false, mGattCallback);
        Log.e(TAG, "connecting mac-address:" + address);
        //delayConnectResponse(connectionTimeOut);
    }

    public BluetoothGattService getService(UUID uuid) {
        if (mAdapter == null || mBleGatt == null) {
            return null;
        }
        return mBleGatt.getService(uuid);
    }

    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID) {
        if (null == mBleGatt) {
            return null;
        }

        //找服务
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = mServicesMap.get(serviceUUID);
        if (null == bluetoothGattCharacteristicMap) {
            Log.e("mytest", "Not found the serviceUUID!");
            return null;
        }

        //找特征
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
        BluetoothGattCharacteristic gattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            if (characterUUID.equals(entry.getKey())) {
                gattCharacteristic = entry.getValue();
                break;
            }
        }
        return gattCharacteristic;
    }

    private void runOnMainThread(Runnable runnable) {
        if (mHandler != null) {
            mHandler.post(runnable);
        }
    }

    public interface MessageCallback {
        void onMessageReceived();

        void onMessageSent();
    }

    MessageCallback mCallback;

    public void setCallback(MessageCallback cb) {
        mCallback = cb;
    }

    private synchronized void setState(int state) {

        mState = state;
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1)
                .sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        setState(STATE_LISTEN);
    }

    // 取消 Connecting Connected状态下的相关线程，然后运行新的mConnectThread线程
    public synchronized void connect(BluetoothDevice device) {
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {

        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    // 停止所有相关线程，设当前状态为none
    public synchronized void stop() {
        setState(STATE_NONE);
    }

    // 在STATE_CONNECTED状态下，调用mConnectedThread里的write方法，写入byte
    public void write() {
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
      //      writeBuffer();
        }
    }

    // 连接失败的时候处理，通知UI，并设为STATE_LISTEN状态
    private void connectionFailed() {
        setState(STATE_LISTEN);

        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("msg", "链接不到设备");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        start();
    }

    // 当连接失去的时候，设为STATE_LISTEN
    private void connectionLost() {
        setState(STATE_LISTEN);

        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("msg", "设备链接中断");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        start();
    }


    public void sendMessage(String message) {
        if (getState() != STATE_CONNECTED) {
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            //write(send);

        }
    }

    public void sendMessage(byte[] data) {
        if (getState() != STATE_CONNECTED) {
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.length > 0) {
            //write(data);
        }
    }

    public class MessageItem {
        public String name;
        public String content;
        public Drawable icon;
    }
}
