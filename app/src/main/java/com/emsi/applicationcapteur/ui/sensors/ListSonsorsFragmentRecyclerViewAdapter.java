package com.emsi.applicationcapteur.ui.sensors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.emsi.applicationcapteur.R;
import com.emsi.applicationcapteur.beans.SensorItem;
import com.emsi.applicationcapteur.ui.sensors.ListSonsorsFragment.OnListFragmentInteractionListener;

public class ListSonsorsFragmentRecyclerViewAdapter extends RecyclerView.Adapter<ListSonsorsFragmentRecyclerViewAdapter.ViewHolder> {

    private final List<SensorItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ListSonsorsFragmentRecyclerViewAdapter(List<SensorItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_listsonsors, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mNameView.setText(mValues.get(position).name);
        holder.mVersionView.setText(mValues.get(position).version);
        holder.mVendorView.setText(mValues.get(position).vendor);
        holder.mTypeView.setText(mValues.get(position).type);
        
        // Add a ripple effect when clicking
        holder.mView.setBackgroundResource(android.R.drawable.list_selector_background);
        
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mNameView;
        public final TextView mTypeView;
        public final TextView mVendorView;
        public final TextView mVersionView;
        public SensorItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mNameView = (TextView) view.findViewById(R.id.name);
            mTypeView = (TextView) view.findViewById(R.id.type);
            mVendorView = (TextView) view.findViewById(R.id.vendor);
            mVersionView = (TextView) view.findViewById(R.id.version);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
