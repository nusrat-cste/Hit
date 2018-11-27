package com.example.nusra.insoledevice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import hk.advanpro.android.sdk.device.Device;
import hk.advanpro.android.sdk.device.ble.DefaultBLEDevice;
import hk.advanpro.android.sdk.device.callback.DeviceEventCallback;
import hk.advanpro.android.sdk.device.result.DeviceEventResult;

public class ConnectedDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);
        Intent intent = getIntent();

        //ArrayList<Device> _connecteddevices = intent.getParcelableExtra("Devices");

       // Device d2 = intent.getParcelableExtra("Device2");

//    if(d1.isConnected()) {
//        d1.on(DefaultBLEDevice.EVENT_BATTERY_CHANGE, new DeviceEventCallback() {
//            @Override
//            public void onData(Device device, DeviceEventResult data) {
//                Log.d("Insole", "Device 1 has " + device.getInfo(DefaultBLEDevice.BLE_DEVICE_INFO_BATTERY).toString() + "% charge");
//                //getBatteryD1=findViewById(R.id.tv_charge_device1);
//                //getBatteryD2.setText(device.getInfo(DefaultBLEDevice.BLE_DEVICE_INFO_BATTERY).toString());
//            }
//        });
//    }
//    else{
//        Log.d("Insole", "not connected");
//    }
//    if(d2.isClose()){
//        d2.on(DefaultBLEDevice.EVENT_BATTERY_CHANGE, new DeviceEventCallback() {
//                            @Override
//                            public void onData(Device device,DeviceEventResult data) {
//                                    //getBatteryD2=findViewById(R.id.tv_charge_device2);
//                                    Log.d("Insole","Device 2 has "+device.getInfo(DefaultBLEDevice.BLE_DEVICE_INFO_BATTERY).toString()+"% charge");
//                                    //getBatteryD2.setText(device.getInfo(DefaultBLEDevice.BLE_DEVICE_INFO_BATTERY).toString());
//                            }
//                        });
//     }
//     else{
//        Log.d("Insole", "not connected");
//    }
    }

}
