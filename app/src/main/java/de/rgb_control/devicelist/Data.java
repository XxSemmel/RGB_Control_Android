package de.rgb_control.devicelist;

import android.bluetooth.BluetoothDevice;

public class Data {

   public String Name;
   public int icon;
   public BluetoothDevice device;

   public Data(String Name, int icon, BluetoothDevice device){
       this.Name = Name;
       this.icon = icon;
       this.device = device;
   }

}
