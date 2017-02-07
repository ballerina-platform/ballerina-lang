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

    private String name;
    private ArrayList<TesterinaFunction> testFunctions;
    private BallerinaFile bFile;

    public TesterinaFile(String name, String resourcePath, BallerinaFile bFile) {
        this.name = name;
        this.bFile = bFile;
        setTestFunctions(this.bFile);
    }

    private void setTestFunctions(BallerinaFile bFile) {
        /**
         * Private method to set only the 'test' functions, parsed from the *.bal file
         *
         * @param bFile Path to Bal file.
         * @return void
         */
        this.testFunctions = new ArrayList<TesterinaFunction>();
        Function[] allFunctions = bFile.getFunctions();
        for (int i = 0; i < allFunctions.length; i++) {
            String name = allFunctions[i].getFunctionName();
            if (name.startsWith(TesterinaFunction.PREFIX_TEST)) {
                Function bFunc = allFunctions[i];
                TesterinaFunction tFunction = new TesterinaFunction(bFunc.getFunctionName(),
                        TesterinaFunction.Type.TEST, bFunc, this);
                this.testFunctions.add(tFunction);
            }
        }
    }

    public ArrayList<TesterinaFunction> getTestFunctions() {
        /**
         * Getter method for testFunctions. Returns an ArrayList of functions starting with prefix 'test'.
         * @return ArrayList
         */
        return this.testFunctions;
    }

    public String getName() {
        /**
         * Getter method for 'name'. Returns the file name.
         * @return String
         */
        return this.name;
    }

    public BallerinaFile getBFile() {
        /**
         * Getter method for 'bFile'. Returns the BallerinaFile object.
         * @return BallerinaFile
         */
        return this.bFile;
    }

}
