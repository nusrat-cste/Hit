package com.example.nusra.insoledevice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import hk.advanpro.android.sdk.AdvanproAndroidSDK;
import hk.advanpro.android.sdk.commons.AConfig;
import hk.advanpro.android.sdk.device.Device;
import hk.advanpro.android.sdk.device.ble.BLEDevice;
import hk.advanpro.android.sdk.device.ble.BLEDeviceManager;
import hk.advanpro.android.sdk.device.ble.BLEInsoleDevice;
import hk.advanpro.android.sdk.device.ble.DefaultBLEDevice;
import hk.advanpro.android.sdk.device.callback.DeviceCallbackException;
import hk.advanpro.android.sdk.device.callback.DeviceCommandCallback;
import hk.advanpro.android.sdk.device.callback.DeviceConnectCallback;
import hk.advanpro.android.sdk.device.callback.DeviceEventCallback;
import hk.advanpro.android.sdk.device.callback.DeviceManagerScanCallback;
import hk.advanpro.android.sdk.device.callback.MainThreadDeviceCommandCallback;
import hk.advanpro.android.sdk.device.callback.MainThreadDeviceEventCallback;
import hk.advanpro.android.sdk.device.enumeration.ConnectType;
import hk.advanpro.android.sdk.device.enumeration.DeviceType;
import hk.advanpro.android.sdk.device.params.ble.insole.BLEInsoleHistoryStepCommandParams;
import hk.advanpro.android.sdk.device.params.ble.insole.BLEInsoleSyncTimeCommandParams;
import hk.advanpro.android.sdk.device.result.DeviceConnectResult;
import hk.advanpro.android.sdk.device.result.DeviceEventResult;
import hk.advanpro.android.sdk.device.result.ble.BLEDeviceScanResult;
import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleHistoryStepCommandResult;
import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleRealGaitEventResult;
import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleRealStepEventResult;
import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleSyncTimeCommandResult;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Insole" ;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION= 241;
    private ArrayList<BLEDeviceScanResult> Devices = new ArrayList<>();
    private ArrayList<Device> _ConnectedDevices = new ArrayList<>();
    public Device device1;
    public Device device2;
    TextView connectdevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       writeToDB();
    //Initializing the sdk
        AdvanproAndroidSDK.init(getApplicationContext());
        //set advanpro android sdk config
        AConfig config = AdvanproAndroidSDK.getConfig();
        //enable print debug log
        config.setDebugLog(true);
        AdvanproAndroidSDK.setConfig(config);
        // end of Initializing the sdk
        connectdevices=findViewById(R.id.tv_connection_prompt);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

        boolean enable = AdvanproAndroidSDK.getDeviceManager(BLEDeviceManager.class).isEnable(ConnectType.BLE);
        final BLEDeviceManager manager = AdvanproAndroidSDK.getDeviceManager(BLEDeviceManager.class);
        Log.e("Useless","Hello world");
        try {
            manager.scan(5, new DeviceManagerScanCallback<BLEDeviceScanResult>(){
                @Override
                public void onScanning(BLEDeviceScanResult result) {
                    Log.d(TAG, String.format("Detected Device Name: %s ",result.getRecord().getLocalName()));
                    //Return only insoles device type
    //                Log.d(TAG, String.format("Detected Device Name: %s, Address: %s,Type:%s",
    //                        result.getRecord().getLocalName(), result.getRecord().getAddress(),
    //                            result.getRecord().getManufacturer().getType()));

                    if(result.getRecord().getManufacturer().getType().toString().equals("Insole")){
                        Devices.add(result);
                    }
                }
                @Override
                public void onStop() {

                    Log.d(TAG, "stop scan");
                    device1 = Devices.get(0).create();
                    device2 = Devices.get(1).create();
//
                    connectdevices.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectToDevices();
                        }
                    });
                }
                @Override
                public void onError(DeviceCallbackException error) {
                    Log.d(TAG, error.getCause().getMessage());
                    error.printStackTrace();
                    //handler err...
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            onStop();
        }


    }

    private void connectToDevices() {
        final Intent intent = (new Intent(getApplicationContext(),ConnectedDeviceActivity.class));
        final Bundle bundle = new Bundle();

                        if(device1.isConnected()==false)
                        {
                            device1.connect(new DeviceConnectCallback() {
                                @Override
                                public void onSucceed(Device device, DeviceConnectResult result) {
                                    Log.d(TAG,"The connection is successful");
                                    _ConnectedDevices.add(device1);

                                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> callback = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                                        @Override
                                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                                        {
                                            Log.d("InsoleD","stepcount Device1"+ data.getWalkStep());
                                        }
                                    };
                                    device1.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);

                                    //gets the data from the device like step counts...
                                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> cb = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                                        @Override
                                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                                        {
// Notice: The callback has in the main thread
                                            Log.d(TAG,"step "+data.getWalkStep()+" gait "+data.getGait()+" isrun "+data.getIsRun());
                                            Log.d(TAG,"status is "+data.getStatus());

// Do something...
                                        }
                                    };
                                    device1.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, cb);

                                    MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult> cb2 = new MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult>() {
                                        @Override
                                        public void onMainThreadData(Device device,BLEInsoleRealGaitEventResult data)
                                        {
                                            Log.d(TAG,"step A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD());
                                            Log.d(TAG,"step "+data.getVarus()+" forefoot "+data.getForefoot()+" sole "+data.getSole());
                                        }
                                    };
                                    device1.on(BLEInsoleDevice.EVENT_INSOLE_REAL_GAIT, cb2);
//Cancel to monitor
                                    //device.un(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);
                                    if(device2.isConnected()){
                                        //startActivity(intent);
                                        connectdevices.setText("Connected");
                                    }
                                }

                                @Override
                                public void onError(Device device, DeviceCallbackException e) {
                                    Log.d(TAG,"The connection failed");
                                }
                            });
                        }
                        else if(device1.isConnected()){
                            Log.d(TAG,"device1 is already connected");
                        }

                        if(device2.isConnected()==false)
                        {
                            device2.connect(new DeviceConnectCallback() {
                            @Override
                            public void onSucceed(Device device2, DeviceConnectResult result) {
                                Log.d(TAG,"The connection is successful");
                                _ConnectedDevices.add(device2);

                                MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> callback = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                                    @Override
                                    public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                                    {
                                        Log.d("InsoleD","stepcount Device1"+ data.getWalkStep());
                                    }
                                };
                                device2.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);
//gets the data from the device like step counts...
                                MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> cb = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                                    @Override
                                    public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                                    {
// Notice: The callback has in the main thread
                                        Log.d(TAG,"step "+data.getWalkStep()+" gait "+data.getGait()+" isrun "+data.getIsRun());
                                    }
                                };
                                device2.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, cb);

                                MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult> cb2 = new MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult>() {
                                    @Override
                                    public void onMainThreadData(Device device,BLEInsoleRealGaitEventResult data)
                                    {
                                        Log.d(TAG,"step A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD());
                                        Log.d(TAG,"step "+data.getVarus()+" forefoot "+data.getForefoot()+" sole "+data.getSole());
                                    }
                                };
                                device2.on(BLEInsoleDevice.EVENT_INSOLE_REAL_GAIT, cb2);

                                if(device1.isConnected()){
                                    //startActivity(intent);
                                    connectdevices.setText("Connected");
                                }
                            }

                            @Override
                            public void onError(Device device2, DeviceCallbackException error) {
                                Log.d(TAG, "The connection failed");
                                // The connection is fails...
                            }
                        });
                        }
                        else if(device2.isConnected()){
                            Log.d(TAG,"device2 is already connected");
//                        device2.on(DefaultBLEDevice.EVENT_BATTERY_CHANGE, new DeviceEventCallback() {
//                            @Override
//                            public void onData(Device device,DeviceEventResult data) {
//                                Log.d(TAG, "Electricity changes： "+device.getInfo(DefaultBLEDevice.BLE_DEVICE_INFO_BATTERY));
//                                //Note: update the UI need to switch to the main thread
//                            }
//                        });
                        }
    }

    private void writeToDB() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("data");

        myRef.child("InsoleLeft").setValue("My left one");
        myRef.child("InsoleRight").setValue("My right one");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("tag", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("tag", "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"Permission granted! Bluetooth device scan started",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Permission denied,this application requires the location permission to perform the scan",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
    