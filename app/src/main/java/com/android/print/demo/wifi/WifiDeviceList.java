package com.android.print.demo.wifi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.print.demo.R;
import com.android.print.demo.permission.EasyPermission;
import com.android.print.sdk.wifi.WifiAdmin;

import java.util.List;

public class WifiDeviceList extends Activity implements EasyPermission.PermissionCallback{
    private static final String TAG = "DeviceListActivity";
    private Button scanButton;
    private Button backButton;
    private WifiAdmin mWifiAdmin;

    private Context context;
    private ArrayAdapter<String> deviceArrayAdapter;
    private ListView mFoundDevicesListView;

    private WifiManager mWifiManager;

    // 扫描结果列表
    private List<ScanResult> list;
    private ScanResult mScanResult;

    private String ssid;
    private String pswd;
    private int mkey;
    private String address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        setTitle(R.string.select_device);


        setResult(Activity.RESULT_CANCELED);

        initView();


        hasScanPermissions();
    }


    String[] permisions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    private void hasScanPermissions() {
        //判断是否有权限
        if (EasyPermission.hasPermissions(context, permisions)) {
            //startScan();
        } else {
            EasyPermission.with(this)
                    .rationale("程序扫描WIFI需要权限")
                    .addRequestCode(1)
                    .permissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .request();
        }
    }


    private void initView() {
        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getAllNetWorkList();
            }
        });

        backButton = (Button) findViewById(R.id.button_bace);
        backButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });


        deviceArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_item);

        mFoundDevicesListView = (ListView) findViewById(R.id.paired_devices);
        mFoundDevicesListView.setAdapter(deviceArrayAdapter);
        mFoundDevicesListView.setOnItemClickListener(mDeviceClickListener);

        mWifiAdmin = new WifiAdmin(WifiDeviceList.this);



		/*Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.getName() + " ( "
						+ getResources().getText(R.string.has_paired) + " )"
						+ "\n" + device.getAddress());
			}
		}*/
    }


    private void registerWIFI() {
        IntentFilter mWifiFilter = new IntentFilter();
        mWifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiConnectReceiver, mWifiFilter);
    }

    private BroadcastReceiver mWifiConnectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Wifi onReceive action = " + intent.getAction());
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int message = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                Log.d(TAG, "liusl wifi onReceive msg=" + message);
                switch (message) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.d(TAG, "WIFI_STATE_DISABLED");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.d(TAG, "WIFI_STATE_DISABLING");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d(TAG, "WIFI_STATE_ENABLED");

/*	             mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
                DhcpInfo dhcpinfo = mWifiManager.getDhcpInfo();

	            address= mWifiAdmin.intToIp(dhcpinfo.serverAddress);

	            returnToPreviousActivity(address);
	            unregisterReceiver(mWifiConnectReceiver);*/

                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.d(TAG, "WIFI_STATE_ENABLING");
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        Log.d(TAG, "WIFI_STATE_UNKNOWN");
                        break;
                    default:
                        break;
                }
            }
        }
    };


    private void returnToPreviousActivity(String address) {

        Intent intent = new Intent();
        intent.putExtra("ip_address", address);
        Log.v("ipaddress", address);
        intent.putExtra("device_name", ssid);
        setResult(Activity.RESULT_OK, intent);


        finish();
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long arg3) {
            ScanResult mScanResult1 = (ScanResult) list.get(position);
            ssid = mScanResult1.SSID;


            String mkey1 = mScanResult1.capabilities;
            if (mkey1.indexOf("NOPASS") > 0) {
                mkey = 1;
            } else if (mkey1.indexOf("WEP") > 0) {
                mkey = 2;
            } else if (mkey1.indexOf("WPA") > 0) {
                mkey = 3;
            }


            showDialog_Layout(WifiDeviceList.this);
        }
    };


    private void showDialog_Layout(Context context) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View textEntryView = inflater.inflate(
                R.layout.ip_address_edit, null);
        final EditText edtInput = (EditText) textEntryView.findViewById(R.id.edtInput);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.password);
        builder.setView(textEntryView);
        builder.setPositiveButton(R.string.yesconn, new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                pswd = edtInput.getText().toString();
                if (pswd != null && !pswd.equals("")) {
                    registerWIFI();
                    WifiAdmin wifiAdmin = new WifiAdmin(WifiDeviceList.this);
                    boolean is = wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(ssid, pswd, mkey));
                    if (is) {
                        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                        DhcpInfo dhcpinfo = mWifiManager.getDhcpInfo();

                        address = mWifiAdmin.intToIp(dhcpinfo.serverAddress);

                        returnToPreviousActivity(address);
                        unregisterReceiver(mWifiConnectReceiver);
                    } else {
                        returnToPreviousActivity("");
                        unregisterReceiver(mWifiConnectReceiver);
                    }

                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create();
        final AlertDialog dialog = builder.show();


    }


    public void getAllNetWorkList() {

        deviceArrayAdapter.clear();
        //开始扫描网络

        mWifiAdmin.startScan();
        list = mWifiAdmin.getWifiList();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                //得到扫描结果
                mScanResult = list.get(i);
                deviceArrayAdapter.add(mScanResult.SSID + "\n " + mScanResult.capabilities + "\n");

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionDenied(int requestCode, List<String> perms) {
        Toast.makeText(context, "程序未授权,扫描失败", Toast.LENGTH_SHORT).show();

        EasyPermission.checkDeniedPermissionsNeverAskAgain(
                this,
                "扫描设备需要开启权限，请在应用设置开启权限。",
                R.string.gotoSettings, R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, perms);
    }
}
