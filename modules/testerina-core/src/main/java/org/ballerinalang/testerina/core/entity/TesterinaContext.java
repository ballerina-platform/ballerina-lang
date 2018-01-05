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

import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * TesterinaContext entity class.
 */
public class TesterinaContext {

    private ArrayList<TesterinaFunction> testFunctions = new ArrayList<>();
    private ArrayList<TesterinaFunction> beforeTestFunctions = new ArrayList<>();
    private ArrayList<TesterinaFunction> afterTestFunctions = new ArrayList<>();

    public static final String ASSERTION_EXCEPTION_CATEGORY = "assert-failure";

    public TesterinaContext(ProgramFile[] programFiles) {
        Arrays.stream(programFiles).forEach(this::extractTestFunctions);
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
     * Get the list of 'test/beforeTest' functions, parsed from the *.bal file.
     *
     * @param programFile {@link ProgramFile}.
     */
    private void extractTestFunctions(ProgramFile programFile) {
        Arrays.stream(programFile.getPackageInfoEntries())
                .map(PackageInfo::getFunctionInfoEntries)
                .flatMap(Arrays::stream)
                .forEachOrdered(f -> addTestFunctions(programFile, f));
    }

    private void addTestFunctions(ProgramFile programFile, FunctionInfo functionInfo) {
            String nameUpperCase = functionInfo.getName().toUpperCase(Locale.ENGLISH);
        if (nameUpperCase.startsWith(TesterinaFunction.PREFIX_TEST) &&
                !nameUpperCase.endsWith(TesterinaFunction.INIT_SUFFIX)) {
            TesterinaFunction tFunction = new TesterinaFunction(programFile, functionInfo, 
                    TesterinaFunction.Type.TEST);
            this.testFunctions.add(tFunction);
        } else if (nameUpperCase.startsWith(TesterinaFunction.PREFIX_BEFORETEST) &&
                !nameUpperCase.endsWith(TesterinaFunction.INIT_SUFFIX)) {
            TesterinaFunction tFunction = new TesterinaFunction(programFile, functionInfo, 
                    TesterinaFunction.Type.BEFORE_TEST);
            this.beforeTestFunctions.add(tFunction);
        } else if (nameUpperCase.startsWith(TesterinaFunction.PREFIX_AFTERTEST) &&
                !nameUpperCase.endsWith(TesterinaFunction.INIT_SUFFIX)) {
            TesterinaFunction tFunction = new TesterinaFunction(programFile, functionInfo, 
                    TesterinaFunction.Type.AFTER_TEST);
            this.afterTestFunctions.add(tFunction);
        }
    }

}
