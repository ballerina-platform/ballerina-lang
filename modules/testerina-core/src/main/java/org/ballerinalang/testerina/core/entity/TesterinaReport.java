/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.core.entity;

import java.io.PrintStream;
import java.util.ArrayList;

public class TesterinaReport {

    private static ArrayList<TesterinaFunctionResult> functionResults = new ArrayList<TesterinaFunctionResult>();
    private static ArrayList<TesterinaFunctionResult> passedFunctionResults = new ArrayList<TesterinaFunctionResult>();
    private static ArrayList<TesterinaFunctionResult> failedFunctionResults = new ArrayList<TesterinaFunctionResult>();
    private static PrintStream outStream = System.err;
    static int passedFunctionCount;
    static int failedFunctionCount;
    static String newLine = System.getProperty("line.separator");

    public void addFunctionResult(TesterinaFunctionResult functionResult){
        functionResults.add(functionResult);
    }

    public ArrayList<TesterinaFunctionResult> getFunctionResults() {
        return functionResults;
    }

    public static void printTestSummary(){
        if(!functionResults.isEmpty()) {
            passedFunctionCount = 0;
            failedFunctionCount = 0;
            passedFunctionResults.clear();
            failedFunctionResults.clear();
            for (TesterinaFunctionResult result : functionResults) {
                if (result.isTestFunctionPassed()) {
                    passedFunctionCount++;
                    passedFunctionResults.add(result);
                } else {
                    failedFunctionCount++;
                    failedFunctionResults.add(result);
                }
            }
            outStream.println("Result : " + newLine + "Test Run : " + functionResults.size() + ", Test Passed: "
                    + passedFunctionCount + ", Test Failures: " + failedFunctionCount);
            printTestDetails();
        }
    }

    public static void printTestDetails(){
        outStream.println( newLine + "Failed Tests");
        for (TesterinaFunctionResult failedResult : failedFunctionResults) {
            outStream.println( newLine + failedResult.getTestFunctionName() + ": "+ failedResult.getAssertFailureMessage());
        }
    }
}
