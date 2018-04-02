package com.android.print.demo;


import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Handler;

import com.android.print.sdk.PrinterInstance;

public interface IPrinterOpertion {
	public void open(Intent data);

	public void close();

	public void chooseDevice();

	public PrinterInstance getPrinter();

	public void usbAutoConn(UsbManager manager);

	public void btAutoConn(Context context, Handler mHandler);


}
