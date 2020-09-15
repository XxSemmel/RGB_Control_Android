package de.rgb_control;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.rgb_control.devicelist.Data;
import de.rgb_control.helper.Helper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BT_ENABLE = 0;
    private BluetoothAdapter mBluetoothAdapter  = null;
    private List<BluetoothDevice> scanResults = new ArrayList<BluetoothDevice>();
    private MenuItem loading_indicator;

    private ArrayList<Data> devices;
    private ListView listView;
    private de.rgb_control.CustomAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    BluetoothLeScanner mBluetoothLeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devices = new ArrayList<>();

        listView = findViewById(R.id.device_list);

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        adapter = new de.rgb_control.CustomAdapter(devices, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(listviewOnItemClick);

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        refreshLayout = findViewById(R.id.updateDeviceListLayout);

        refreshLayout.setOnRefreshListener(onRefreshListener);

        requestPermissions();

        enableBt();

        onBLEScan();




    }

    private void onBLEScan(){
        ScanFilter.Builder builder = new ScanFilter.Builder();

        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setManufacturerData(Integer.parseInt("6f63", 16),Helper.hexStringToByteArray("6E74726F6C")).build());

        mBluetoothLeScanner.startScan(scanFilters, new ScanSettings.Builder().build(),mLeScanCallback);



        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                mBluetoothLeScanner.stopScan(mLeScanCallback);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        loading_indicator.setVisible(false);
                        refreshLayout.setRefreshing(false);

                    }
                });


            }
        }, 3000);


    }


    private ListView.OnItemClickListener listviewOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            int position = i;
            BluetoothDevice device= devices.get(position).device;
            Intent intent = new Intent(getApplicationContext(), MainNavigation.class);
            startActivity(intent);

        }
    };



    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            onBLEScan();
            loading_indicator.setVisible(true);
        }
    };


    private ScanCallback mLeScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, final ScanResult result) {

                    super.onScanResult(callbackType, result);

                    BluetoothDevice device = result.getDevice();


                    if(!scanResults.contains(device)){
                        scanResults.add(device);
                        devices.add(new Data(result.getDevice().getName(), R.drawable.ic_launcher, device));
                        adapter.notifyDataSetChanged();
                    }


                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    loading_indicator.setVisible(false);
                }

            };

    public void enableBt(){
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
        }
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                for(int res : grantResults){
                    if (res!=PackageManager.PERMISSION_GRANTED){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Warning");
                        builder.setMessage("Du musst der App die erwünschten Berechtigungen geben, damit sie mit deinem Gerät kommunizieren kann!");

                        builder.setIcon(R.drawable.ic_warning);
                        builder.setCancelable(false);

                        builder.setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        android.os.Process.killProcess(android.os.Process.myPid()); //exit
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }


        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        loading_indicator = menu.findItem(R.id.loading_indcator);
        loading_indicator.setActionView(R.layout.loading_indicator);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.info:
                de.rgb_control.InfoDialog info = new de.rgb_control.InfoDialog(this, "Diese App dient zur Steuerung einer Lichterkette...", "Information");
                info.show();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }





}