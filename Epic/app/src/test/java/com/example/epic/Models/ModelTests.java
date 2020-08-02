package com.example.epic.Models;


import com.example.epic.ui.Models.Species;
import com.example.epic.ui.Models.SpeciesChild;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ModelTests {


    @Test
    public void SetNameTest() {
        boolean valid = true;
        Species speciesTest = new Species("Test");

        speciesTest.setName("Worked");
        // android.util.Patterns does not work with junit, so instead implemented the regex sequence is applies.
        if (!speciesTest.getName().equals("Worked")) {
            valid = false;
        }
        assertTrue(valid);
    }

    @Test
    public void SetNameTestSpeciesChild() {
        boolean valid = true;
        SpeciesChild speciesTest2 = new SpeciesChild("Test");

        speciesTest2.setName("Worked");
        // android.util.Patterns does not work with junit, so instead implemented the regex sequence is applies.
        if (!speciesTest2.getName().equals("Worked")) {
            valid = false;
        }
        assertTrue(valid);
    }
}
