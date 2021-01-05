/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.util;

import io.ballerina.projects.testsuite.Test;
import io.ballerina.projects.testsuite.TestSuite;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.ballerinalang.test.runtime.BTestRunner;
import org.ballerinalang.test.runtime.exceptions.BallerinaTestException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static io.ballerina.runtime.api.utils.IdentifierUtils.encodeNonFunctionIdentifier;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.ANON_ORG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;

/**
 * Utility methods.
 */
public class TesterinaUtils {

    private static final PrintStream outStream = System.out;
    private static final PrintStream errStream = System.err;

    /**
     * Cleans up any remaining testerina metadata.
     *
     * @param path The path of the Directory/File to be deleted
     */
    public static void cleanUpDir(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        } catch (IOException e) {
            errStream.println("Error occurred while deleting the dir : " + path.toString() + " with error : "
                                      + e.getMessage());
        }
    }

    /**
     * Execute tests in build.
     *
     * @param sourceRootPath source root path
     * @param testSuite test meta data
     */
    public static void executeTests(Path sourceRootPath, TestSuite testSuite) throws RuntimeException {
        try {
            BTestRunner testRunner = new BTestRunner(outStream, errStream);
            // Run the tests
            testRunner.runTest(testSuite);
            cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
            if (testRunner.getTesterinaReport().isFailure()) {
                throw new RuntimeException("there are test failures");
            }
        } catch (BallerinaTestException e) {
            errStream.println("error: " + e.getMessage());
            RuntimeUtils.silentlyLogBadSad(e);
            throw e;
        } catch (Throwable e) {
            RuntimeUtils.silentlyLogBadSad(e);
            throw new RuntimeException("test execution failed due to runtime exception");
        }
    }

    /**
     * Format error message.
     *
     * @param errorMsg error message
     * @return formatted error message
     */
    public static String formatError(String errorMsg) {
        StringBuilder newErrMsg = new StringBuilder();
        errorMsg = errorMsg.replaceAll("\n", "\n\t    ");
        List<String> msgParts = Arrays.asList(errorMsg.split("\n"));

        for (String msg : msgParts) {
            if (msgParts.indexOf(msg) != 0 && !msg.equals("\t    ")) {
                msg = "\t    \t" + msg.trim();
            }
            if (!msg.equals("\t    ")) {
                newErrMsg.append(msg).append("\n");
            }
        }
        return newErrMsg.toString();
    }

    /**
     * Provides Qualified Class Name.
     *
     * @param orgName     Org name
     * @param packageName Package name
     * @param version Package version
     * @param className   Class name
     * @return Qualified class name
     */
    public static String getQualifiedClassName(String orgName, String packageName,
                                               String version, String className) {
        if (!DOT.equals(packageName)) {
            className = encodeNonFunctionIdentifier(packageName) + "." + version.replace('.', '_') + "." + className;
        }
        if (!ANON_ORG.equals(orgName)) {
            className = encodeNonFunctionIdentifier(orgName) + "." +  className;
        }
        return className;
    }

    /**
     * Provides the updated list of test functions for single Execution after adding the dependent tests.
     *
     * @param currentTests the tests available in current test suite
     * @param functions    the list of test functions provided by user
     * @return updated list of test function names
     */
    private static List<String> getUpdatedFunctionList(List<Test> currentTests, List<String> functions) {
        List<String> updatedFunctionList = new ArrayList<>(functions);
        for (String functionName : functions) {
            List<String> dependentFunctions = getAllDependentFunctions(getTest(functionName, currentTests),
                    currentTests);
            for (String dependentTest : dependentFunctions) {
                if (!updatedFunctionList.contains(dependentTest)) {
                    updatedFunctionList.add(dependentTest);
                }
            }
        }
        return updatedFunctionList;
    }

    /**
     * Get the Relevant Test object for a given test function name.
     *
     * @param testName name of the test function
     * @param tests    the tests available in current test suite
     * @return
     */
    private static Test getTest(String testName, List<Test> tests) {
        for (Test test : tests) {
            if (testName.equals(test.getTestName())) {
                return test;
            }
        }
        return null;
    }

    /**
     * Get all dependent functions for a given test function.
     *
     * @param test         the Test to find dependencies
     * @param currentTests the tests available in current test suite
     * @return list of dependent functions
     */
    private static List<String> getAllDependentFunctions(Test test, List<Test> currentTests) {
        List<String> completeDependentTestList = new ArrayList<>();
        if (test != null) {
            List<String> dependentTests = test.getDependsOnTestFunctions();
            if (dependentTests != null) {
                for (String dependentTest : dependentTests) {
                    if (!completeDependentTestList.contains(dependentTest)) {
                        completeDependentTestList.add(dependentTest);
                    }
                    List<String> otherDependentTests = getAllDependentFunctions(getTest(dependentTest, currentTests),
                            currentTests);
                    for (String otherDependentTest : otherDependentTests) {
                        if (!completeDependentTestList.contains(otherDependentTest)) {
                            completeDependentTestList.add(otherDependentTest);
                        }
                    }
                }
            }
        }
        return completeDependentTestList;
    }

    /**
     * Provides the updated test functions for single Execution after adding the dependent tests.
     *
     * @param suite         current test suite
     * @param functions     the list of test functions provided by user
     * @return updated list of test functions
     */
    public static List<Test> getSingleExecutionTests(TestSuite suite, List<String> functions) {
        List<String> filteredList = new ArrayList<>();

        // Go through each function in the functionsList
        for (String function : functions) {
            if (function.contains(":")) {
                String[] functionDetail = function.split(":");
                try {
                    if (functionDetail[0].equals(suite.getPackageID())) {
                        if (functionDetail[1].equals(TesterinaConstants.WILDCARD)) {
                            handleWildCard(filteredList, suite.getTests());
                        } else if (functionDetail[1].endsWith(TesterinaConstants.WILDCARD)) {
                            handleEndingWithWildCard(filteredList, suite.getTests(), functionDetail[1]);
                        } else {
                            filteredList.add(functionDetail[1]);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    errStream.println("Error occured while executing tests. Test list cannot be empty");
                }
            } else {
                if (function.endsWith(TesterinaConstants.WILDCARD)) {
                    handleEndingWithWildCard(filteredList, suite.getTests(), function);
                } else {
                    filteredList.add(function);
                }
            }
        }

        List<Test> currentTests = suite.getTests();
        List<String> updatedFunctionList = getUpdatedFunctionList(currentTests, filteredList);
        List<Test> updatedTestList = new ArrayList<>();
        for (Test test : currentTests) {
            if (updatedFunctionList.contains(test.getTestName())) {
                updatedTestList.add(test);
            }
        }
        return updatedTestList;
    }

    private static void handleWildCard(List<String> filteredList, List<Test> suiteTests) {
        for (Test test : suiteTests) {
            filteredList.add(test.getTestName());
        }
    }

    private static void handleEndingWithWildCard(List<String> filteredList, List<Test> suiteTests, String function) {
        String fn = function.replace(TesterinaConstants.WILDCARD, "");
        for (Test test : suiteTests) {
            if (test.getTestName().startsWith(fn)) {
                filteredList.add(test.getTestName());
            }
        }
    }

    public static List<org.ballerinalang.test.runtime.entity.Test> getSingleExecutionTestsOld(
            List<org.ballerinalang.test.runtime.entity.Test> currentTests, List<String> functions) {
        return Collections.emptyList();
    }

}
