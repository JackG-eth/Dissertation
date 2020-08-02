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
    Adapter to deal with the species child Fragment
 */
public class SpeciesChildRecyclerViewAdapter extends RecyclerView.Adapter<SpeciesChildRecyclerViewAdapter.ViewHolder>{

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
    public SpeciesChildRecyclerViewAdapter(ArrayList<String> animalImages, ArrayList<String> animalNames, Context context, SpeciesChildRecyclerViewAdapter.OnSpeciesListener onSpeciesListener ) {
        mAnimalImages = animalImages;
        mAnimalNames = animalNames;
        mContext = context;
        this.mOnSpeciesListener = onSpeciesListener;
    }

    @NonNull
    @Override
    public SpeciesChildRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        SpeciesChildRecyclerViewAdapter.ViewHolder holder = new SpeciesChildRecyclerViewAdapter.ViewHolder(view,mOnSpeciesListener);
        return holder;
    }

    // Important method, changes on what layouts look like
    @Override
    public void onBindViewHolder(@NonNull SpeciesChildRecyclerViewAdapter.ViewHolder holder, final int position) {

        // GLIDE BUMPTECH image loading from internet API
        Glide.with(mContext)
                .asBitmap()
                .load(mAnimalImages.get(position))
                .centerCrop()
                .error(Glide.with(mContext).asBitmap().load(R.drawable.missing_image))
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


        // will need to change
        ImageView animalImage;
        TextView animalName;

        SpeciesChildRecyclerViewAdapter.OnSpeciesListener onSpeciesListener;

        public ViewHolder(@NonNull View itemView, SpeciesChildRecyclerViewAdapter.OnSpeciesListener onSpeciesListener) {
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

