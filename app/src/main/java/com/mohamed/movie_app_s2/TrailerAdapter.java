package com.mohamed.movie_app_s2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mohamed on 05/10/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    public interface OnItemClick {
        void setOnItemClick(int position);
    }


    private ArrayList<Trailer> mTrailerList;
    private Context mContext;
    private OnItemClick mOnItemClick;

    public TrailerAdapter(ArrayList<Trailer> mTrailerList, Context mContext, OnItemClick mOnItemClick) {
        this.mTrailerList = mTrailerList;
        this.mContext = mContext;
        this.mOnItemClick = mOnItemClick;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        Trailer t = mTrailerList.get(position);
        holder.title.setText(t.getmName());
    }

    @Override
    public int getItemCount() {
        return mTrailerList.size();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnClickListener {
        TextView title;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_trailer_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mOnItemClick.setOnItemClick(position);
        }
    }
}
