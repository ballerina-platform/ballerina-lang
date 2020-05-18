package org.ballerinalang.langlib.testutils;

import java.util.ArrayList;
import java.util.List;

public class CTestGroup {
    String name;
    String description;
    List<CTests> tests = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void addTests(CTests tests) {
        this.tests.add(tests);
    }

    public List<CTests> getTests() {
        return this.tests;
    }
}
