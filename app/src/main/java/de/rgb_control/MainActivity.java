package de.rgb_control;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.companion.AssociationRequest;
import android.companion.BluetoothLeDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import de.rgb_control.devicelist.Data;
import de.rgb_control.helper.BLE;
import de.rgb_control.helper.Helper;

public class MainActivity extends AppCompatActivity implements de.rgb_control.CustomAdapter.OnDeviceClickListener{

    public static BLE control;

    private static final int REQUEST_BT_ENABLE = 0;
    private MenuItem loading_indicator;
    private ArrayList<Data> devices;
    private de.rgb_control.CustomAdapter adapter;
    private static final int SELECT_DEVICE_REQUEST_CODE = 42;
    private BluetoothAdapter mBluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devices = new ArrayList<Data>();
        RecyclerView recyclerView = findViewById(R.id.device_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new de.rgb_control.CustomAdapter(devices, this);
        recyclerView.setAdapter(adapter);


        FloatingActionButton add_btn = findViewById(R.id.add_btn);
        add_btn.setOnClickListener(add_btn_click);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        requestPermissions();

        enableBt();

        LoadDevices();



    }

    private void LoadDevices(){
        devices.addAll(getPairedDevices());
        adapter.notifyDataSetChanged();
    }

    private final FloatingActionButton.OnClickListener add_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LinkDevice();
        }
    };


    private ArrayList<Data> getPairedDevices(){
        SharedPreferences prefs = this.getSharedPreferences(
                "de.rgb_control.PairedDevices", Context.MODE_PRIVATE);

        String json = prefs.getString("data", "");

        if(json.length()!=0){
            Gson gson = new Gson();
            Type type = new TypeToken <ArrayList<Data>> () {}.getType();
            return gson.fromJson(json, type);
        }
        else return new ArrayList<Data>();

    }

    private void AddDevice(Data data){
        ArrayList<Data> pairedDevices = new ArrayList<Data>(getPairedDevices());
        if (data != null)
        {
            pairedDevices.add(data);
        }
        SharedPreferences prefs = getSharedPreferences("de.rgb_control.PairedDevices", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(pairedDevices);
        editor.putString("data", json);
        editor.apply();

    }

    private void MoveDevice(int fromPosition, int toPosition) {
        ArrayList<Data> pairedDevices = new ArrayList<Data>(getPairedDevices());
        Data row = pairedDevices.get(fromPosition);
        pairedDevices.remove(fromPosition);
        pairedDevices.add(toPosition, row);
        SharedPreferences prefs = getSharedPreferences("de.rgb_control.PairedDevices", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(pairedDevices);
        editor.putString("data", json);
        editor.apply();
    }

    private void RemoveDevice(int index){
        ArrayList<Data> pairedDevices = new ArrayList<Data>(getPairedDevices());
        pairedDevices.remove(index);
        SharedPreferences prefs = getSharedPreferences("de.rgb_control.PairedDevices", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(pairedDevices);
        editor.putString("data", json);
        editor.apply();
    }

    private void LinkDevice(){
        ScanFilter scanFilter = new ScanFilter.Builder().setManufacturerData(Integer.parseInt("6f63", 16),Helper.hexStringToByteArray("6E74726F6C")).build();

        CompanionDeviceManager deviceManager = getSystemService(CompanionDeviceManager.class);
        BluetoothLeDeviceFilter deviceFilter = new BluetoothLeDeviceFilter.Builder()
                .setScanFilter(scanFilter)
                .build();

        AssociationRequest pairingRequest = new AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .build();

        deviceManager.associate(pairingRequest, new CompanionDeviceManager.Callback() {
            @Override
            public void onDeviceFound(IntentSender chooserLauncher) {

                try {
                    startIntentSenderForResult(chooserLauncher,
                            SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(CharSequence error) {

            }
        }, null);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==SELECT_DEVICE_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {

            ScanResult scanResult = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);

            BluetoothDevice deviceToPair =  scanResult.getDevice();

            AddDevice(new Data(deviceToPair.getName(), R.drawable.ic_launcher, deviceToPair.getAddress()));
            devices.clear();
            devices.addAll(getPairedDevices());
            adapter.notifyDataSetChanged();

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private final BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(newState== BluetoothProfile.STATE_CONNECTED){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        control = new BLE(gatt);
                        Intent intent = new Intent(getApplicationContext(), MainNavigation.class);
                        startActivity(intent);
                    }
                });

            }
            else{


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Error");
                        builder.setMessage("Verbindung zum Gerät fehlgeschlagen!");

                        builder.setIcon(R.drawable.ic_warning);
                        builder.setCancelable(false);
                        builder.setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        gatt.disconnect();
                                        gatt.close();
                                        adapter.setClickable(true);
                                        hideLoading();
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();



                    }
                });

            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status==BluetoothGatt.GATT_SUCCESS){
                control.initServices();
            }
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
                new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
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
        hideLoading();

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

    private void showLoading(){
        if(loading_indicator!=null){
            loading_indicator.setVisible(true);
        }
    }

    private void hideLoading(){
        if(loading_indicator!=null){
            loading_indicator.setVisible(false);
        }
    }


    @Override
    public void onDeviceItemClick(int position) {
        showLoading();

        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothDevice bluetoothDevice = bluetoothManager.getAdapter().getRemoteDevice(devices.get(position).deviceAddress);

        bluetoothDevice.connectGatt(getApplicationContext(), false, callback).connect();
        adapter.setClickable(false);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){


        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            MoveDevice(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            RemoveDevice(viewHolder.getAdapterPosition());
            devices.remove(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            Drawable icon = ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.ic_delete);
            final ColorDrawable background = new ColorDrawable(Color.RED);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            }
            else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }
            background.draw(c);
            icon.draw(c);
        }
    };
}