package com.production.w.productionlinemonitor;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by w on 4/23/2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private String[] mDataset;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public LinearLayout mLinearLayout;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
        public ViewHolder(LinearLayout ly) {
            super(ly);
            mLinearLayout = ly;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(String[] myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.work_station_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.mTextView.setText(mDataset[position]);
        TextView tv_name =  holder.mLinearLayout.findViewById(R.id.tv_name);
        TextView tv_status = holder.mLinearLayout.findViewById(R.id.pl_tv_status);
        ImageButton ib_watch = holder.mLinearLayout.findViewById(R.id.ib_watch);

        tv_name.setText(mDataset[position]);
        tv_status.setText(R.string.workStationNormal);
        ib_watch.setImageResource(R.drawable.system);
        ib_watch.setAdjustViewBounds(true);

        ib_watch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WorkStationActivity.class);
                intent.putExtra(WorkStationListActivity.EXTRA_ID, position + 1);
                mContext.startActivity(intent);
            }
        });
        Log.e(TAG, "onBindViewHolder: " + position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.e(TAG, "getItemCount: " + mDataset.length);
        return mDataset.length;
    }
}
