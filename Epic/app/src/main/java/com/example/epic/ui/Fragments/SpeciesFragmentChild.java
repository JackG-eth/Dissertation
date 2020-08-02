package com.example.epic.ui.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epic.R;
import com.example.epic.ui.Adapters.SpeciesChildRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/*
   This subclass displays the information dependant on the choice made ins Species Fragment
 */
public class SpeciesFragmentChild extends Fragment implements SpeciesChildRecyclerViewAdapter.OnSpeciesListener {

    // Tag for debugging
    private static final String TAG = "SpeciesFrag Debugging";

    /**
     * Tags for accessing columns from documents in a firebase collection
     */
    private static final String KEY_NAME = "Name";
    private static final String KEY_Image = "ImageUrl";
    private static final String KEY_About = "About";

    /*
     * Initialising connection to Firebase database.
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mSelectedSpecies;
    /*
        Arraylists for storing specific information for each animal.
     */
    private ArrayList<String> mAnimalImages = new ArrayList<>();
    private ArrayList<String> mAnimalNames = new ArrayList<>();
    private ArrayList<String> mAnimalAbout = new ArrayList<>();

    private String mAnimalName;

    private SpeciesChildRecyclerViewAdapter mSpeciesAdapter;
    private RecyclerView mRecyclerView;

    private String mNamePassed = "";

    private String longitude = "NA";
    private String latitude = "NA";

    private View mRoot;

    /*
        Simple setter
     */
    public SpeciesFragmentChild(String animalName) {
        mAnimalName = animalName;
    }

    /*
        Simple getter
     */
    public String getAnimalName() {
        return mAnimalName;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_species, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getAnimalName());


        /*
         Get the users location if accessed from map fragment
         */
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            longitude = bundle.getString("Longitude");
            latitude = bundle.getString("Latitude");

            Log.d("Current location:", longitude + "," + latitude);

        }

        // Clear the arrayLists to prevent duplicates, may change this logic.
        mAnimalImages.clear();
        mAnimalNames.clear();
        mAnimalAbout.clear();

        // Make sure adapter is aware if there is a dataset change.
        mRecyclerView = mRoot.findViewById(R.id.species_recycler);
        mSpeciesAdapter = new SpeciesChildRecyclerViewAdapter(mAnimalImages, mAnimalNames, getContext(), this);
        mRecyclerView.setAdapter(mSpeciesAdapter);
        mSpeciesAdapter.notifyDataSetChanged();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadImage();

        return mRoot;
    }

    /**
     * Read in species name
     * Check it against some if/else statements
     * Create a new document read
     * Populate!
     */
    private void loadImage() {

        mNamePassed = getAnimalName();

        if (mNamePassed.equals("Amphibian")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Amphibian").collection("Amphibian");
        } else if (mNamePassed.equals("Bird")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Bird").collection("Bird");
        } else if (mNamePassed.equals("Fish")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Fish").collection("Fish");
        } else if (mNamePassed.equals("Invertebrate")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Invertebrate").collection("Invertebrate");
        } else if (mNamePassed.equals("Mammal")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Mammal").collection("Mammal");
        } else if (mNamePassed.equals("Reptile")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Reptile").collection("Reptile");
        } else if (mNamePassed.equals("Vegetation")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Vegetation").collection("Vegetation");
        } else if (mNamePassed.equals("Butterflies & Moths")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Butterflies & Moths").collection("Butterflies & Moths");
        }else if (mNamePassed.equals("Beetles & Bugs")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Beetles & Bugs").collection("Beetles & Bugs");
        }else if (mNamePassed.equals("DragonFlies Etc")) {
            mSelectedSpecies = db.collection("SpeciesList").document("DragonFlies Etc").collection("DragonFlies Etc");
        }else if (mNamePassed.equals("Bees, Wasps & Flies")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Bees, Wasps & Flies").collection("Bees, Wasps & Flies");
        } else if (mNamePassed.equals("Freshwater Invertebrates")) {
            mSelectedSpecies = db.collection("SpeciesList").document("Freshwater Invertebrates").collection("Freshwater Invertebrates");
        }else {
            // do nothing atm
        }

        // add try catch statement here?
        namePassed(mSelectedSpecies);
        /// Add multiple if else's, better way to do it?
    }

    /*
     Dependant on the choice made in the previous fragment retrieve certain information.
     */
    private void namePassed(CollectionReference speciesSelected) {
        speciesSelected.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                // Log.d(TAG, document.getId() + " => " + document.getData());

                                String name = document.getString(KEY_NAME);
                                String imageUrl = document.getString(KEY_Image);
                                String about = document.getString(KEY_About);

                                mAnimalImages.add(imageUrl);
                                mAnimalNames.add(name);
                                mAnimalAbout.add(about);
                            }
                            initRecyclerView();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    /*
       View to display list to user, if nothing is displayed the user will be asked to check their internet connection.
     */
    private void initRecyclerView() {
        Log.d(TAG, "InitRecyclerView: setting up recycler view");
        mRecyclerView = mRoot.findViewById(R.id.species_recycler);
        mSpeciesAdapter = new SpeciesChildRecyclerViewAdapter(mAnimalImages, mAnimalNames, getContext(), this);
        mRecyclerView.setAdapter(mSpeciesAdapter);
        mSpeciesAdapter.notifyDataSetChanged();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (mAnimalNames.isEmpty()) {
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
    }

    /*
       Adding a listener for button clicks items in the recycler list.
     */
    @Override
    public void OnRecyclerItemClick(int position) {

        mAnimalNames.get(position);

        SpeciesFragmentChildChild speciesFrag = new SpeciesFragmentChildChild(mAnimalNames.get(position), mAnimalImages.get(position), mAnimalAbout.get(position), mAnimalName);

        Bundle bundle = new Bundle();

        bundle.putString("Longitude", longitude);
        bundle.putString("Latitude", latitude);
        speciesFrag.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(((ViewGroup) getView().getParent()).getId(), speciesFrag, "findThisFragment")
                .addToBackStack(null)
                .commit();


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
