package com.waslnyapp.waslny.SavedRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.waslnyapp.waslny.SavedSingleActivity;
import com.waslnyapp.waslny.R;


public class SavedViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView rideId;
    public TextView time;
    public TextView rideLocation;

    public SavedViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = (TextView) itemView.findViewById(R.id.rideId); //RecyclerView item history layout
        time = (TextView) itemView.findViewById(R.id.time);     //RecyclerView item history layout
        rideLocation = (TextView) itemView.findViewById(R.id.rideLocation);     //RecyclerView item history layout
    }


    @Override
    public void onClick(View v) {

        //passing values " rideId " between two activities
        Intent intent = new Intent(v.getContext(), SavedSingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);
    }
}
