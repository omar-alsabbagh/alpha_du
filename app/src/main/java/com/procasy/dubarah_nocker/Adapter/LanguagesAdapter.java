package com.procasy.dubarah_nocker.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.procasy.dubarah_nocker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Omar on 5/24/2016.
 */

public  class LanguagesAdapter extends RecyclerView.Adapter<LanguagesAdapter.ViewHolder> {


    private List<String> mValues;
    Context context;
    private AdapterCallback mAdapterCallback;

    public static class ViewHolder extends RecyclerView.ViewHolder  {

        public final TextView skill_name;
        public final ImageView skill_add;
        public final LinearLayout linearLayout;
        public ViewHolder(View view) {
            super(view);
            skill_name = (TextView) view.findViewById(R.id.skill_name);
            skill_add = (ImageView) view.findViewById(R.id.add_skill);
            linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + skill_name.getText();
        }
    }

    public String getValueAt(int position) {
        return mValues.get(position);
    }

    public LanguagesAdapter(Context context, List<String> items, AdapterCallback callback) {
        mValues = items;
        this.context = context;
        this.mAdapterCallback = callback;
    }
    private void updateData(List<String> newData)
    {
        mValues.clear();
        mValues.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.one_skill_listing, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.skill_name.setText(mValues.get(position));
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.onMethodCallback(mValues.get(position));
                List<String> strings= new ArrayList<String>();
                mValues.remove(position);
                strings.addAll(mValues);
                updateData(strings);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
