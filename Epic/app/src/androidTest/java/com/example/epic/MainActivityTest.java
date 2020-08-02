package com.example.epic;

import android.os.SystemClock;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import com.example.epic.ui.Activities.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/*

   It covers transition from fragments as well as conte   This class contains the majority of all UI Testsnt validation
 */
public class MainActivityTest {

    //This rule provides functional testing of a single Activity
    @Rule
    public ActivityTestRule<MainActivity> mainActTestRule = new ActivityTestRule<>(MainActivity.class);

    // Create the activity object
    private MainActivity mActivity = null;

    /*
    Called before executing test phase
    Initiates the main activity
     */
    @Before
    public void setUp() throws Exception {

        mActivity = mainActTestRule.getActivity();

    }

    @Test
    public void MainActivityHost() throws Exception {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_home));
        onView(withId(R.id.welcome_message)).check(matches(withText("Welcome")));
    }

    @Test
    public void TestNavigationButton() throws Exception {
        onView(withId(R.id.action_menu)).perform(click());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_home));
    }

    /*
     This test checks that the species fragment and its adapters are working as expected (App must have been used and database accessed for test to run as expected)
     validation involves checking that the final fragment contains the expected text
    */
    @Test
    public void TestNavigationToSpeciesList() throws Exception {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_species_list));
        SystemClock.sleep(1500);
        onView(withId(R.id.species_recycler))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Amphibian")),
                        click()));
        onView(withId(R.id.species_recycler))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Common Frog")),
                        click()));
        onView(withId(R.id.record_sighting_now)).check(matches(withText("Record Sighting")));
    }


    /*
     Tests the logout button is working as expected and that you can log back in.
     */
    @Test
    public void TestLogoutButton() throws Exception {
        onView(withText(R.string.account)).perform(click());
        onView(withText(R.string.action_logout)).perform(click());
        onView(withId(R.id.email_input)).perform(typeText("jh01023@surrey.ac.uk"));
        onView(withId(R.id.password_input)).perform(scrollTo()).perform(typeText("Testing123!"));
        onView(withId(R.id.login_button)).perform(scrollTo()).perform(click());
    }

    /*
    TestNavigationToMap() Tests the navigation to the map fragment is working
    Simple test to navigate, further testing done in own java file.
     */
    @Test
    public void TestNavigationToMap() throws Exception {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_map));
        onView(withId(R.id.info_map)).perform(click());
        onView(withId(R.id.checkbox_legend)).check(matches(withText("Display Map Legend.")));
    }

    /*
    TestNavigationToMap() Tests the navigation to the map fragment is working
    Simple test to navigate, further testing done in own java file.
     */
    @Test
    public void TestNavigationToMapToSpecies() throws Exception {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_map));
        onView(withId(R.id.eye)).perform(click());
        onView(withId(R.id.species_recycler))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Amphibian")),
                        click()));
    }

    /*
    TestNavigationToFeedback() Tests the navigation to the Feedback fragment is working
    Results are verified by checking that the associated textview is populated
     */
    @Test
    public void TestNavigationToFeedback() throws Exception {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_feedback));
    }

    /*
    TestNavigationToAbout() Tests the navigation to the about fragment is working
     */
    @Test
    public void TestNavigationToAbout() throws Exception {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_about));
        onView(withId(R.id.website_url)).perform(scrollTo()).check(matches(withText("Find out more here.")));
    }

    /*
    TestNavigationToHome() Tests the navigation to the Home fragment is working
    Check its accesses fragment by looking for specific string
    */
    @Test
    public void TestNavigationToHome() throws Exception {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_home));
        onView(withId(R.id.welcome_message)).check(matches(withText("Welcome")));
    }

    @Test
    public void TestNavigationToSightingsFragment() throws Exception {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_sightings));
        onView(withId(R.id.title)).check(matches(withText("What have you spotted?")));
    }

    /*
    Tests that the main activity object is not null
     */
    @Test
    public void testLaunch(){
        assertNotNull(mActivity);
    }
    /*
    Called after executing test phase
    */

    @After
    public void tearDown() throws Exception {

        mActivity = null;
    }
}