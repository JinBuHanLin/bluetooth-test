package com.kcoppock.bluetoothconnector;
/**
 * BluetoothCallback provides a callback interface for the settings
 * UI to receive events from {@link BluetoothEventManager}.
 */
interface BluetoothCallback {
    void onBluetoothStateChanged(int bluetoothState);
    void onScanningStateChanged(boolean started);
    void onDeviceAdded(CachedBluetoothDevice cachedDevice);
    void onDeviceDeleted(CachedBluetoothDevice cachedDevice);
    void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState);
}
