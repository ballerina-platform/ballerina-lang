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

import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Function;

import java.util.ArrayList;

/**
 * TesterinaFile entity class
 */
public class TesterinaFile {

    private ArrayList<TesterinaFunction> testFunctions = new ArrayList<>();
    private ArrayList<TesterinaFunction> beforeTestFunctions = new ArrayList<>();
    private ArrayList<TesterinaFunction> afterTestFunctions = new ArrayList<>();
    private BallerinaFile bFile;

    public TesterinaFile(BallerinaFile bFile) {
        this.bFile = bFile;
        extractTestFunctions(bFile);
    }

    /**
     * Getter method for testFunctions. Returns an ArrayList of functions starting with prefix 'test'.
     *
     * @return ArrayList
     */
    public ArrayList<TesterinaFunction> getTestFunctions() {
        return this.testFunctions;
    }

    /**
     * Getter method for testFunctions. Returns an ArrayList of functions starting with prefix 'test'.
     *
     * @return ArrayList
     */
    public ArrayList<TesterinaFunction> getBeforeTestFunctions() {
        return this.beforeTestFunctions;
    }

    /**
     * Getter method for testFunctions. Returns an ArrayList of functions starting with prefix 'test'.
     *
     * @return ArrayList
     */
    public ArrayList<TesterinaFunction> getAfterTestFunctions() {
        return this.afterTestFunctions;
    }

    /**
     * Getter method for 'bFile'. Returns the BallerinaFile object.
     *
     * @return BallerinaFile
     */
    public BallerinaFile getBFile() {
        return this.bFile;
    }

    /**
     * Get the list of 'test/beforeTest' functions, parsed from the *.bal file
     *
     * @param bFile Path to Bal file.
     */
    private void extractTestFunctions(BallerinaFile bFile) {
        Function[] functions = bFile.getFunctions();
        for (Function function : functions) {
            String name = function.getName();
            if (name.toUpperCase().startsWith(TesterinaFunction.PREFIX_TEST)) {
                TesterinaFunction tFunction = new TesterinaFunction(function.getName(), TesterinaFunction.Type.TEST,
                        function, this);
                this.testFunctions.add(tFunction);
            } else if (name.toUpperCase().startsWith(TesterinaFunction.PREFIX_BEFORETEST)) {
                TesterinaFunction tFunction = new TesterinaFunction(function.getName(),
                        TesterinaFunction.Type.BEFORE_TEST, function, this);
                this.beforeTestFunctions.add(tFunction);
            } else if (name.toUpperCase().startsWith(TesterinaFunction.PREFIX_AFTERTEST)) {
                TesterinaFunction tFunction = new TesterinaFunction(function.getName(),
                        TesterinaFunction.Type.AFTER_TEST, function, this);
                this.afterTestFunctions.add(tFunction);
            }
        }
    }

}
