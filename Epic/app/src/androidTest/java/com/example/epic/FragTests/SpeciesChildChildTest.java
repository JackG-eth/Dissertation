package com.example.epic.FragTests;

import androidx.fragment.app.FragmentManager;
import androidx.test.rule.ActivityTestRule;

import com.example.epic.R;
import com.example.epic.ui.Activities.MainActivity;
import com.example.epic.ui.Fragments.SpeciesFragmentChildChild;

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


public class SpeciesChildChildTest {

    //This rule provides functional testing of a single Activity
    @Rule
    public ActivityTestRule<MainActivity> mainActTestRule = new ActivityTestRule<>(MainActivity.class);

    /*
    Called before executing test phase
    Initiates the main activity
     */
    @Before
    public void setUp() throws Exception {
        String Name,Species,About,Image;
        Name = "Name";
        Species = "Species";
        About = "About";
        Image = "Image";
        mainActTestRule.getActivity().getFragmentManager().beginTransaction();
        SpeciesFragmentChildChild speciesFragmentChildChild = new SpeciesFragmentChildChild(Name,Image,About,Species);
        FragmentManager fragmentManager = mainActTestRule.getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, speciesFragmentChildChild).commit();
    }

    @Test
    public void TestChildButton() throws Exception {
        onView(withId(R.id.record_sighting_now)).check(matches(withText("Record Sighting")));
        onView(withId(R.id.record_sighting_now)).perform(click());
        onView(withId(R.id.how_many)).check(matches(withText("How many did you spot?")));
        onView(withId(R.id.number_of_animals)).perform(typeText("1"));
        onView(withId(R.id.number_submit)).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        mainActTestRule.getActivity().getSupportFragmentManager().popBackStack();
    }
}
