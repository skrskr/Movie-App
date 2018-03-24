package com.mohamed.movie_app_s2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by mohamed on 17/09/17.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {

    public interface OnItemClick {
        void onItemClick(int position);
    }

    private ArrayList<Movie> mMoviesList;
    Context context;
    OnItemClick onItemClick;

    public MoviesAdapter(ArrayList<Movie> mMoviesList, Context context, OnItemClick onItemClick) {
        this.context = context;
        this.mMoviesList = mMoviesList;
        this.onItemClick = onItemClick;
    }

    @Override
    public MoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutListItem = R.layout.list_item;
        boolean shouldAttachToParent = false;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutListItem, parent, shouldAttachToParent);
        return new MoviesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesViewHolder holder, int position) {
        String imagePath = mMoviesList.get(position).getmPosterPath();
        Picasso.with(context).load(imagePath).into(holder.poster);

    }

    @Override
    public int getItemCount() {
        return mMoviesList.size();
    }

    public class MoviesViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnClickListener {
        ImageView poster;

        public MoviesViewHolder(View itemView) {
            super(itemView);
            poster = (ImageView) itemView.findViewById(R.id.iv_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            onItemClick.onItemClick(position);
        }
    }

}
