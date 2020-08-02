package com.example.epic.ui.Models;

/**
 * Species Class defines a simple object that is accessed in various fragments, prevents duplication of code
 */
public class Species {

    private String name;

    public Species(String name) {
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
