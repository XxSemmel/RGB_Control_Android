package de.rgb_control.helper;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import androidx.annotation.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BLE {

    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic rgb_characteristic;
    BluetoothGattService color_service;
    private boolean onServiceInit = false;
    private int lastColor;

    public BLE(BluetoothGatt gatt){
        this.gatt = gatt;


        gatt.discoverServices();

    }

    public void initServices(){
        color_service= gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
        rgb_characteristic = color_service.getCharacteristic(UUID.fromString("0bca6a30-1418-4fd9-a7d2-950beb793e86"));
        onServiceInit = true;
    }


    public void sendColor(int color, boolean ignoreSameColor){

        if(onServiceInit){


                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;

                String data = String.valueOf(red)+ " "+ String.valueOf(green)+ " "+ String.valueOf(blue);




                rgb_characteristic.setValue(data);
                gatt.writeCharacteristic(rgb_characteristic);
                lastColor=color;


        }


    }

    public void sendColor(int color){

        if(onServiceInit){

            if(lastColor!=color){

                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;

                String data = String.valueOf(red)+ " "+ String.valueOf(green)+ " "+ String.valueOf(blue);




                rgb_characteristic.setValue(data);
                gatt.writeCharacteristic(rgb_characteristic);
                lastColor=color;
            }

        }


    }

    public void sendColor(String color){

        if(onServiceInit){

            rgb_characteristic.setValue(color);
            gatt.writeCharacteristic(rgb_characteristic);

        }


    }

    public void turnOff(){
        sendColor("000 000 000");
    }

    public void turnOn(int color){
        sendColor(color, true);
    }




}
