package com.example.epic.ui.Models;

/**
 * SpeciesChild Class defines a simple object that is accessed in various fragments, prevents duplication of code
 */
public class SpeciesChild {

    private String name;

    public SpeciesChild(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* for default adapter override.*/
    @Override
    public String toString(){
        return name;
    }
}
