package de.rgb_control.ui.effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import de.rgb_control.MainActivity;
import de.rgb_control.R;
import de.rgb_control.helper.BLE;

public class EffectFragment extends Fragment {

    private BLE control;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        control= MainActivity.control;
        View root = inflater.inflate(R.layout.fragment_effects, container, false);
        CardView rainbow= root.findViewById(R.id.rainbow);
        CardView runninglights = root.findViewById(R.id.running_lights);
        runninglights.setOnClickListener(runninglightsClicked);
        rainbow.setOnClickListener(rainbowclick);

        return root;
    }

    private final CardView.OnClickListener runninglightsClicked = new CardView.OnClickListener(){

        @Override
        public void onClick(View view) {
            control.sendEffect(BLE.Effects.RUNNING_LIGHTS);
        }
    };


    private final CardView.OnClickListener rainbowclick = new CardView.OnClickListener(){

        @Override
        public void onClick(View view) {
            control.sendEffect(BLE.Effects.RAINBOW);
        }
    };
}