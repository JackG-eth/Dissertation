package com.example.epic;


import androidx.test.rule.ActivityTestRule;

import com.example.epic.ui.Activities.LoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

/*
 This class tests the login and registration activities UI
 */
public class LoginRegisterActivityTest {


    //This rule provides functional testing of a single Activity
    @Rule
    public ActivityTestRule<LoginActivity> LoginActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    // Create the activity object
    private LoginActivity mLoginActivity = null;

    /*
    Called before executing test phase
    Initiates the login activity
     */
    @Before
    public void setUp() throws Exception {

        mLoginActivity = LoginActivityTestRule.getActivity();

    }

    /*
    Tests that the main activity object is not null
    */
    @Test
    public void testLaunch(){
        assertNotNull(mLoginActivity);
    }

    /*s
    Testing Login Button Works
     */
    @Test
    public void TestLoginButton() throws Exception {
        onView(withId(R.id.email_input)).perform(typeText("jh01023@surrey.ac.uk"));
        onView(withId(R.id.password_input)).perform(scrollTo()).perform(typeText("Testing123!"));
        onView(withId(R.id.login_button)).perform(scrollTo()).perform(click());
    }

    /*
    Testing registration button opens registration activity and closes correctly
     */
    @Test
    public void TestRegistrationButton() throws Exception {
        onView(withId(R.id.signup)).perform(scrollTo()).perform(click());
        onView(withId(R.id.return_to_login)).check(matches(withText("Already have an account? Login here")));
        onView(withId(R.id.email_input_register)).perform(typeText("jh01023@surrey.ac.uk"));
        onView(withId(R.id.password_input_register)).perform(scrollTo()).perform(typeText("Testing123!"));
        onView(withId(R.id.password_input_register_confirm)).perform(scrollTo()).perform(typeText("Testing123!"));
        onView(withId(R.id.create_account)).perform(scrollTo()).perform(click());
        onView(withId(R.id.return_to_login)).perform(scrollTo()).perform(click());
        onView(withId(R.id.signup)).check(matches(withText("Click here to sign up!")));
    }

    /**
     * Simple Check to ensure privacy policy opens
     */
    @Test
    public void PrivacyPolicy() throws Exception {
        onView(withId(R.id.privacy_policy)).perform(scrollTo()).perform(click());
    }


    /*
    Testing google button functions as expected
     */
    @Test
    public void TestGoogleButton() throws Exception {
        onView(withId(R.id.google_signin)).perform(scrollTo()).perform(click());
    }


    @After
    public void tearDown() throws Exception {
        mLoginActivity = null;
    }
}