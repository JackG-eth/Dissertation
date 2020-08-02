package com.example.epic.ui.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.epic.R;

/*
    HomeFragment is the page first displayed to the user
    It is always at the base of the Fragment stack. (First and last thing user sees)
 */
public class HomeFragment extends Fragment  {

    /*
        Static info displayed on home screen
     */
    ImageView introImage;
    String introImageUrl;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_home);


        introImage = root.findViewById(R.id.home_image);

        // new to change to string
        introImageUrl = "https://firebasestorage.googleapis.com/v0/b/epic-5ae7a.appspot.com/o/intro_image.jpg?alt=media&token=0ffe801c-402a-4f9d-9e3d-3c6a765864bb";

        Glide.with(getActivity())
                .asBitmap()
                .load(introImageUrl)
                .centerCrop()
                .into(introImage);

        return root;
    }

    /*
        Default implementation for handling fragment to activity communication.
        Nothing is passed between the two.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}