package de.rgb_control.ui.settings;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.rgb_control.MainActivity;
import de.rgb_control.R;
import de.rgb_control.helper.BLE;

public class SettingsFragment extends PreferenceFragmentCompat {


    private BLE control;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        control=MainActivity.control;
        setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference neopixels = findPreference("numb");
        EditTextPreference rename = findPreference("name");
        ListPreference appstyle = findPreference("styles");
        appstyle.setOnPreferenceChangeListener(appstylechange);
        rename.setOnPreferenceChangeListener(namechange);
        neopixels.setOnPreferenceChangeListener(neopixelchange);

    }




    ListPreference.OnPreferenceChangeListener appstylechange = new ListPreference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (newValue.toString()){
                case "system_default":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
                case "dark":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "light":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
            }
            return true;
        }
    };





    EditTextPreference.OnPreferenceChangeListener namechange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            control.changeDeviceName(newValue.toString());

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Information");
            builder.setMessage("Dein Gerät benötigt einen Neustart, damit die Änderungen übernommen werden! Jetzt neustarten?");

            builder.setIcon(R.drawable.ic_info);
            builder.setPositiveButton(
                    "Neustarten",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            control.reboot();
                            android.os.Process.killProcess(android.os.Process.myPid()); //exit
                        }
                    });
            builder.setNegativeButton("Später", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
    };

    EditTextPreference.OnPreferenceChangeListener neopixelchange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, final Object newValue) {
            control.sendNeopixels(newValue.toString());

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Information");
            builder.setMessage("Dein Gerät benötigt einen Neustart, damit die Änderungen übernommen werden! Jetzt neustarten?");

            builder.setIcon(R.drawable.ic_info);
            builder.setPositiveButton(
                    "Neustarten",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            control.reboot();
                            android.os.Process.killProcess(android.os.Process.myPid()); //exit
                        }
                    });
            builder.setNegativeButton("Später", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
    };


}