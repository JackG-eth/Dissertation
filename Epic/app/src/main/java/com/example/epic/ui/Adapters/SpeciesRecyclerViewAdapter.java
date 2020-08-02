package com.example.epic.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.epic.R;

import java.util.ArrayList;

/*
    Adapter to deal with the speciesFragment
 */
public class SpeciesRecyclerViewAdapter extends RecyclerView.Adapter<SpeciesRecyclerViewAdapter.ViewHolder> {

    // ArrayList that handles animal images
    private ArrayList<String> mAnimalImages;
    // ArrayList that handles animal Names
    private ArrayList<String> mAnimalNames;

    // Allows the class to access application specific resources
    private Context mContext;

    // For intercepting the events from a users interaction
    private OnSpeciesListener mOnSpeciesListener;


    /*
        SpeciesRecyclerViewAdapter constructor
     */
    public SpeciesRecyclerViewAdapter(ArrayList<String> animalImages, ArrayList<String> animalNames, Context context, OnSpeciesListener onSpeciesListener ) {
        mAnimalImages = animalImages;
        mAnimalNames = animalNames;
        mContext = context;
        this.mOnSpeciesListener = onSpeciesListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        ViewHolder holder = new ViewHolder(view,mOnSpeciesListener);
        return holder;
    }

    /*
        Returns the items position in the array, useful for determining which item the user has pressed
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Important method, changes on what layouts look like
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        // GLIDE BUMPTECH image loading from internet API
        Glide.with(mContext)
                .asBitmap()
                .load(mAnimalImages.get(position))
                .centerCrop()
                .into(holder.animalImage);

        holder.animalName.setText(mAnimalNames.get(position));

    }

    /*
        Returns the size of the array
     */
    @Override
    public int getItemCount() {
        return mAnimalNames.size();
    }

    /*
        A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        ImageView animalImage;
        TextView animalName;

        OnSpeciesListener onSpeciesListener;

        public ViewHolder(@NonNull View itemView, OnSpeciesListener onSpeciesListener) {
            super(itemView);
            animalImage = itemView.findViewById(R.id.recycler_image);
            animalName = itemView.findViewById(R.id.recycler_image_text);

            this.onSpeciesListener = onSpeciesListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onSpeciesListener.OnRecyclerItemClick(getAdapterPosition());
        }
    }

    public interface OnSpeciesListener{
        void OnRecyclerItemClick(int position);
    }
}
