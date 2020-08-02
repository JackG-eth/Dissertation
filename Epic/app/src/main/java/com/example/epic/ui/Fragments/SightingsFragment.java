package com.example.epic.ui.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.epic.R;
import com.example.epic.ui.Models.Species;
import com.example.epic.ui.Models.SpeciesChild;
import com.example.epic.ui.Classes.NetworkStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
   The Sightings Fragment provides the user with an alternative route when submitting data.
 */
public class SightingsFragment extends Fragment {

    // Spinners to provide user with selection from animals
    private Spinner speciesSpinner;
    private Spinner speciesSpinnerChild;

    // Check for whether required fields have been filled correctly.
    private Boolean notValidated;

    /*
        Simply values required to populate database, user overwrites these when submitting.
     */
    private Date currentTime = null;
    private String species;
    private String speciesChild;
    private String extraInfo;
    private Location location;
    private int howMany;

    // Debugging tag
    private static final String TAG = "SightingsFrag Debugging";

    // Key name for retrieving information from firebase DB
    private static final String KEY_NAME = "Name";

    // ArrayAdapter containing species object, allows for dynamic viewing and is used with the spinner.
    private ArrayAdapter<Species> speciesArrayAdapter = null;

    // ArrayAdapter containing SpeciesChild object, allows for dynamic viewing and is used with the spinner.
    private ArrayAdapter<SpeciesChild> speciesChildArrayAdapter = null;

    /*
     * Initialising connection to Firebase database.
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mSpecies = db.collection("SpeciesList");
    private CollectionReference mSelectedSpecies;
    private CollectionReference mSightingsCollection;

    // Create a HashMap for storing new sightings on database.
    private Map<String, Object> sighting = new HashMap<>();

    // Stores all species and their children from database
    private List<Species> speciesList;
    private List<SpeciesChild> speciesListChild;

    /*
        Initialise widgets that display information to user.
     */
    private EditText speciesSelected;
    private EditText speciesSelectedChild;
    private EditText extraInformation;
    private EditText total;
    private Button mRecord;

    private LocationManager locManager;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_sightings, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_sightings);

        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        /*
           Initialising objects with their IDs from layout resource
         */
        extraInformation = root.findViewById(R.id.extra_info);
        mRecord = root.findViewById(R.id.record);
        total = root.findViewById(R.id.total);
        total.setInputType(InputType.TYPE_CLASS_NUMBER);

        speciesSpinner = root.findViewById(R.id.SpinnerChoice_Species);
        speciesSelected = root.findViewById(R.id.species_selected);
        speciesSpinnerChild = root.findViewById(R.id.SpeciesChild);
        speciesSelectedChild = root.findViewById(R.id.SpeciesChildSelected);

        // initialise arrayLists
        speciesList = new ArrayList<>();
        speciesListChild = new ArrayList<>();

        /*
            Check whether user has an active connection or not, then perform specific task dependant on result.

            Connected = show species

            Not connected = warn user it will not work until they are connected
         */
        if (NetworkStatus.getInstance(getActivity()).isOnline()) {
            loadSpecies();
            mRecord.setEnabled(true);
        } else {
            Log.d(TAG, "onCreateView: no network");
            mRecord.setEnabled(false);
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

        /*
            This section works in tandem with the firebase DB
            Depending on what the user selects from the species list will determine what is shown in the "specieschild" list.
         */
        speciesArrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,speciesList);
        speciesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesArrayAdapter.notifyDataSetChanged();
        speciesSpinner.setAdapter(speciesArrayAdapter);
        speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Species species = (Species) parent.getSelectedItem();
                speciesSelected.setText(species.getName());
                String name = species.getName();
                speciesListChild.clear();
                loadSpeciesChild(name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "nothing selected");
            }
        });

        speciesChildArrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,speciesListChild);
        speciesChildArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesChildArrayAdapter.notifyDataSetChanged();
        speciesSpinnerChild.setAdapter(speciesChildArrayAdapter);
        speciesSpinnerChild.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                SpeciesChild speciesC = (SpeciesChild) parent.getSelectedItem();
                speciesSelectedChild.setText(speciesC.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "nothing selected");
            }
        });

        /*
            Method for submitting the users selection to database
            If the user disconnects while submitting the data is cached and submitted once a new connection is established

            Various validation goes on within this method.
         */
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTime = Calendar.getInstance().getTime();
                species = speciesSelected.getText().toString();
                speciesChild = speciesSelectedChild.getText().toString();
                extraInfo = extraInformation.getText().toString();
                howMany = 1;
                notValidated = true;
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                    location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                total.setInputType(InputType.TYPE_CLASS_NUMBER);
                if (currentTime.toString().equals("")) {
                    notValidated = false;
                } else if (species.equals("")) {
                    notValidated = false;
                } else if (speciesChild.equals("")) {
                    notValidated = false;
                } else if (extraInfo.equals("")) {
                    extraInfo = "None Submitted";
                }
                else if (!total.getText().toString().equals("")) {
                    howMany = Integer.parseInt(total.getText().toString());

                }
                if (!notValidated) {
                    Log.d(TAG, "Field was empty");
                    // do nothing
                } else {
                    sighting.put("Species", species);
                    sighting.put("Name", speciesChild);
                    sighting.put("Number Spotted", howMany);
                    sighting.put("Time", currentTime);
                    sighting.put("About", extraInfo);
                    if(location == null)
                    {
                        sighting.put("Location", "Location was not accessible");
                    }
                    else{
                        sighting.put("Location", "Longitude: " + location.getLongitude() +", "+ "Latitude: " + location.getLatitude());
                    }


                    mSightingsCollection = db.collection("Sighting");
                    mSightingsCollection.document(currentTime.toString()).set(sighting)
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


                }
                Toast.makeText(getActivity(), "Thank you for your submission", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    /*
        Loads species from database and then calls initFilledView() to display them dynamically
     */
    private void loadSpecies(){
        mSpecies.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                 //Log.d(TAG, document.getId() + " => " + document.getData());
                                String name = document.getString(KEY_NAME);

                                Species species = new Species(name);
                                speciesList.add(species);

                            }
                            initFilledView();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }

                });
    }

    /*
        Once data has been read in from database, update spinners to display it
     */
    private  void initFilledView(){
        speciesArrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),android.R.layout.simple_spinner_item,speciesList);
        speciesSpinner.setAdapter(speciesArrayAdapter);
        speciesArrayAdapter.notifyDataSetChanged();
    }

    /*
       This method loads the correct subspecies dependant on the users Species choice.
     */
    private void loadSpeciesChild(String speciesName){

        Log.d(TAG, "loadSpeciesChild:  HERE loadSpeciesChild");

        if(speciesName.equals("Amphibian")){
            mSelectedSpecies = db.collection("SpeciesList").document("Amphibian").collection("Amphibian");
        }
        else if(speciesName.equals("Bird")){
            mSelectedSpecies = db.collection("SpeciesList").document("Bird").collection("Bird");
        }
        else if(speciesName.equals("Fish")){
            mSelectedSpecies = db.collection("SpeciesList").document("Fish").collection("Fish");
        }
        else if(speciesName.equals("Invertebrate")){
            mSelectedSpecies = db.collection("SpeciesList").document("Invertebrate").collection("Invertebrate");
        }
        else if(speciesName.equals("Mammal")){
            mSelectedSpecies = db.collection("SpeciesList").document("Mammal").collection("Mammal");
        }
        else if(speciesName.equals("Reptile")){
            mSelectedSpecies = db.collection("SpeciesList").document("Reptile").collection("Reptile");
        }
        else if(speciesName.equals("Vegetation")){
            mSelectedSpecies = db.collection("SpeciesList").document("Vegetation").collection("Vegetation");
        }
        else {
            // do nothing atm
        }

        Log.d(TAG, "loadSpeciesChild: " + speciesName);
        namePassed(mSelectedSpecies);
        /// Add multiple if else's, better way to do it?
    }

    /*
        Display child species dependant on what name was passed.
     */
    private void namePassed(CollectionReference speciesSelected){
        speciesSelected.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.d(TAG,  "HERE namePassed");

                                String name = document.getString(KEY_NAME);
                                SpeciesChild sChild = new SpeciesChild(name);
                                speciesListChild.add(sChild);
                            }
                            initFilledViewChild();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    // Display child species results dynamically
    private  void initFilledViewChild(){
        speciesChildArrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,speciesListChild);
        speciesSpinnerChild.setAdapter(speciesChildArrayAdapter);
        speciesChildArrayAdapter.notifyDataSetChanged();
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