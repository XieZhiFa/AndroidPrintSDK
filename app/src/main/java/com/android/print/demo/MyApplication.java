package com.android.print.demo;

import android.app.Application;

import com.android.print.sdk.util.Utils;

import java.util.Properties;

/**
 * 应用程序
 * Created by Android on 2018/3/30.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


        initPrintSDK();
    }



    //sdk 防止第一次运行程序, 闪退问题
    private void initPrintSDK(){
        try {
            Properties pro = Utils.getBtConnInfo(this);
            if(pro.isEmpty()){
                pro.put("mac", "");
            }
        } catch (Exception e) {
            Utils.saveBtConnInfo(this, "");
        }
    }
}
