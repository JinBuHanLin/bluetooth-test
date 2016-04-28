package com.kcoppock.bluetoothconnector;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.jinlin.bluetoothexample.R;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Copyright 2013 Kevin Coppock
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Main activity which handles the flow of connecting to the requested Bluetooth A2DP device
 */
public class BluetoothActivity extends Activity implements BluetoothBroadcastReceiver.Callback, BluetoothA2DPRequester.Callback {
    Button searchBluetooth,connBluetooth;
    ListView pairedListView,newDevicesListView;
    TextView m_tvDeviceName;
    private static final String TAG = "BluetoothActivity";
    private static final String HTC_MEDIA = "ATH-S700BT";//This is the name of the device to connect to. You can replace this with the name of your device.
    private BluetoothAdapter mAdapter;//Local reference to the device's BluetoothAdapter
    //private ListView listView;
    private ArrayList listBle;
    private ArrayAdapter listAdapter;
    private Set<BluetoothDevice> devicesArray;
    private BluetoothActivity bluetoothActivity;
    private String clickName;
    private User userdata;
    ArrayAdapter mPairedDevicesArrayAdapter ;
    ArrayAdapter mNewDevicesArrayAdapter ;
    private final int REQUEST_ENABLE = 0;

    //    BluetoothService bluetoothService;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Context mContext=BluetoothActivity.this;
        bluetoothActivity = this;
        searchBluetooth = (Button) findViewById(R.id.searchBluetooth);
        connBluetooth = (Button) findViewById(R.id.connBluetooth);
        connBluetooth.setEnabled(true);
        m_tvDeviceName= (TextView) findViewById(R.id.m_tvDeviceName);
        mAdapter = BluetoothAdapter.getDefaultAdapter();//Store a local reference to the BluetoothAdapter
//        bluetoothService = new BluetoothService(this, mHandler);
//        if(!mAdapter.isEnabled()){
//            //如果没有启用，发出提示进行启用。
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent,REQUEST_ENABLE);
//            //当然你也可以不提示，强行打开（如果没有root权限，系统会提示获取蓝牙的root权限）
//            //mAdapter.enable();
//
//        }else if (mChatService == null){
//            //初始化连接线程
//            setupChat();
//        }

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        pairedListView = (ListView) findViewById(R.id.pairedListView);
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();// Get a set of currently paired devices
        if (pairedDevices.size() > 0) {// If there are paired devices, add each one to the ArrayAdapter
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        //mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        //listAdapter=new ArrayAdapter<String[]>(this,android.R.layout.simple_list_item_1,0);
        //listBle = new ArrayList();
        //UserAdapter adapterble = new UserAdapter(this, getPairedDevicesBle(mAdapter));
        //newDevicesListView.setAdapter(adapterble);//Add new adapter for ble device scan
        newDevicesListView = (ListView) findViewById(R.id.newDevicesListView);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
//            @Override
//            public void onItemClick(AdapterView<?> parent, final View view,int position, long id) {
//                mAdapter.cancelDiscovery();// Cancel discovery because it's costly and we're about to connect
//                final User item = (User) parent.getItemAtPosition(position);
//                view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        view.setAlpha(1);
//                        Toast.makeText(view.getContext(), "you click " + item.name, Toast.LENGTH_LONG).show();
//                        clickName = item.address;
//                        if (mAdapter.isEnabled()) {//Already connected, skip the rest
//                            onBluetoothConnected();
//                            return;
//                        }
//                        if (mAdapter.enable()) {//Check if we're allowed to enable Bluetooth. If so, listen for a successful enabling
//                            BluetoothBroadcastReceiver.register(bluetoothActivity, bluetoothActivity);
//                        } else {
//                            Log.e(TAG, "Unable to enable Bluetooth. Is Airplane Mode enabled?");
//                        }
//                    }
//                });
//            }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

    }
    public static String EXTRA_DEVICE_ADDRESS = "device_address";// Return Intent extra
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, final View view, int position, long arg3) {
//            mAdapter.cancelDiscovery();//Cancel discovery because it's costly and we're about to connect
//            String info = ((TextView) view).getText().toString();// Get the device MAC address, which is the last 17 chars in the View
//            String address = info.substring(info.length() - 17);
//            Intent intent = new Intent();//Create the result Intent and include the MAC address
//            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
//            setResult(Activity.RESULT_OK, intent);// Set result and finish this Activity
//            finish();
            //BluetoothDevice device = mAdapter.getRemoteDevice(address);
            //bluetoothService.connect(device);
//            mAdapter.cancelDiscovery();// Cancel discovery because it's costly and we're about to connect
//            final User item =  parent.getItemAtPosition(position);
//            view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
//                @Override
//                public void run() {
//                    view.setAlpha(1);
//                    Toast.makeText(view.getContext(), "you click " + item.name, Toast.LENGTH_LONG).show();
//                    clickName = item.address;
//                    if (mAdapter.isEnabled()) {//Already connected, skip the rest
//                        onBluetoothConnected();
//                        return;
//                    }
//                    if (mAdapter.enable()) {//Check if we're allowed to enable Bluetooth. If so, listen for a successful enabling
//                        BluetoothBroadcastReceiver.register(bluetoothActivity, bluetoothActivity);
//                    } else {
//                        Log.e(TAG, "Unable to enable Bluetooth. Is Airplane Mode enabled?");
//                    }
//                }
//            });
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { // When discovery finds a device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);// Get the BluetoothDevice object from the Intent
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {// If it's already paired, skip it, because it's been listed already
                    Log.i("BluetoothActivity", "devie 蓝牙名称:" + device.getName() + ",蓝牙地址：" + device.getAddress());
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mNewDevicesArrayAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {// When discovery is finished, change the Activity title
                Log.i("BluetoothActivity", "device : 搜索结束");
                m_tvDeviceName.setText("搜索结束");
                connBluetooth.setEnabled(true);
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
    //初始化连接线程
    private BluetoothChatService mChatService = null;
    private void setupChat() {
        mChatService = new BluetoothChatService(this, mHandler);
    }
    public void OnBtnClick(View v){
        switch (v.getId()){
            case R.id.searchBluetooth:
                m_tvDeviceName.setText("正在搜索中。。。");
                connBluetooth.setEnabled(false);
                setProgressBarIndeterminateVisibility(true);
                setTitle(R.string.scanning);
                if(!mAdapter.isEnabled()){
                    //如果没有启用，发出提示进行启用。
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent,REQUEST_ENABLE);
                    //当然你也可以不提示，强行打开（如果没有root权限，系统会提示获取蓝牙的root权限）
                    //mAdapter.enable();
                }else if (mChatService == null){
                    //初始化连接线程
                    setupChat();
                }
                if (mAdapter.isDiscovering()) {// If we're already discovering, stop it
                    mAdapter.cancelDiscovery();
                }
                if(!mNewDevicesArrayAdapter.isEmpty()||mNewDevicesArrayAdapter!=null){
                    mNewDevicesArrayAdapter.clear();
                }
                mAdapter.startDiscovery();// Request discover from BluetoothAdapter
                break;
            case R.id.connBluetooth:
                m_tvDeviceName.setText("发送连接");
                BluetoothSocket tmp = null;
                Log.i("BluetoothActivity","connBluetooth click ");
                if(mAdapter.isDiscovering()){
                    mAdapter.cancelDiscovery();
                }
                //BluetoothDevice device=mAdapter.getRemoteDevice("00:00:05:06:25:88");
                BluetoothDevice device=mAdapter.getRemoteDevice("00:0E:0E:02:0F:C2");
                new ConnectThread(device).start();
//                if (mPairedDevicesArrayAdapter.getCount() > 0) {
//                    String address = mPairedDevicesArrayAdapter.getItem(0).toString();
//                    address=address.substring(address.length() - 17);
//                    Log.i("BluetoothActivity","address is "+address);
//                    BluetoothDevice device = mAdapter.getRemoteDevice(address);
//                    clickName = address;
//                    onBluetoothConnected();
//                    mChatService.connect(device,true);
//                    BluetoothService bluetoothService= (BluetoothService) getSystemService(BLUETOOTH_SERVICE);
//                    bluetoothService.connect(device);
//                    return;
//                }
//                if (mNewDevicesArrayAdapter.getCount() > 0) {
//                    String noDevices = getResources().getText(R.string.none_found).toString();
//                    mNewDevicesArrayAdapter.add(noDevices);
//                }
//                if (mAdapter.enable()) {//Check if we're allowed to enable Bluetooth. If so, listen for a successful enabling
//                    BluetoothBroadcastReceiver.register(bluetoothActivity, bluetoothActivity);
//                } else {
//                    Log.e(TAG, "Unable to enable Bluetooth. Is Airplane Mode enabled?");
//                }
//                ba.getProfileProxy(context, bs, BluetoothProfile.A2DP);
//                ba.getProfileProxy(context, bs, BluetoothProfile.HEADSET);

                break;
        }
    }
    BluetoothHeadset bh;
    BluetoothA2dp a2dp;
    //    BluetoothProfile.ServiceListener bs = new BluetoothProfile.ServiceListener() {
//        @Override
//        public void onServiceConnected(int profile, BluetoothProfile proxy) {
//            Log.i("log", "onServiceConnected");
//            try {
//                if (profile == BluetoothProfile.HEADSET) {
//                    bh = (BluetoothHeadset) proxy;
//                    if (bh.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED){
//                        bh.getClass().getMethod("connect", BluetoothDevice.class).invoke(bh, device);
//                    }
//                } else if (profile == BluetoothProfile.A2DP) {
//                    a2dp = (BluetoothA2dp) proxy;
//
//                    if (a2dp.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED){
//                        a2dp.getClass().getMethod("connect", BluetoothDevice.class).invoke(a2dp, device);
//                    }
//                }
//                if (bh != null&&a2dp != null) {
//                    A2dpConnectionThread.stop = false;
//                    new A2dpConnectionThread(context, device, a2dp, bh).start();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        @Override
//        public void onServiceDisconnected(int profile) {
//        }
//    };
    class  A2dpConnectionThread extends Thread{
        Context context;
        BluetoothDevice device;
        BluetoothA2dp a2dp;
        BluetoothHeadset bh;
        public A2dpConnectionThread(Context context,BluetoothDevice device,BluetoothA2dp a2dp,BluetoothHeadset bh){
            this.context=context;
            this.device=device;
            this.a2dp=a2dp;
            this.bh=bh;


        }
        public void run(){

        }
    }
    //    public static final UUID MY_UUID=UUID.fromString("");
    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private class ConnectThread extends Thread {
        BluetoothSocket mmSocket;
        BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {// Use a temporary object that is later assigned to mmSocket,because mmSocket is final
            BluetoothSocket tmp = null;
            this.mmDevice = device;

            if(device.getBondState()==BluetoothDevice.BOND_NONE){
                Log.i("BluetoothActivity","蓝牙未配对");
            }else {
                Log.i("BluetoothActivity","蓝牙配对");
            }
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {  // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.i("BluetoothActivity","产生BluetoothSocket");
            } catch (IOException e) { Log.i("BluetoothActivity","未成功产生BluetoothSocket"); }
            this.mmSocket = tmp;
        }
        public void run() { // Cancel discovery because it will slow down the connection
            if(mBluetoothAdapter!=null) {
                Log.i("BluetoothActivity", "本地蓝牙适配器不为null");
                if (mBluetoothAdapter.isDiscovering()) {
                    Log.i("BluetoothActivity", "本地蓝牙适配器仍在扫描");
                    mBluetoothAdapter.cancelDiscovery();
                    Log.i("BluetoothActivity", "停止扫描");
                }
            }
            try { // Connect the device through the socket. This will block until it succeeds or throws an exception
                Log.i("BluetoothActivity","开始连接");
                mmSocket.connect();
                Log.i("BluetoothActivity", "连接成功");
            } catch (IOException connectException) {// Unable to connect; close the socket and get out
                Log.i("BluetoothActivity", "连接失败");
                cancel();
                connectException.printStackTrace();
                return;
            }
            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }
        public void cancel() {//Will cancel an in-progress connection, and close the socket
            try {
                Log.i("BluetoothActivity", "关闭连接。。");
                mmSocket.close();
                Log.i("BluetoothActivity", "关闭成功");
            } catch (IOException e) {
                Log.i("BluetoothActivity", "关闭失败");
            }
        }
    }
    BluetoothA2dp mBluetoothHeadset;
    //    BluetoothHeadset mBluetoothHead;
    BluetoothAdapter mBluetoothAdapter;
    //    private BluetoothService mBluetoothLeService;
    public void connBluetoothA2DP(){
        //BluetoothHeadset mBluetoothHeadset;
        // Get the default adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Establish connection to the proxy.
        mBluetoothAdapter.getProfileProxy(BluetoothActivity.this, mProfileListener, BluetoothProfile.A2DP);
    }
    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                mBluetoothHeadset = (BluetoothA2dp) proxy;
            }
            //System.out.println(mBluetoothHeadset.connect(mDevice));//
            //System.out.println(mBluetoothHeadset.isA2dpPlaying(mDevice));
            //System.out.println(mBluetoothHeadset.getPriority(mDevice));
//            mBluetoothLeService.connect(mBluetoothHeadset);
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
            }
        }
    };
    private ArrayAdapter<String[]> getPairedDevices(BluetoothAdapter mBleAdapter) {
        devicesArray=mBleAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray) {
                String[] additem ={ device.getName(), device.getAddress()};
                listAdapter.add(additem);
            }
        }
        return listAdapter;
    }
    private ArrayList<User> getPairedDevicesBle(BluetoothAdapter mBleAdapter) {
        devicesArray=mBleAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray) {
                userdata = new User(device.getName(),device.getAddress());
                //String[] additem ={ device.getName(), device.getAddress()};
                listBle.add(userdata);
            }
        }
        return listBle;
    }
    @Override
    public void onBluetoothError () {
        Log.i("BluetoothActivity","onBluetoothError 方法");
        Log.e(TAG, "There was an error enabling the Bluetooth Adapter.");
    }
    @Override
    public void onBluetoothConnected () {
        Log.i("BluetoothActivity","onBluetoothConnected 方法");
        new BluetoothA2DPRequester(this).request(this, mAdapter);
    }
    @Override
    public void onA2DPProxyReceived (BluetoothA2dp proxy) {
        Log.i("BluetoothActivity","onA2DPProxyReceived 方法");
        Method connect = getConnectMethod();
        BluetoothDevice device = findBondedDeviceByAddress(mAdapter, clickName);
        if (connect == null || device == null) {//If either is null, just return. The errors have already been logged
            return;
        }
        try {
            connect.setAccessible(true);
            connect.invoke(proxy, device);
        } catch (InvocationTargetException ex) {
            Log.e(TAG, "Unable to invoke connect(BluetoothDevice) method on proxy. " + ex.toString());
        } catch (IllegalAccessException ex) {
            Log.e(TAG, "Illegal Access! " + ex.toString());
        }
    }
    /**
     * Wrapper around some reflection code to get the hidden 'connect()' method
     * @return the connect(BluetoothDevice) method, or null if it could not be found
     */
    private Method getConnectMethod () {
        Log.i("BluetoothActivity","getConnectMethod 方法");
        try {
            return BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
        } catch (NoSuchMethodException ex) {
            Log.e(TAG, "Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.");
            return null;
        }
    }
    /**
     * Search the set of bonded devices in the BluetoothAdapter for one that matches
     * the given name
     * @param adapter the BluetoothAdapter whose bonded devices should be queried
     * @param //name the name of the device to search for
     * @return the BluetoothDevice by the given name (if found); null if it was not found
     */
    private static BluetoothDevice findBondedDeviceByAddress (BluetoothAdapter adapter, String address) {
        for (BluetoothDevice device : getBondedDevices(adapter)) {
            if (address.matches(device.getAddress())) {
                Log.v(TAG, String.format("Found device with name %s and address %s.", device.getName(), device.getAddress()));
                return device;
            }
        }
        Log.w(TAG, String.format("Unable to find device with address %s.", address));
        return null;
    }
    /**
     * Safety wrapper around BluetoothAdapter#getBondedDevices() that is guaranteed
     * to return a non-null result
     * @param adapter the BluetoothAdapter whose bonded devices should be obtained
     * @return the set of all bonded devices to the adapter; an empty set if there was an error
     */
    private static Set<BluetoothDevice> getBondedDevices (BluetoothAdapter adapter) {
        Set<BluetoothDevice> results = adapter.getBondedDevices();
        if (results == null) {
            results = new HashSet<BluetoothDevice>();
        }
        return results;
    }
    //蓝牙连接线程
    private String mConnectedDeviceName = "";
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //蓝牙连接状态改变时
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        //已连接
                        case BluetoothChatService.STATE_CONNECTED:

                            //显示连接设备
                            m_tvDeviceName.setText("连接设备: " +mConnectedDeviceName);
                            break;
                        //连接中
                        case BluetoothChatService.STATE_CONNECTING:
                            m_tvDeviceName.setText("正在连接中。。。");
                            break;
                        //未连接成功
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            m_tvDeviceName.setText("未连接设备");
                            Toast.makeText(BluetoothActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                //连接成功，获取设备名称
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
                //连接过程中发来的消息
                case Constants.MESSAGE_TOAST:
                    if (null != BluetoothActivity.this) {
                        Toast.makeText(BluetoothActivity.this, msg.getData().getString(Constants.TOAST),Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {// Make sure we're not doing discovery anymore
            mAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP,mBluetoothHeadset);
        this.unregisterReceiver(mReceiver);// Unregister broadcast listeners
    }
}
