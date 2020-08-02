package com.example.epic.FragTests;

import androidx.fragment.app.FragmentManager;
import androidx.test.rule.ActivityTestRule;

import com.example.epic.R;
import com.example.epic.ui.Activities.MainActivity;
import com.example.epic.ui.Fragments.AboutFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class AboutTest {

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
        AboutFragment aboutF = new AboutFragment();
        FragmentManager fragmentManager = mainActTestRule.getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, aboutF).commit();
    }


    @Test
    public void TestDisplayTitle() throws Exception {
        onView(withId(R.id.website_url)).perform(scrollTo()).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        mainActTestRule.getActivity().getSupportFragmentManager().popBackStack();
    }
}
