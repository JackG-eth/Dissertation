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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epic.R;
import com.example.epic.ui.Adapters.SpeciesRecyclerViewAdapter;
import com.example.epic.ui.Classes.NetworkStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/*
    Displays the species available to the user using a reyclerview and connection to database.
 */
public class SpeciesFragment extends Fragment implements SpeciesRecyclerViewAdapter.OnSpeciesListener {

    // Debugging Tag
    private static final String TAG = "SpeciesFrag Debugging";

    /**
     * Tags for accessing columns from documents in a firebase collection
     */
    private static final String KEY_NAME = "Name";
    private static final String KEY_Image = "ImageUrl";

    /*
     * Initialising connection to Firebase database.
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mBirds = db.collection("SpeciesList");

    // ArrayList to store The species name and associated image.
    private ArrayList<String> mAnimalImages = new ArrayList<>();
    private ArrayList<String> mAnimalNames = new ArrayList<>();

    // Will be overwritten with users location if accessed from map
    private String longitude = "NA";
    private String latitude = "NA";

    /*
        RecyclerViewAdapter and recyclerview object.
     */
    private SpeciesRecyclerViewAdapter mSpeciesAdapter;
    private RecyclerView mRecyclerView;

    private View mRoot;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_species, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_species);

        /*
         Get the users location if accessed from map fragment
         */
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            longitude = bundle.getString("Longitude");
            latitude = bundle.getString("Latitude");

            Log.d("Current location:" , longitude + "," + latitude);

        }

        // Clear the arrayLists to prevent duplicates, may change this logic.
        mAnimalImages.clear();
        mAnimalNames.clear();

        mRecyclerView = mRoot.findViewById(R.id.species_recycler);
        mSpeciesAdapter = new SpeciesRecyclerViewAdapter(mAnimalImages,mAnimalNames,getContext(),this);

        // Make sure adapter is aware if there is a dataset change.
        mRecyclerView.setAdapter(mSpeciesAdapter);
        mSpeciesAdapter.notifyDataSetChanged();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        /*
        No cached size added as usage is limited
         */
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        /*
            Check user is connected, if not attempt to retrieve cached information
         */
        if (NetworkStatus.getInstance(getActivity()).isOnline()) {
            db.enableNetwork();
            loadImage();
        }
        else{
            db.disableNetwork();
            offlineListen(db);
        }

        return mRoot;
    }

    /*
        Load Species and associated image from colletion in Firebase
     */
    private void loadImage(){

        /*
        If no internet connection is available load cached results.
         */
            if (mAnimalNames.isEmpty()) {
                mBirds.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, "Adding docs again" + document.getId() + " => " + document.getData());
                                        String name = document.getString(KEY_NAME);
                                        String imageUrl = document.getString(KEY_Image);


                                        mAnimalImages.add(imageUrl);
                                        mAnimalNames.add(name);
                                    }
                                    initRecyclerView();
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            }

    }

    /**
     * Offline functionality
     * Retrieved cached information, reduces network usage.
     */
    public void offlineListen(FirebaseFirestore db) {
        // [START offline_listen]
        mBirds.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }
                        for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                String name = change.getDocument().get(KEY_NAME).toString();
                                String imageUrl = change.getDocument().get(KEY_Image).toString();
                                Log.d(TAG, "Offline Name :" + change.getDocument().get(KEY_NAME).toString());
                                Log.d(TAG, "Offline ImageUrl :" + change.getDocument().get(KEY_Image).toString());
                                mAnimalImages.add(imageUrl);
                                mAnimalNames.add(name);
                                Log.d(TAG, "Offline Add :" + change.getDocument().getData());
                            }
                            String source = querySnapshot.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                            Log.d(TAG, "Data fetched from " + source);
                        }
                        initRecyclerView();

                    }
                });
        // [END offline_listen]
    }

    /*
        View to display list to user, if nothing is displayed the user will be asked to check their internet connection.
     */
    private  void initRecyclerView(){
        Log.d(TAG, "InitRecyclerView: setting up recycler view");
        mRecyclerView = mRoot.findViewById(R.id.species_recycler);
        mSpeciesAdapter = new SpeciesRecyclerViewAdapter(mAnimalImages,mAnimalNames,getContext(),this);
        mRecyclerView.setAdapter(mSpeciesAdapter);
        mSpeciesAdapter.notifyDataSetChanged();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if(mAnimalNames.isEmpty()){
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

        SpeciesFragmentChild speciesFrag = new SpeciesFragmentChild(mAnimalNames.get(position));

        Bundle bundle = new Bundle();

        bundle.putString("Longitude",longitude);
        bundle.putString("Latitude",latitude);
        speciesFrag.setArguments(bundle);

        /*
         Initialise new SpeciesChild fragment
         */
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(((ViewGroup)getView().getParent()).getId(), speciesFrag, "findThisFragment")
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