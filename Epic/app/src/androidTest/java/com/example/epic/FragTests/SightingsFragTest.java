package com.example.epic.FragTests;

import androidx.fragment.app.FragmentManager;
import androidx.test.rule.ActivityTestRule;

import com.example.epic.R;
import com.example.epic.ui.Activities.MainActivity;
import com.example.epic.ui.Fragments.SightingsFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class SightingsFragTest {


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
        SightingsFragment SightingsF = new SightingsFragment();
        FragmentManager fragmentManager = mainActTestRule.getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, SightingsF).commit();
    }


    @Test
    public void TestDisplayTitle() throws Exception {
        onView(withId(R.id.title)).check(matches(withText("What have you spotted?")));
    }

    @Test
    public void TestDisplaySpecies() throws Exception {
        onView(withId(R.id.species_selected)).perform(typeText("1"));
    }

    @Test
    public void TestDisplaySpeciesChild() throws Exception {
        onView(withId(R.id.SpeciesChildSelected)).perform(typeText("1"));
    }

    @Test
    public void TestNumber() throws Exception { ;
        onView(withId(R.id.total)).perform(typeText("1"));
    }

    @Test
    public void ExtraInfo() throws Exception {
        onView(withId(R.id.extra_info)).perform(typeText("1"));
    }

    @Test
    public void RecordButton() throws Exception {
        onView(withId(R.id.record)).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        mainActTestRule.getActivity().getSupportFragmentManager().popBackStack();
    }
}
