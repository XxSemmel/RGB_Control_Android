package de.rgb_control;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import de.rgb_control.devicelist.Data;
import de.rgb_control.devicelist.ItemTouchHelperAdapter;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private boolean clickable = true;
    private final List<Data> data;
    private final OnDeviceClickListener onDeviceClickListener;

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(data, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(data, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textView;
        private final ImageView imageView;;
        OnDeviceClickListener onDeviceClickListener;
        private final LinearLayout linearLayout;
        public ViewHolder(View view, OnDeviceClickListener onDeviceClickListener){
            super(view);

            linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout);
            textView = (TextView) view.findViewById(R.id.device_list_name);
            imageView = (ImageView) view.findViewById(R.id.device_list_icon);
            this.onDeviceClickListener = onDeviceClickListener;
            view.setOnClickListener(this);

        }



        @Override
        public void onClick(View v) {
            onDeviceClickListener.onDeviceItemClick(getAdapterPosition());
        }
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        this.notifyDataSetChanged();
    }
    
    public CustomAdapter(List<Data> data, OnDeviceClickListener onDeviceClickListener){
        this.data = data;
        this.onDeviceClickListener = onDeviceClickListener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list, parent, false);

        return new ViewHolder(view, onDeviceClickListener);
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position){
        viewHolder.textView.setText(data.get(position).Name);
        viewHolder.imageView.setImageResource(data.get(position).icon);

        if (!clickable){
            viewHolder.linearLayout.setClickable(false);
        } else {
            viewHolder.linearLayout.setClickable(true);
        }

    }
    
    public int getItemCount(){
        return data.size();
    }


    public interface OnDeviceClickListener{
        void onDeviceItemClick(int position);
    }





}
