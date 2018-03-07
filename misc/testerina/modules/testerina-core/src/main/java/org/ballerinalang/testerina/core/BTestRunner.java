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

package org.ballerinalang.testerina.core;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.core.entity.TesterinaContext;
import org.ballerinalang.testerina.core.entity.TesterinaReport;
import org.ballerinalang.testerina.core.entity.TesterinaResult;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BTestRunner entity class.
 */
public class BTestRunner {

    private static Path programDirPath = Paths.get(System.getProperty("user.dir"));
    private static PrintStream outStream = System.err;
    private TesterinaReport tReport = new TesterinaReport();

    /**
     * Executes a given set of ballerina program files.
     *
     * @param sourceFilePaths List of @{@link Path} of ballerina files
     * @param groups          List of groups to be included
     */
    public void runTest(Path[] sourceFilePaths, List<String> groups) {
        runTest(sourceFilePaths, groups, false);
    }

    /**
     * Executes a given set of ballerina program files.
     *
     * @param sourceFilePaths List of @{@link Path} of ballerina files
     * @param groups          List of groups to be included/excluded
     * @param ignoreGroups    flag to specify whether to include or exclude provided groups
     */
    public void runTest(Path[] sourceFilePaths, List<String> groups, boolean ignoreGroups) {
        CompileResult[] compileResults = Arrays.stream(sourceFilePaths).map(k -> k.toString()).map(k -> BCompileUtil
                .compile(programDirPath.toString(), k, CompilerPhase.CODE_GEN)).toArray(CompileResult[]::new);
        Arrays.stream(compileResults).forEach(k -> {
            for (Diagnostic diagnostic : k.getDiagnostics()) {
                outStream.println(diagnostic.getMessage());
            }
        });
        Arrays.stream(compileResults).forEachOrdered(compileResult -> TesterinaRegistry.getInstance().addProgramFile
                (compileResult.getProgFile()));

        executeTestFunctions(TesterinaRegistry.getInstance().getProgramFiles(), groups, ignoreGroups);
        tReport.printTestSummary();
    }

    private void executeTestFunctions(Collection<ProgramFile> programFiles, List<String> groups, boolean
            excludeGroups) {
        TesterinaContext tFile = new TesterinaContext(groups, excludeGroups);
        tFile.process(programFiles);
        Map<String, TestSuite> testSuites = tFile.getTestSuites();

        if (testSuites.isEmpty()) {
            throw new BallerinaException("No test functions found in the provided ballerina files.");
        }

        AtomicBoolean shouldSkip = new AtomicBoolean();

        testSuites.forEach((packageName, suite) -> {
            // TODO there can be only a single Program file for a package. So check and improve
            suite.getInitFunction().invoke();

            outStream.println("Executing test suite for package: " + packageName);
            for (ProgramFile programFile : suite.getProgramFiles()) {
                AnnotationProcessor.injectMocks(tFile.getMockFunctionsMap(), programFile);
            }

            suite.getBeforeSuiteFunctions().forEach(test -> {
                String errorMsg;
                try {
                    test.invoke();
                } catch (BallerinaException e) {
                    shouldSkip.set(true);
                    errorMsg = String.format("Failed to execute before test suite function [%s] of test suite " +
                                             "package [%s]. Cause: %s", test.getName(), packageName, e.getMessage());
                    outStream.println(errorMsg);
                }
            });
            suite.getTests().forEach(test -> {
                if (!shouldSkip.get()) {
                    // run the beforeEach tests
                    suite.getBeforeEachFunctions().forEach(beforeEachTest -> {
                        String errorMsg;
                        try {
                            beforeEachTest.invoke();
                        } catch (BallerinaException e) {
                            shouldSkip.set(true);
                            errorMsg = String.format("Failed to execute before each test function [%s] for the "
                                                     + "test [%s] of test suite package [%s]. Cause: %s",
                                    beforeEachTest.getName(),
                                    test.getTestFunction().getName(), packageName, e.getMessage());
                            outStream.println(errorMsg);
                        }
                    });
                }
                if (!shouldSkip.get()) {
                    // run before tests
                    String errorMsg;
                    try {
                        if (test.getBeforeTestFunctionObj() != null) {
                            test.getBeforeTestFunctionObj().invoke();
                        }
                    } catch (BallerinaException e) {
                        shouldSkip.set(true);
                        errorMsg = String.format("Failed to execute before test function" + " [%s] for the test " +
                                                 "[%s] of test suite package [%s]. Cause: %s",
                                test.getBeforeTestFunctionObj().getName
                                        (), test.getTestFunction().getName(), packageName, e.getMessage());
                        outStream.println(errorMsg);
                    }
                }
                // run the test
                TesterinaResult functionResult = null;
                String errorMsg = null;
                boolean isTestPassed = false;
                try {
                    if (!shouldSkip.get()) {
                        BValue[] valueSets = null;
                        if (test.getDataProviderFunction() != null) {
                            valueSets = test.getDataProviderFunction().invoke();
                        }
                        if (valueSets == null) {
                            test.getTestFunction().invoke();
                            // report the test result
                            functionResult = new TesterinaResult(test.getTestFunction().getName(), true,
                                    shouldSkip.get(), errorMsg);
                            tReport.addFunctionResult(functionResult);
                        } else {
                            for (BValue value : valueSets) {
                                if (value instanceof BRefValueArray) {
                                    BRefValueArray array = (BRefValueArray) value;
                                    for (BIterator it = array.newIterator(); it.hasNext(); ) {
                                        BValue[] vals = it.getNext(0);
                                        if (vals[1] instanceof BNewArray) {
                                            BNewArray bNewArray = (BNewArray) vals[1];
                                            BValue[] args = new BValue[(int) bNewArray.size()];
                                            for (int j = 0; j < bNewArray.size(); j++) {
                                                args[j] = bNewArray.getBValue(j);
                                            }
                                            test.getTestFunction().invoke(args);
                                            functionResult = new TesterinaResult(test.getTestFunction().getName(),
                                                    true, shouldSkip.get(), errorMsg);
                                            tReport.addFunctionResult(functionResult);
                                        }
                                    }
//                                        test.getTestFunction().invoke(array.getBValue());
                                } else {
                                    test.getTestFunction().invoke(new BValue[]{value});
                                    // report the test result
                                    functionResult = new TesterinaResult(test.getTestFunction().getName(), true,
                                            shouldSkip.get(), errorMsg);
                                    tReport.addFunctionResult(functionResult);
                                }
                            }
                        }
                        isTestPassed = true;
                    } else {
                        // report the test result
                        functionResult = new TesterinaResult(test.getTestFunction().getName(), false,
                                shouldSkip.get(), errorMsg);
                        tReport.addFunctionResult(functionResult);
                    }
                } catch (Exception e) {
                    errorMsg = String.format("Failed to execute the test function [%s] of test suite package [%s]. "
                                             + "Cause: %s", test.getTestFunction().getName(), packageName,
                            e.getMessage());
                    outStream.println(errorMsg);
                    // report the test result
                    functionResult = new TesterinaResult(test.getTestFunction().getName(), false,
                            shouldSkip.get(), errorMsg);
                    tReport.addFunctionResult(functionResult);
                    return;
                }

                // run the after tests
                String error;
                try {
                    if (test.getAfterTestFunctionObj() != null) {
                        test.getAfterTestFunctionObj().invoke();
                    }
                } catch (BallerinaException e) {
                    error = String.format("Failed to execute after test function" + " [%s] for the test [%s] of test " +
                                          "suite package [%s]. Cause: %s", test.getAfterTestFunctionObj().getName(),
                            test
                                    .getTestFunction().getName(), packageName, e.getMessage());
                    outStream.println(error);
                }

                // run the afterEach tests
                suite.getAfterEachFunctions().forEach(afterEachTest -> {
                    String errorMsg2;
                    try {
                        afterEachTest.invoke();
                    } catch (BallerinaException e) {
                        errorMsg2 = String.format("Failed to execute after each test function" + " [%s] for the test " +
                                                  "[%s] of test suite package [%s]. Cause: %s", afterEachTest.getName(),
                                test
                                        .getTestFunction().getName(), packageName, e.getMessage());
                        outStream.println(errorMsg2);
                    }
                });
            });
        });

        //        ArrayList<TesterinaFunction> testFunctions = tFile.getTestFunction();
        //        ArrayList<TesterinaFunction> beforeTestFunctions = tFile.getBeforeTestFunction();
        //        ArrayList<TesterinaFunction> afterTestFunctions = tFile.getAfterTestFunction();
        //
        //        if (testFunctions.isEmpty()) {
        //            throw new BallerinaException("No test functions found in the provided ballerina files.");
        //        }
        //
        //        //before test
        //        for (TesterinaFunction tFunction : beforeTestFunctions) {
        //            try {
        //                tFunction.invoke();
        //            } catch (BallerinaException e) {
        //                outStream.println(
        //                        "error in '" + tFunction.getName() + "': " + e.getMessage());
        //            }
        //        }
        //
        //        // Executing Tests
        //        for (TesterinaFunction tFunction : testFunctions) {
        //
        //            boolean isTestPassed = true;
        //            // Check whether the function is disabled
        //            if (!tFunction.getRunTest()) {
        //                tReport.incrementSkipCount();
        //                continue;
        //            }
        //
        //            // check whether value set is present and execute tests
        //            List<String[]> valueSets = tFunction.getValueSet();
        //            String errorMessage = "";
        //            if (tFunction.getValueSet().size() > 0) {
        //                for (String[] valueSet : valueSets) {
        //                    isTestPassed = true;
        //                    try {
        //                        // Parsing the args to invoke function
        //            tFunction.invoke(Arrays.stream(valueSet).map(s -> new BString(s)).toArray(BString[]::new));
        //                    } catch (BallerinaException e) {
        //                        isTestPassed = false;
        //                        errorMessage = "valueSet: " + Arrays.toString(valueSet) + " " + e.getMessage();
        //                        outStream.println("test '" + tFunction.getName() + "' failed: " + errorMessage);
        //                    } catch (Exception e) {
        //                        isTestPassed = false;
        //                        errorMessage = "valueSet: " + Arrays.toString(valueSet) + " " + e.getMessage();
        //                        outStream.println("test '" + tFunction.getName() + "' has an error: " + errorMessage);
        //                    }
        //                    // if there are no exception thrown, test is passed
        //                    TesterinaResult functionResult = new TesterinaResult(tFunction.getName(), isTestPassed,
        //                            errorMessage);
        //                    tReport.addFunctionResult(functionResult);
        //                }
        //            } else {
        //                try {
        //                    tFunction.invoke();
        //                } catch (BallerinaException e) {
        //                    isTestPassed = false;
        //                    errorMessage = e.getMessage();
        //                    outStream.println("test '" + tFunction.getName() + "' failed: " + errorMessage);
        //                } catch (Exception e) {
        //                    isTestPassed = false;
        //                    errorMessage = e.getMessage();
        //                    outStream.println("test '" + tFunction.getName() + "' has an error: " + errorMessage);
        //                }
        //                // if there are no exception thrown, test is passed
        //                TesterinaResult functionResult = new TesterinaResult(tFunction.getName(), isTestPassed,
        //                        errorMessage);
        //                tReport.addFunctionResult(functionResult);
        //            }
        //        }
        //
        //        //after test
        //        for (TesterinaFunction tFunction : afterTestFunctions) {
        //            try {
        //                tFunction.invoke();
        //            } catch (Exception e) {
        //                outStream.println("error in '" + tFunction.getName() + "': " + e.getMessage());
        //            }
        //        }
    }

    /**
     * Return the Test report of program runner.
     *
     * @return @{@link TesterinaReport} object
     */
    public TesterinaReport getTesterinaReport() {
        return tReport;
    }

}


