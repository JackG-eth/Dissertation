package com.example.epic.ui.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.epic.R;
import com.example.epic.ui.Fragments.AboutFragment;
import com.example.epic.ui.Fragments.FeedbackFragment;
import com.example.epic.ui.Fragments.HomeFragment;
import com.example.epic.ui.Fragments.MapFragment;
import com.example.epic.ui.Fragments.SightingsFragment;
import com.example.epic.ui.Fragments.SpeciesFragment;
import com.example.epic.ui.Fragments.SpeciesFragmentChild;
import com.google.android.material.navigation.NavigationView;
import com.google.ar.core.ArCoreApk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/*
    MainActivity encompasses the entire navigation of the application
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,SpeciesFragmentChild.OnFragmentInteractionListener,
        SpeciesFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener, MapFragment.OnFragmentInteractionListener, SightingsFragment.OnFragmentInteractionListener,
        FeedbackFragment.OnFragmentInteractionListener, AboutFragment.OnFragmentInteractionListener, EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    // Boolean to check whether user has granted required permissions
    private boolean permission;
    private boolean permissionCamera;
    private boolean arAvailable;

    /*
        parameters to handles permission requests
     */
    private static final int RC_LOCATION = 1;
    private static final String[] LOCATION_AND_CONTACTS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE};
    private static final int RC_CAMERA = 2;
    private static final String[] CAMERA_AND_STORAGE = { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // initialise authentication
    private FirebaseAuth mAuth;

    //Logic to handle login activity
    private Intent loginIntent = null;
    private Intent arIntent = null;

    // Debugging to deal with fragment backstack
    private static final String TAG = "BackStack";

    // Drawer layout and toolbar initialisation
    private DrawerLayout drawer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = this.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();

        /**
         * Logic that deals with activity rotations, might be used in the future.
         */
        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class fragmentClass = null;
            fragmentClass = HomeFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                toolbar.setTitle(R.string.menu_home);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        }

        maybeEnableArButton();

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(!arAvailable){
            Menu nav_menu = navigationView.getMenu();
            nav_menu.findItem(R.id.nav_ar).setVisible(false);
        }


    }

    /*
     Check that the user has given permission to access their network and location
     */
    private boolean hasLocationAndNetworkPermissions() {
        return EasyPermissions.hasPermissions(this, LOCATION_AND_CONTACTS);
    }

    /*
        Check the user has given permission to access the camera and storage of the device
     */
    private boolean hasCameraAndStorage() {
        return EasyPermissions.hasPermissions(this, CAMERA_AND_STORAGE);
    }

    /*
        https://github.com/googlesamples/easypermissions Api makes checking for permissions much cleaner
     */
    @AfterPermissionGranted(RC_LOCATION)
    public void locationAndContactsTask() {
        if (hasLocationAndNetworkPermissions()) {
            // Have permissions, do the thing!
            permission = true;

        }
        else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location_network),
                    RC_LOCATION,
                    LOCATION_AND_CONTACTS);

        }

    }
    @AfterPermissionGranted(RC_CAMERA)
    public void CameraAndStorage() {
        if (hasCameraAndStorage()) {
            // Have permissions, do the thing!
            permissionCamera = true;

        }
        else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_camera),
                    RC_CAMERA,
                    CAMERA_AND_STORAGE);

        }

    }

    /*
        Deals with the result from requesting permissions from the user
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Code to handle going back from fragments
     */
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            Log.d(TAG, Integer.toString(count));
            getSupportFragmentManager().popBackStack();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }


    /*
        initialise logout button
        force user back to login page
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            mAuth.signOut();
            updateUI();
        }

        if (id == R.id.action_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Confirm");
            builder.setMessage("Are you sure?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing, but close the dialog
                    mAuth.getCurrentUser().delete();
                    mAuth.signOut();
                    updateUI();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

        if (id == R.id.action_menu){
            drawer.openDrawer(Gravity.LEFT);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * If the ARCore API is not available to the device, hide the button to access it.
     */
    void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isSupported()) {
            Log.d(TAG, "maybeEnableArButton: " + "Is supported");
            arAvailable = true;
        } else { // Unsupported or unknown.
            Log.d(TAG, "maybeEnableArButton: " + "Is not supported");
            arAvailable = false;
        }
    }


    /*
        Deals with Fragment Back stack
        Special logic for map fragment to prevent access without required permissions
        When user goes back from an activity the back stack is cleared to prevent a build up and prevent
        app crashes/ save power and increase performance
     */
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String ftag = "";
        Fragment fragment = null;
        Class fragmentClass = null;
        if (id == R.id.nav_home) {
            fragmentClass = HomeFragment.class;
            toolbar.setTitle(R.string.menu_home);
            ftag= "Home";
        } else if (id == R.id.nav_map) {
            if (hasLocationAndNetworkPermissions()){
                fragmentClass = MapFragment.class;
                toolbar.setTitle(R.string.menu_map);
                ftag = "Map";
            }
            else if(!permission){
                locationAndContactsTask();
                fragmentClass = HomeFragment.class;
                toolbar.setTitle(R.string.menu_home);
                ftag= "Home";
            }
            else {
                fragmentClass = MapFragment.class;
                toolbar.setTitle(R.string.menu_map);
                ftag = "Map";
            }
        } else if (id == R.id.nav_sightings) {
            if (hasLocationAndNetworkPermissions()){
                fragmentClass = SightingsFragment.class;
                toolbar.setTitle(R.string.menu_sightings);
                ftag= "Sightings";
            }
            else if(!permission){
                locationAndContactsTask();
                fragmentClass = HomeFragment.class;
                toolbar.setTitle(R.string.menu_home);
                ftag= "Home";
            }
            else {
                fragmentClass = SightingsFragment.class;
                toolbar.setTitle(R.string.menu_sightings);
                ftag= "Sightings";
            }
        } else if (id == R.id.nav_species_list) {
            fragmentClass = SpeciesFragment.class;
            toolbar.setTitle(R.string.menu_species);
            ftag= "Species List";
        } else if (id == R.id.nav_feedback) {
            fragmentClass = FeedbackFragment.class;
            toolbar.setTitle(R.string.menu_feedback);
            ftag= "Feedback";
        } else if (id == R.id.nav_about) {
            fragmentClass = AboutFragment.class;
            toolbar.setTitle(R.string.menu_about);
            ftag= "About";
        }
        else if (id == R.id.nav_ar) {
            if (hasCameraAndStorage() & arAvailable){
                arIntent = new Intent(getApplicationContext(), AugmentedImageActivity.class);
                startActivityForResult(arIntent, 0);
                toolbar.setTitle(R.string.menu_ar);
                Log.d(TAG, "onNavigationItemSelected: " + "HERE Camera pass");
            }
            else if(!permissionCamera){
                CameraAndStorage();
            }
            else if(!arAvailable){
                // Do nothing
            }
            else {
                // Do nothing
            }
        }
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count != 0) {
            for(int i = 0; i < count; ++i) {
                fragmentManager.popBackStack();
            }
        }

        if(id != R.id.nav_ar) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, fragment, ftag);
            fragmentTransaction.addToBackStack(ftag);
            fragmentTransaction.commit();

            if (fragment.getTag() == "Home") {
                Log.d(TAG, "FragmentTag " + fragment.getTag());
                fragmentManager.popBackStack();
            }
        }



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        updateUI();
        maybeEnableArButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        maybeEnableArButton();
    }

    /*
      If user is not logged in they are returned to the login activity.
     */
    private void updateUI() {
        mAuth.getCurrentUser();
        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If current user does not exist force to login activity
        if(currentUser == null) {
            Log.d(TAG, "updateUI: hereinside  =null");
            loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(loginIntent, 0);
        } else {
            // do nothing signed in ();
        }
    }

    /*
        Default Method
     */
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /*
    Default Method
    */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    /*
       Default Method
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    /*
        Default Method
     */
    @Override
    public void onRationaleAccepted(int requestCode) {

    }

    /*
    Default Method
    */
    @Override
    public void onRationaleDenied(int requestCode) {

    }
}
