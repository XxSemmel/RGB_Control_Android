package de.rgb_control.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.rgb_control.MainActivity;
import de.rgb_control.R;
import de.rgb_control.helper.BLE;
import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorPickerView;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private BLE control;
    private boolean powerstate = true;

    public HomeFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final ColorPickerView colorPicker = root.findViewById(R.id.colorPicker);
        FloatingActionButton btn = root.findViewById(R.id.powerbutton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!powerstate){
                    control.turnOn(colorPicker.getColor());
                    powerstate=true;
                }
                else {
                    control.turnOff();
                    powerstate=false;
                }
            }
        });
        colorPicker.subscribe(colorObserver);

        control=MainActivity.control;

        return root;
    }

    ColorObserver colorObserver = new ColorObserver() {
        @Override
        public void onColor(int color, boolean fromUser, boolean shouldPropagate) {
            control.sendColor(color);
            powerstate=true;
        }
    };
}