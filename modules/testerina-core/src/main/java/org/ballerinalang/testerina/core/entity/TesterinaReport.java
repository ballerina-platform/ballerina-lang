package org.ballerinalang.testerina.core.entity;

import java.util.ArrayList;

/**
 * Created by nirodha on 2/19/17.
 */
public class TesterinaReport {
    private static ArrayList<TesterinaFunctionResult> functionResults;

    TesterinaReport(){
        functionResults = new ArrayList<TesterinaFunctionResult>();
    }

    public void addFunctionResult(TesterinaFunctionResult functionResult){
        functionResults.add(functionResult);
    }

    public ArrayList<TesterinaFunctionResult> getFunctionResults() {
        return functionResults;
    }


}
