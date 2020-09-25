package de.rgb_control.helper;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import java.util.UUID;

public class BLE {

    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic rgb_characteristic;
    private BluetoothGattCharacteristic neopixels_characteristic;
    private BluetoothGattCharacteristic effect_characteristic;
    private BluetoothGattCharacteristic reboot;
    private BluetoothGattCharacteristic name;
    private boolean iseffectrunning = false;
    private boolean onServiceInit = false;
    private int lastColor;

    public BLE(BluetoothGatt gatt){
        this.gatt = gatt;


        gatt.discoverServices();

    }

    public void initServices(){
        BluetoothGattService color_service = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
        rgb_characteristic = color_service.getCharacteristic(UUID.fromString("0bca6a30-1418-4fd9-a7d2-950beb793e86"));
        BluetoothGattService settings_service = gatt.getService(UUID.fromString("cfa34f22-6f5a-4587-95af-bc4cc057610b"));
        neopixels_characteristic = settings_service.getCharacteristic(UUID.fromString("99cf3c7b-0f41-4adc-ba90-0b5c6242af06"));
        reboot = settings_service.getCharacteristic(UUID.fromString("8b9787e5-8192-4104-a87e-a4fa80c304e6"));
        name=settings_service.getCharacteristic(UUID.fromString("3aa67643-5d02-4278-9db0-a95993c011d7"));
        effect_characteristic = color_service.getCharacteristic(UUID.fromString("8e341dc8-2907-49af-9e34-9da998eec5fc"));

        onServiceInit = true;
    }


    public void sendColor(int color, boolean ignoreSameColor){

        if(onServiceInit){
            if(iseffectrunning){
                stopEffect();
            }


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

            if(iseffectrunning){
                stopEffect();
            }

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


    public void sendColor(boolean waitforinit, final int color){

        if(onServiceInit){

            if(iseffectrunning){
                stopEffect();
            }


                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;

                String data = String.valueOf(red)+ " "+ String.valueOf(green)+ " "+ String.valueOf(blue);




                rgb_characteristic.setValue(data);
                gatt.writeCharacteristic(rgb_characteristic);
                lastColor=color;


        }
        else {

            Thread thread = new Thread() {
                @Override
                public void run() {
                    while (!onServiceInit){

                    }
                    sendColor(true, color);
                }
            };

            thread.start();


        }


    }

    public void sendColor(String color){

        if(onServiceInit){
            if(iseffectrunning){
                stopEffect();
            }

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

    public void sendNeopixels(String count){
        if(onServiceInit){
            neopixels_characteristic.setValue(count);
            gatt.writeCharacteristic(neopixels_characteristic);
        }

    }

    public void reboot(){
        if(onServiceInit){
            reboot.setValue("1");
            gatt.writeCharacteristic(reboot);
        }
    }

    public void changeDeviceName(String device_name){
        if(onServiceInit){
            name.setValue(device_name);
            gatt.writeCharacteristic(name);
        }
    }

    public int getColor(){
        return lastColor;
    }


    public void sendEffect(Effects effect){
        if(onServiceInit){
            switch (effect){
                case RAINBOW:
                    effect_characteristic.setValue("Rainbow");
                    break;
                case FIRE:
                    effect_characteristic.setValue("Fire");
                    break;
                case RUNNING_LIGHTS:
                    effect_characteristic.setValue("Running Lights");
                    break;


            }
            gatt.writeCharacteristic(effect_characteristic);
            iseffectrunning=true;
        }

    }

    public void stopEffect(){
        if(onServiceInit){
            effect_characteristic.setValue("stop");
            gatt.writeCharacteristic(effect_characteristic);
            iseffectrunning=false;
        }
    }


    public enum Effects{
        RAINBOW,
        RUNNING_LIGHTS,
        FIRE
    }



}
