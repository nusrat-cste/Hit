package com.example.nusra.hit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;


import java.util.ArrayList;

import hk.advanpro.android.sdk.AdvanproAndroidSDK;
import hk.advanpro.android.sdk.commons.AConfig;
import hk.advanpro.android.sdk.device.Device;
import hk.advanpro.android.sdk.device.ble.BLEDeviceManager;
import hk.advanpro.android.sdk.device.ble.BLEInsoleDevice;
import hk.advanpro.android.sdk.device.callback.DeviceCallbackException;
import hk.advanpro.android.sdk.device.callback.DeviceConnectCallback;
import hk.advanpro.android.sdk.device.callback.DeviceManagerScanCallback;
import hk.advanpro.android.sdk.device.callback.MainThreadDeviceEventCallback;
import hk.advanpro.android.sdk.device.enumeration.ConnectType;
import hk.advanpro.android.sdk.device.result.DeviceConnectResult;
import hk.advanpro.android.sdk.device.result.ble.BLEDeviceScanResult;
import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleRealGaitEventResult;
import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleRealStepEventResult;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Insole" ;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION= 241;
    private ArrayList<BLEDeviceScanResult> Devices = new ArrayList<>();
    private ArrayList<Device> _ConnectedDevices = new ArrayList<>();
    public Device r_device;
    public Device l_device;
    TextView connectdevices;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Calendar _calender;
    String lName;
    String rName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    l_device = Devices.get(1).create();
                    if(Devices.get(0).getRecord().getLocalName().equals("6X1CSV"))
                    {
                        r_device = Devices.get(0).create();
                        rName = "6X1CSV";
                        Log.d("Left Right", "Right sole is: " + Devices.get(0).getRecord().getLocalName());
                        writeToDB(Devices.get(0).getRecord().getLocalName(),Devices.get(0).getRecord().getAddress(),"R");
                    }
                    else if(Devices.get(0).getRecord().getLocalName().equals("BBJE8I"))
                    {
                        l_device = Devices.get(0).create();
                        lName = "BBJE8I";
                        Log.d("Left Right", "Left sole is: " + Devices.get(0).getRecord().getLocalName());
                        writeToDB(Devices.get(0).getRecord().getLocalName(),Devices.get(0).getRecord().getAddress(),"L");
                    }


                    if(Devices.get(1).getRecord().getLocalName().equals("BBJE8I"))
                    {
                        l_device = Devices.get(1).create();
                        lName = "BBJE8I";
                        Log.d("Left Right", "Left sole is: " + Devices.get(1).getRecord().getLocalName());
                        writeToDB(Devices.get(1).getRecord().getLocalName(),Devices.get(1).getRecord().getAddress(),"L");
                    }
                    else if(Devices.get(1).getRecord().getLocalName().equals("6X1CSV"))
                    {   
                        r_device = Devices.get(1).create();
                        rName = "6X1CSV";
                        Log.d("Left Right", "Right sole is: " + Devices.get(1).getRecord().getLocalName());
                        writeToDB(Devices.get(1).getRecord().getLocalName(),Devices.get(1).getRecord().getAddress(),"R");
                    }

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
//        final Intent intent = (new Intent(getApplicationContext(),ConnectedDeviceActivity.class));
//        final Bundle bundle = new Bundle();

        if(r_device.isConnected()==false)
        {
            r_device.connect(new DeviceConnectCallback() {
                @Override
                public void onSucceed(Device device, DeviceConnectResult result) {
                    Log.d(TAG,"The connection is successful");
                    _ConnectedDevices.add(r_device);

                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> callback = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                        {
                            Log.d("InsoleD","stepcount Device1"+ data.getWalkStep());
                        }
                    };
                    r_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);

                    //gets the data from the device like step counts...
                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> cb = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                        {
                            writeToDB(lName,data.getWalkStep(),data.getWalkDuration(), data.getRunStep(),data.getRunDuration());
                            Log.d(TAG,"step "+data.getWalkStep()+" gait "+data.getGait()+" isrun "+data.getIsRun());
                            Log.d(TAG,"status is "+data.getStatus());
                        }
                    };
                    r_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, cb);

                    MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult> cb2 = new MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealGaitEventResult data)
                        {
                            Log.d(TAG,"step A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD());
                            Log.d(TAG,"step "+data.getVarus()+" forefoot "+data.getForefoot()+" sole "+data.getSole());
                        }
                    };
                    r_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_GAIT, cb2);
//Cancel to monitor
                    //device.un(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);
                    if(l_device.isConnected()){
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
        else if(r_device.isConnected()){
            Log.d(TAG,"r_device is already connected");
        }

        if(l_device.isConnected()==false)
        {
            l_device.connect(new DeviceConnectCallback() {
                @Override
                public void onSucceed(Device device, DeviceConnectResult result) {
                    Log.d(TAG,"The connection is successful");
                    _ConnectedDevices.add(l_device);

                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> callback = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                        {
                            Log.d("InsoleD","stepcount Device1"+ data.getWalkStep());
                            writeToDB(rName,data.getWalkStep(),data.getWalkDuration(), data.getRunStep(),data.getRunDuration());

                        }
                    };
                    l_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);
//gets the data from the device like step counts...
                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> cb = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                        {
// Notice: The callback has in the main thread
                            Log.d(TAG,"step "+data.getWalkStep()+" gait "+data.getGait()+" isrun "+data.getIsRun());
                        }
                    };
                    l_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, cb);

                    MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult> cb2 = new MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealGaitEventResult data)
                        {
                            Log.d(TAG,"step A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD());
                            Log.d(TAG,"step "+data.getVarus()+" forefoot "+data.getForefoot()+" sole "+data.getSole());
                        }
                    };
                    l_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_GAIT, cb2);

                    if(r_device.isConnected()){
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
        else if(l_device.isConnected()){
            Log.d(TAG,"l_device is already connected");
//                        l_device.on(DefaultBLEDevice.EVENT_BATTERY_CHANGE, new DeviceEventCallback() {
//                            @Override
//                            public void onData(Device device,DeviceEventResult data) {
//                                Log.d(TAG, "Electricity changes： "+device.getInfo(DefaultBLEDevice.BLE_DEVICE_INFO_BATTERY));
//                                //Note: update the UI need to switch to the main thread
//                            }
//                        });
        }
    }

    private void writeToDB(String SoleName,String SoleMac, String Key) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Insole");
        if (Key.equals("L")){
            myRef.child("Left").child("DeviceName").setValue(SoleName);
            myRef.child("Left").child("MacAddress").setValue(SoleMac);
        }
        else if(Key.equals("R")){
            myRef.child("Right").child("DeviceName").setValue(SoleName);
            myRef.child("Right").child("MacAddress").setValue(SoleMac);}
    }

    private void writeToDB(String SoleName,int wc, int wd, int rc, int rd) {

        Calendar  _time= _calender.getInstance();
        database = FirebaseDatabase.getInstance();
        Long _tStamp = _time.getTimeInMillis();
        Log.e("Insole",_tStamp.toString());

        myRef = database.getReference("Data").child(SoleName).child("StepData");

        myRef.child(_tStamp.toString()).child("WalkCount").setValue(wc);
        myRef.child(_tStamp.toString()).child("WalkDuration").setValue(wd);
        myRef.child(_tStamp.toString()).child("RunCount").setValue(rc);
        myRef.child(_tStamp.toString()).child("RunDuration").setValue(rd);

//        myRef.child("Right").child("MacAddress").setValue(SoleMac);
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
