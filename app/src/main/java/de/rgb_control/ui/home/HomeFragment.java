package de.rgb_control.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import de.rgb_control.MainActivity;
import de.rgb_control.R;
import de.rgb_control.helper.BLE;
import petrov.kristiyan.colorpicker.ColorPicker;
import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorPickerView;

public class HomeFragment extends Fragment {


    private BLE control;
    public static boolean powerstate = true;
    private ColorPickerView colorPicker;


    public HomeFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        control=MainActivity.control;

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        colorPicker = root.findViewById(R.id.colorPicker);
        Button maincolors = root.findViewById(R.id.maincolors);
        maincolors.setOnClickListener(maincolorclick);
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


        int color = control.getColor();
        if(color==0){
            colorPicker.setInitialColor(16711680);
            control.sendColor(true,16711680);
        }
        else {
            colorPicker.setInitialColor(control.getColor());
        }




        return root;
    }

    ColorObserver colorObserver = new ColorObserver() {
        @Override
        public void onColor(int color, boolean fromUser, boolean shouldPropagate) {
                if(fromUser){

                    control.sendColor(color);
                    powerstate=true;
                }

        }
    };


    Button.OnClickListener maincolorclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final ColorPicker picker = new ColorPicker(getActivity());
            picker.setTitle("Wähle deine Farbe aus");
            picker.setColors(getColors());
            picker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener(){

                @Override
                public void setOnFastChooseColorListener(int position, int color) {
                    control.sendColor(color);
                    colorPicker.setInitialColor(color);
                }

                @Override
                public void onCancel() {

                }
            });
            picker.show();

        }
    };

    private ArrayList<String> getColors(){
        ArrayList<String> colors = new ArrayList<>();
        colors.add("#ff0000"); //red
        colors.add("#00FF00");//green
        colors.add("#0000FF");//blue
        colors.add("#FFFFFF"); //white
        colors.add("#FFFF00"); //yellow
        colors.add("#B10DC9");//purple
        colors.add("#00eeff");//lightblue
        colors.add("#ff00cc");//lila
        colors.add("#ff2700");//orange
        colors.add("#cccc00");//darkyellow
        return colors;
    }
}