package com.example.epic.FragTests;

import android.os.SystemClock;

import androidx.fragment.app.FragmentManager;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import com.example.epic.R;
import com.example.epic.ui.Activities.MainActivity;
import com.example.epic.ui.Fragments.SpeciesFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class SpeciesListTest {

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
        SpeciesFragment SpeciesF = new SpeciesFragment();
        FragmentManager fragmentManager = mainActTestRule.getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, SpeciesF).commit();
    }

    @Test
    public void TestNavigationToSpeciesList() throws Exception {
        SystemClock.sleep(1500);
        onView(withId(R.id.species_recycler)).perform(RecyclerViewActions.actionOnItem(
                hasDescendant(withText("Amphibian")),
                click()));
        onView(withId(R.id.species_recycler))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Common Frog")),
                        click()));
        onView(withId(R.id.record_sighting_now)).check(matches(withText("Record Sighting")));

    }
    @After
    public void tearDown() throws Exception {
        mainActTestRule.getActivity().getSupportFragmentManager().popBackStack();
    }
}
