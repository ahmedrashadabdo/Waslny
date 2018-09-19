package com.waslnyapp.waslny.SavedRecyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.waslnyapp.waslny.R;

import java.util.List;

public class SavedAdapter extends RecyclerView.Adapter<SavedViewHolders> {

    private List<SavedObject> itemList;
    private Context context;

    public SavedAdapter(List<SavedObject> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public SavedViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        SavedViewHolders rcv = new SavedViewHolders(layoutView);
        return rcv;
    }

    /*LayoutParams are used by views to tell their parents how they want to be laid out. See ViewGroup Layout Attributes for a list of all child view attributes that this class supports.

    The base LayoutParams class just describes how big the view wants to be for both width and height. For each dimension, it can specify one of:

    == FILL_PARENT (renamed MATCH_PARENT in API Level 8 and higher), which means that the view wants to be as big as its parent (minus padding)
    == WRAP_CONTENT, which means that the view wants to be just big enough to enclose its content (plus padding)
    an exact number
    There are subclasses of LayoutParams for different subclasses of ViewGroup. For example, AbsoluteLayout has its own subclass of LayoutParams which adds an X and Y value.*/

    @Override
    public void onBindViewHolder(SavedViewHolders holder, final int position) {
        holder.rideId.setText(itemList.get(position).getRideId());
        holder.rideLocation.setText(itemList.get(position).getrideLocation());
        if(itemList.get(position).getTime()!=null){
            holder.time.setText(itemList.get(position).getTime());
        }
    }
    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

}