package com.example.epic.FragTests;

import androidx.fragment.app.FragmentManager;
import androidx.test.rule.ActivityTestRule;

import com.example.epic.R;
import com.example.epic.ui.Activities.MainActivity;
import com.example.epic.ui.Fragments.FeedbackFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class FeedbackTest {


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
        FeedbackFragment feedbackTest = new FeedbackFragment();
        FragmentManager fragmentManager = mainActTestRule.getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, feedbackTest).commit();
    }

    @Test
    public void TestFeedbackYes() throws Exception {
        onView(withId(R.id.button_yes)).perform(click());
    }

    @Test
    public void TestFeedbackNo() throws Exception {
        onView(withId(R.id.button_no)).perform(click());
    }

    @Test
    public void Response() throws Exception {
        onView(withId(R.id.feedback_response)).perform(typeText("1"));
    }

    @Test
    public void Send() throws Exception {
        onView(withId(R.id.feedback_send)).perform(click());
    }

    @After
    public void tearDown() throws Exception {
        mainActTestRule.getActivity().getSupportFragmentManager().popBackStack();
    }
}
