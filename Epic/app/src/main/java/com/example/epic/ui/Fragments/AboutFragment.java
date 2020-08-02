package com.example.epic.ui.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.epic.R;


/**
 * Simple class that simply displays static information on the company.
 */
public class AboutFragment extends Fragment {


    // Textview that stores the link to Epics Website
    protected TextView link;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_about, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_about);

        link = root.findViewById(R.id.website_url);

        // redirect to epic website
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browser = new Intent(Intent.ACTION_VIEW,Uri.parse("https://oart.org.uk/epic/"));
                startActivity(browser);
            }
        });

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