package org.ballerinalang.langlib.testutils;

import java.util.ArrayList;
import java.util.List;

public class CTests {

    String testName;
    String description;
    String path;
    List<TestSteps> testFunctions = new ArrayList<>();

    public String getTestName() {
        return testName;
    }
    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setDescription(String description) {this.description = description; };
    public String getDescription(){ return this.description; };

    public void setPathName(String path){
        this.path = path;
    }
    public String getPath(){
        return this.path;
    }

    public List<TestSteps> getTestFunctions() {
        return testFunctions;
    }
    public void addTestFunctions(TestSteps function) {
        this.testFunctions.add(function);
    }

}
