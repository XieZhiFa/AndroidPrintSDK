package com.android.print.demo.bluetooth;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;

import com.android.print.demo.IPrinterOpertion;
import com.android.print.demo.MainActivity;
import com.android.print.sdk.PrinterInstance;
import com.android.print.sdk.PrinterConstants.Connect;
import com.android.print.sdk.bluetooth.BluetoothPort;
import com.android.print.sdk.util.Utils;

public class BluetoothOperation implements IPrinterOpertion {
    private BluetoothAdapter adapter;
    private Context mContext;

    private BluetoothDevice mDevice;
    private Handler mHandler;
    private PrinterInstance mPrinter;
    public static boolean hasRegDisconnectReceiver;
    private IntentFilter filter;
    private String mac;


    public BluetoothOperation(Context context, Handler handler) {
        adapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        mHandler = handler;


        filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(myReceiver, filter);
        hasRegDisconnectReceiver = true;

    }

    public void open(Intent data) {
        mac = data.getExtras().getString(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
        mPrinter = new BluetoothPort().btConnnect(mContext, mac, adapter, mHandler);
        Utils.saveBtConnInfo(mContext, mac);
    }

    @Override
    public void btAutoConn(Context context, Handler mHandler) {

        mPrinter = new BluetoothPort().btAutoConn(context, adapter, mHandler);
        if (mPrinter == null) {
            mHandler.obtainMessage(Connect.NODEVICE).sendToTarget();
        }

    }


    public void close() {
        if (mPrinter != null) {
            mPrinter.closeConnection();
            mPrinter = null;
        }
        if (hasRegDisconnectReceiver) {
            mContext.unregisterReceiver(myReceiver);
            hasRegDisconnectReceiver = false;
        }
    }

    public PrinterInstance getPrinter() {
        if (mPrinter != null && mPrinter.isConnected()) {
            if (!hasRegDisconnectReceiver) {
                mContext.registerReceiver(myReceiver, filter);
                hasRegDisconnectReceiver = true;
            }
        }
        return mPrinter;
    }

    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {

                if (device != null && mPrinter != null
                        && mPrinter.isConnected() && device.equals(mDevice)) {
                    close();
                }

                mHandler.obtainMessage(Connect.CLOSED).sendToTarget();
            }


        }
    };

    @Override
    public void chooseDevice() {
        if (!adapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) mContext).startActivityForResult(enableIntent,
                    MainActivity.ENABLE_BT);
        } else {
            Intent intent = new Intent(mContext, BluetoothDeviceList.class);
            ((Activity) mContext).startActivityForResult(intent,
                    MainActivity.CONNECT_DEVICE);
        }
    }

    @Override
    public void usbAutoConn(UsbManager manager) {
    }

    public BroadcastReceiver getMyReceiver() {
        return myReceiver;
    }


}
