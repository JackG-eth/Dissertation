package com.example.epic.ui.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.epic.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
    Display more detailed information for a specific choice
 */
public class SpeciesFragmentChildChild extends Fragment{

    // Debugging tag
    private static final String TAG = "SpeciesChildFrag";

    private String mAnimalName;
    private String mAbout;
    private String mImage;
    private String mAnimalSpecies;

    /**
     * Tags for accessing columns from documents in a firebase collection
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mSightingsCollection;

    // Create a HashMap for storing new sightings on database.
    private Map<String, Object> sighting = new HashMap<>();

    private String longitude = "NA";
    private String latitude = "NA";

    private TextView mName;
    private TextView mAboutAnimal;
    private ImageView mAnimalImage;

    private Date currentTime = null;
    private View mRoot;

    private Button record;

    // Default number of animals seen is 1
    private int mNumberAnimals = 1;

    /**
     * SpeciesFragmentChildChild Constructor
     * @param animalName Name of the animal
     * @param animalImage Animals Associated image
     * @param animalAbout Associated information about that animal
     * @param animalSpecies What species is the animal?
     */
    public SpeciesFragmentChildChild(String animalName, String animalImage, String animalAbout, String animalSpecies) {
        mAnimalName = animalName;
        mImage = animalImage;
        mAbout = animalAbout;
        mAnimalSpecies = animalSpecies;
    }

    /*
    Simple getter
    */
    public String getAnimalName() {
        return mAnimalName;
    }

    /*
    Simple getter
     */
    public String getAbout() {
        return mAbout;
    }

    /*
    Simple getter
    */
    public String getImage() {
        return mImage;
    }

    /*
    Simple getter
    */
    public String getAnimalSpecies() {
        return mAnimalSpecies;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_species_child, container, false);

        // Fill widgets with associated information
        mAnimalName = getAnimalName();
        mImage = getImage();

        mAbout = getAbout();
        mAnimalSpecies = getAnimalSpecies();

        /*
         Get the users location if accessed from map fragment
         */
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            longitude = bundle.getString("Longitude");
            latitude = bundle.getString("Latitude");

            Log.d("Current location:" , longitude + "," + latitude);

        }

        /*
         If empty display an internet caution
         */
        if(mAnimalName.isEmpty()){
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Internet Connection Alert")
                    .setMessage("You have no internet connection, this process will not work until you connect again")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        }

        record = mRoot.findViewById(R.id.record_sighting_now);

        currentTime = Calendar.getInstance().getTime();

        /*
            Code for submitting a sighting
            Previous data from choices made earlier is passed down.
         */
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(getActivity());

                View dView = getLayoutInflater().inflate(R.layout.child_dialog,null);

                final EditText nAnimals = dView.findViewById(R.id.number_of_animals);

                nAnimals.setInputType(InputType.TYPE_CLASS_NUMBER);

                Button mSubmit = dView.findViewById(R.id.number_submit);

                dialog.setContentView(dView);
                dialog.create();
                dialog.show();


                /** Create separate method to do bulk
                 */
                mSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(nAnimals.getText().toString().isEmpty()){
                            Log.d(TAG, Integer.toString(mNumberAnimals));

                            sighting.put("Species",mAnimalSpecies);
                            sighting.put("Name",mAnimalName);
                            sighting.put("Number Spotted",mNumberAnimals);
                            sighting.put("Time",currentTime);
                            sighting.put("Location", "Longitude: " + longitude +","+ "Latitude: " + latitude);

                            mSightingsCollection = db.collection("Sighting");
                            mSightingsCollection.document(mAnimalName +","+currentTime).set(sighting)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });

                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                            int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();

                            // Takes user back to mapfrag after completion, ensures no frags are left unhandled.
                            if (count != 0) {
                                for (int i = 0; i < count; ++i) {
                                    fragmentManager.popBackStack();
                                }
                            }
                            MapFragment mapFrag = new MapFragment();

                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(((ViewGroup) getView().getParent()).getId(), mapFrag, "findThisFragment")
                                    .addToBackStack(null)
                                    .commit();

                            dialog.dismiss();
                        }
                        else {

                            mNumberAnimals = Integer.parseInt(nAnimals.getText().toString());

                            sighting.put("Species",mAnimalSpecies);
                            sighting.put("Name",mAnimalName);
                            sighting.put("Number Spotted",mNumberAnimals);
                            sighting.put("Time",currentTime);
                            sighting.put("Location", "Longitude: " + longitude +","+ "Latitude: " + latitude);

                            mSightingsCollection = db.collection("Sighting");
                            mSightingsCollection.document(mAnimalName +","+currentTime).set(sighting)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });

                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                            int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();

                            // Takes user back to mapfrag after completion, ensures no frags are left unhandled.
                            if (count != 0) {
                                for (int i = 0; i < count; ++i) {
                                    fragmentManager.popBackStack();
                                }
                            }
                            MapFragment mapFrag = new MapFragment();

                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(((ViewGroup) getView().getParent()).getId(), mapFrag, "findThisFragment")
                                    .addToBackStack(null)
                                    .commit();

                            dialog.dismiss();
                        }
                    }
                });
                }

        });




       // Log.d(TAG,"Child Name " + mAnimalName);
       // Log.d(TAG,"Child Image " + mImage);
        // Log.d(TAG,"Child About " + mAbout);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getAnimalName());

        mName = mRoot.findViewById(R.id.recycler_image_text_child);
        mName.setText(mAnimalName);

        mAboutAnimal = mRoot.findViewById(R.id.about_info);
        mAboutAnimal.setText(mAbout);

        mAnimalImage = mRoot.findViewById(R.id.recycler_image_child);

        Glide.with(getActivity())
                .asBitmap()
                .load(mImage)
                .centerCrop()
                .error(Glide.with(getActivity()).asBitmap().load(R.drawable.missing_image))
                .centerCrop()
                .into(mAnimalImage);

        return mRoot;
    }

    @Override
    public void onResume() {
        super.onResume();
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
