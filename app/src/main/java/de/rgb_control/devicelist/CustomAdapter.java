package de.rgb_control;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import de.rgb_control.devicelist.Data;

public class CustomAdapter extends ArrayAdapter<Data>{

    private ArrayList<Data> dataSet;
    Context mContext;

    public static class ViewHolder{
        TextView txtName;
        ImageView icon;
    }

    public CustomAdapter(ArrayList<Data> data, Context context) {
        super(context, R.layout.device_list, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Data data = getItem(position);
        ViewHolder viewHolder = new ViewHolder();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        View view = inflater.inflate(R.layout.device_list, null, false);


        viewHolder.txtName=(TextView)view.findViewById(R.id.device_list_name);
        viewHolder.icon=(ImageView)view.findViewById(R.id.device_list_icon);

        viewHolder.txtName.setText(data.Name);
        viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(data.icon));


        return view;

    }
}
