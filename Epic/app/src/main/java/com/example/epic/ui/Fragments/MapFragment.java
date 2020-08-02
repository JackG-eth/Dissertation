package com.example.epic.ui.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.epic.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/*
    MapFragment Class handles everything to do with maps within the application
 */
public class MapFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback{

    //Request code for location permission request
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Tag for debugging
    private static final String TAG = "MapFrag Debugging";

    /*
     * Strings for accessing various database columns
     */
    private static final String KEY_NAME = "Name";
    private static final String KEY_LAT = "Lat";
    private static final String KEY_Long = "Long";
    private static final String KEY_DESCRIPTION = "Description";

    // Boolean to check whether network is enabled or not
    protected boolean network_enabled;

    // Animation while map is loading
    private ProgressBar progressBar = null;

    /*
     * Initialising connection to Firebase database.
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mPoints = db.collection("PointsOfInterest");


    /*
     * Setup required for google maps and retrieving users location
     */
    protected double longitude = 0.0;
    protected double latitude = 0.0;
    private Location location;
    private LocationManager locManager;
    private GoogleMap mMap;
    private MapView mMapView;

    private Dialog dialogMarker;
    private View dialogViewMarker;

    // Button that allows users to record sightings while using the map
    private FloatingActionButton eyeSpy;
    // Button that allows users to make the map more interactive
    private FloatingActionButton infoButton;

    // ArrayList to store markers once call to database has been made.
    protected ArrayList<Marker> mMarkers;

    /*
     * Simple checkbox methods for making the map more interactive
     */
    private CheckBox legend_map = null;
    private CheckBox legend_points = null;
    private CheckBox location_points = null;
    private ImageView legend_image = null;
    private String alreadyTriggered = "";

    // LocationListener to listen for updates in the users location
    private LocationListener locationListener;

    // Boolean to check whether the user has selected a new option
    private boolean CheckBoxTicked = false;

    // Used when the user resumes the activity and location is required.
    protected Location updated;

    // View for the fragment
    private View dView;

    // Dialog Frag view
    private Dialog dialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_map);

        // Man android class for accessing location services, initialised in onCreate method
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        /**
         * Method to listen for user location updates
         *
         * If user has enabled interactive distance calculations then this method will be used.
         *
         * Change to proximity Alert?? EASTER! need network to test
         */
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if(location!=null){
                    calculateDistance(location);
                    updated = location;
                }
                else {
                    //do nothing
                }

            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
            }
        };

        /*
         * Checking that the user has granted permission to use the map functionality.
         *
         * Location manager gets the users last known location.
         * Also checking whether the user has an active internet connection, if "yes" get their specific coordinates
         */
        if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(network_enabled){

                if(location!=null){
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                }
            }
            // Listen for user updates, Time/distance can be changed.
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,1,locationListener);
        } else {
            requestStoragePermission();
        }

        mMapView = root.findViewById(R.id.mapView);

        progressBar = root.findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(View.VISIBLE);

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        // Initialize the gms maps services
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * This class automatically initializes the maps system and the view.
         *
         * We set the boundaries of the map, add a ground overlay and update the camera settings
         */
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                /* Coordinates of site*/
                final LatLngBounds NewarkBounds = new LatLngBounds(
                        new LatLng(50.822137, -0.358761),
                        new LatLng(50.828420, -0.343136)
                );

                mMap = googleMap;

                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.setMyLocationEnabled(true);

                // Adding ground overlay image (new map)
                GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromResource(R.mipmap.final_map))
                        .positionFromBounds(NewarkBounds)
                        .visible(true);

                mMap.addGroundOverlay(newarkMap);

                mMap.setMapType(3);


                /*
                 * Once the map has loaded we update the camera settings
                 * This provides a better user experience
                 */
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {

                        Log.d(TAG, "onMapLoaded: HERE");
                        // Set the camera to the greatest possible zoom level that includes the bounds
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(NewarkBounds,0));

                        //Centering the camera within bounds:
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NewarkBounds.getCenter(), 16));

                        //Restricting the user's scrolling and panning within bounds:
                        mMap.setLatLngBoundsForCameraTarget(NewarkBounds);

                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

            }
        });

        eyeSpy = root.findViewById(R.id.eye);

        /**
         * Initialise eye-spy button
         * Pass the users location, remove current fragment from stack and replace it with a new one.
         */
        eyeSpy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpeciesFragment speciesFrag = new SpeciesFragment();

                Bundle bundle = new Bundle();

                bundle.putString("Longitude",Double.toString(longitude));
                bundle.putString("Latitude",Double.toString(latitude));
                speciesFrag.setArguments(bundle);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(), speciesFrag, "SpeciesFrag Eye spy")
                        .addToBackStack(null)
                        .commit();

            }
        });

        infoButton = root.findViewById(R.id.info_map);
        legend_image = root.findViewById(R.id.legend_map_image);
        legend_image.bringToFront();
        legend_image.setVisibility(View.INVISIBLE);

        // Animation while map is loading, enhance user experience
        dialog = new Dialog(getActivity());
        dView = getLayoutInflater().inflate(R.layout.map_dialog,null);

        legend_map = dView.findViewById(R.id.checkbox_legend);
        legend_points = dView.findViewById(R.id.checkbox_points_of_interest);
        location_points = dView.findViewById(R.id.checkbox_points_of_interest_location_animated);

        /*
         * Interactive button to make map more interactive
         * Allows user to select what extra features they want
         * Each Checkbox calls a subset of methods
         */
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.setContentView(dView);

                legend_map.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            dialog.dismiss();
                            displayLegend();
                        }
                        else{
                            hideLegend();
                        }
                    }
                });

                legend_points.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            dialog.dismiss();
                            showMarkers();

                        }
                        else {
                            hideMarkers();

                        }
                    }
                });

                location_points.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            CheckBoxTicked = true;

                            dialog.dismiss();

                        }
                        else {
                            CheckBoxTicked = false;
                        }
                    }
                });

                dialog.create();
                dialog.show();

            }
        });

        new MyTask().execute();

        return root;
    }

    /*
        If user wants to see the map legend, display it
     */
    private void displayLegend(){
        if(legend_map.isChecked()){
            legend_image.setVisibility(View.VISIBLE);
        }
    }

    /*
        Hide the map legend
    */
    private void hideLegend(){
        if(!legend_map.isChecked()){
            legend_image.setVisibility(View.INVISIBLE);
        }
    }

    /*
        Display markers on map, providing more information
     */
    private void showMarkers(){
        if(legend_points.isChecked()){
            for (Marker m : mMarkers) {
                m.setVisible(true);
            }
        }
    }

    /*
        Hide the markers
     */
    private void hideMarkers(){
        if(!legend_points.isChecked()){
            for (Marker m : mMarkers) {
                m.setVisible(false);
            }
        }
    }

    /*
        Method to calculate the distance between the user and markers.
        If the user is within 10 meters of a maker it displays specific information for that location
        The method distanceTo uses the "WGS84 ellipsoid'
     */
    private void calculateDistance(Location currentLocation){
        double markerlat;
        double markerlong;
        boolean triggered = false;
        if(CheckBoxTicked) {
            for (Marker m : mMarkers) {
                markerlat = m.getPosition().latitude;
                markerlong = m.getPosition().longitude;
                Location markerLocation = new Location("currentlocation");
                markerLocation.setLatitude(markerlat);
                markerLocation.setLongitude(markerlong);
                float distance = currentLocation.distanceTo(markerLocation);
                Log.d(TAG, "calculateDistance: " + distance);
                Log.d(TAG, "Cannot show dialog already been shown: " + alreadyTriggered);
                if (distance <= 20 && !alreadyTriggered.equals(m.getTitle())) {
                    triggered = true;
                    alreadyTriggered = m.getTitle();
                    Log.d(TAG, "Test Name" + alreadyTriggered);
                    Log.d(TAG, "calculateDistance: " + "Here Inside distance <10");
                    dialogMarker = new Dialog(getActivity());
                    dialogViewMarker = getLayoutInflater().inflate(R.layout.location_popup_dialog,null);
                    final TextView markerTitle = dialogViewMarker.findViewById(R.id.markerTitle);
                    markerTitle.setText(m.getTitle());
                    final TextView markerInfo = dialogViewMarker.findViewById(R.id.markerInfo);
                    markerInfo.setText(m.getSnippet() +" Distance from marker: " +distance + "meters");
                    break;
                } else {
                    // do nothing
                }
            }
            if(triggered){
                dialogMarker.setContentView(dialogViewMarker);
                dialogMarker.create();
                dialogMarker.show();
            }
        }
        else{
            // not enabled
        }

    }


    /*
        This is a backup method as the user cannot access the map fragment unless permissions have been granted.
     */
    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            /*
                Create a new dialog and ask for permission from the user
                If the user selects no then the process will not work as intended
             */
            new AlertDialog.Builder(getContext())
                    .setTitle("Permission required")
                    .setMessage("This feature will not work if it cannot access your location")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /*
        If permissions have been granted then the map is initialised and the users location is displayed provided it passes other tests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getContext(),"Permissions was not granted",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
        Default imported method
     */
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    /*
    Default imported method
    */
    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    /**
     * This method retrieves the marker information from the database and adds them to an arrayList
     */
    private class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
                mPoints.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    mMarkers = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // Log.d(TAG, document.getId() + " => " + document.getData());
                                        String name = document.getString(KEY_NAME);
                                        double latCoordinate = document.getDouble(KEY_LAT);
                                        double longCoordinate = document.getDouble(KEY_Long);
                                        String description = document.getString(KEY_DESCRIPTION);

                                        mMarkers.add(mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latCoordinate ,longCoordinate))
                                                .title(name)
                                                .snippet(description).visible(false)));
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            return null;
       }
    }

    /*
        Fragments use onStop and not onDestroy
        This method removes the listener looking for location updates when the app is not open
        Saves battery.
     */
    @Override
    public void onStop() {

        Log.d(TAG, "onStop: " + "here");
        super.onStop();
        locManager.removeUpdates(locationListener);

    }

    /*
        When the user opens the application again and the map fragment is resumed, enable the location listener again.
     */
    @Override
    public void onResume() {
        super.onResume();

        if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,1,locationListener);
        }
        if (updated!=null) {
            calculateDistance(updated);
        }

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