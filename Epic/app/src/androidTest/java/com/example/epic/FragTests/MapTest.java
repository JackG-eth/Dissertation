package com.example.epic.FragTests;

import androidx.fragment.app.FragmentManager;
import androidx.test.rule.ActivityTestRule;

import com.example.epic.R;
import com.example.epic.ui.Activities.MainActivity;
import com.example.epic.ui.Fragments.MapFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class MapTest {

    //This rule provides functional testing of a single Activity
    @Rule
    public ActivityTestRule<MainActivity> mainActTestRule = new ActivityTestRule<>(MainActivity.class);

    /*
    Called before executing test phase
    Initiates the main activity
     */
    @Before
    public void setUp() throws Exception {
        mainActTestRule.getActivity().getFragmentManager().beginTransaction();
        MapFragment MapF = new MapFragment();
        FragmentManager fragmentManager = mainActTestRule.getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, MapF).commit();
    }


    /*
        Test if there is an empty parameter alert is shown
        button does not work
     */
    @Test
    public void TestDisplay() throws Exception {
        onView(withId(R.id.info_map)).perform(click());
        onView(withId(R.id.checkbox_legend)).check(matches(withText("Display Map Legend.")));
    }

    /*
    Test if there is an empty parameter alert is shown
    button does not work
 */
    @Test
    public void TestDisplayInterest() throws Exception {
        onView(withId(R.id.info_map)).perform(click());
        onView(withId(R.id.checkbox_points_of_interest)).check(matches(withText("Display points of interest.")));
    }

    /*
    Test if there is an empty parameter alert is shown
    button does not work
 */
    @Test
    public void TestLocation() throws Exception {
        onView(withId(R.id.info_map)).perform(click());
        onView(withId(R.id.checkbox_points_of_interest_location_animated)).check(matches(withText("Display facts based on location.")));
    }

    /*
    Test if there is an empty parameter alert is shown
    button does not work
 */
    @Test
    public void TestEye() throws Exception {
        onView(withId(R.id.eye)).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        mainActTestRule.getActivity().getSupportFragmentManager().popBackStack();
    }
}
