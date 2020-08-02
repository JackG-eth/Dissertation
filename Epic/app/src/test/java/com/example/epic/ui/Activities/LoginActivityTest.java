package com.example.epic.ui.Activities;


import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LoginActivityTest {

    private static final String INVALID_EMAIL = "test123";
    private static final String VALID_EMAIL = "jh01023@surrey.ac.uk";
    private static final String INVALID_PASSWORD = "test123";
    private static final String VALID_PASSWORD = "Testing123!";
    // https://firebase.google.com/docs/rules/unit-tests

    @Test
    public void emailIsValid() {
        boolean valid = true;
        // android.util.Patterns does not work with junit, so instead implemented the regex sequence is applies.
        if (!VALID_EMAIL.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
           valid = false;
        }
        assertTrue(valid);
    }

    @Test
    public void emailIsInValid() {
        boolean valid = false;
        // android.util.Patterns does not work with junit, so instead implemented the regex sequence is applies.
        if (!INVALID_EMAIL.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
            valid = true;
        }
        assertTrue(valid);
    }

    @Test
    public void PasswordIsValid() {
        boolean valid = true;
        if (VALID_PASSWORD.isEmpty() ||!VALID_PASSWORD.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$")) {
            valid = false;
        }
        assertTrue(valid);
    }

    @Test
    public void PasswordIsInValid() {
        boolean valid = false;
        if (INVALID_PASSWORD.isEmpty() ||!INVALID_PASSWORD.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$")) {
            valid = true;
        }
        assertTrue(valid);
    }
}