package com.example.epic.ui.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.epic.R;

/**
 * Fragment for handling user feedback
 */
public class FeedbackFragment extends Fragment {

    // handling users email feedback
    private TextView email;

    private Button mYes;
    private Button mNo;

    private EditText mResponse;
    private Button mSend;

    private String mResponseText;
    private String mRating;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_feedback, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_feedback);

        mYes = root.findViewById(R.id.button_yes);
        mNo = root.findViewById(R.id.button_no);
        mResponse = root.findViewById(R.id.feedback_response);
        mSend = root.findViewById(R.id.feedback_send);

        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRating = "Yes";
                Toast.makeText(getContext(),"Recorded",Toast.LENGTH_SHORT).show();
            }
        });

        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRating = "No";
                Toast.makeText(getContext(),"Recorded",Toast.LENGTH_SHORT).show();
            }
        });


        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mRating == null){
                    mRating = "None Set";
                }
                if(mResponse.getText().toString().isEmpty()){
                    mResponseText = "None Set";
                }
                else{
                    mResponseText = mResponse.getText().toString();
                }
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "epic@oart.org.uk" });
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mRating);
                emailIntent.putExtra(Intent.EXTRA_TEXT, mResponseText);
                emailIntent.setType("text/plain");
                startActivity(emailIntent);
            }
        });


        /*
        *Add when app is on play store.
        googlplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        */

        /*
            Handles users feedback by opening up their chosen email service and providing Epics Email.
         */
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