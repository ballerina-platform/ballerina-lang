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
import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.core.entity.TesterinaReport;
import org.ballerinalang.testerina.core.entity.TesterinaResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * BTestRunner entity class.
 */
public class BTestRunner {

    private static Path programDirPath = Paths.get(System.getProperty("user.dir"));
    private static PrintStream errStream = System.err;
    private static PrintStream outStream = System.out;
    private TesterinaReport tReport = new TesterinaReport();
    private Map<String, TestSuite> testSuites = new HashMap<>();

    /**
     * Executes a given set of ballerina program files.
     *
     * @param sourceFilePaths List of @{@link Path} of ballerina files
     * @param groups          List of groups to be included
     */
    public void runTest(Path[] sourceFilePaths, List<String> groups) {
        runTest(sourceFilePaths, groups, true);
    }

    /**
     * Executes a given set of ballerina program files.
     *
     * @param sourceFilePaths List of @{@link Path} of ballerina files
     * @param groups          List of groups to be included/excluded
     * @param shouldIncludeGroups    flag to specify whether to include or exclude provided groups
     */
    public void runTest(Path[] sourceFilePaths, List<String> groups, boolean shouldIncludeGroups) {
        TesterinaRegistry.getInstance().setGroups(groups);
        TesterinaRegistry.getInstance().setShouldIncludeGroups(shouldIncludeGroups);
        // compile
        Arrays.stream(sourceFilePaths).forEach(k -> {
            CompileResult compileResult = BCompileUtil.compile(programDirPath.toString(), k.toString(), CompilerPhase
                    .CODE_GEN);
            for (Diagnostic diagnostic : compileResult.getDiagnostics()) {
                errStream.println(diagnostic.getKind() + ": " + diagnostic.getPosition() + " " + diagnostic
                        .getMessage());
            }
            if (Arrays.stream(compileResult.getDiagnostics()).collect(Collectors.toList()).size() > 0) {
                throw new BallerinaException("Compilation failure in: " + k.toString());
            }
            outStream.println("Processing compiled result of package: " + compileResult.getProgFile().getEntryPkgName
                    ());
            TesterinaRegistry.getInstance().addProgramFile(compileResult.getProgFile());
            // process the compiled files
            ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
            processorServiceLoader.forEach(plugin -> {
                if (plugin instanceof TestAnnotationProcessor) {
                    ((TestAnnotationProcessor) plugin).packageProcessed(compileResult.getProgFile());
                }
            });
//            PackageInfo packageInfo = compileResult.getProgFile().getEntryPackage();
//            testSuites.computeIfAbsent(packageInfo.getPkgPath(), func -> new TestSuite(packageInfo.getPkgPath()));
//            AnnotationProcessor.processAnnotations(packageInfo, testSuites.get(packageInfo.getPkgPath()),
//                    mockFunctionsMap, groups, shouldIncludeGroups);

        });
        // execute the test programs
        execute();
        // print the report
        tReport.printTestSummary();
    }

//    private void process(ProgramFile programFile) {
//        for (PackageInfo packageInfo : programFile.getPackageInfoEntries()) {
//            if (packageInfo.getPkgPath().startsWith("ballerina")) {
//                // skip this
//            } else {
//                if (testSuites.get(packageInfo.getPkgPath()) == null) {
//                    // create a new suite instance
//                    testSuites.put(packageInfo.getPkgPath(), new TestSuite(packageInfo.getPkgPath()));
//                    //processTestSuites
//                }
//                AnnotationProcessor.processAnnotations(this, programFile, packageInfo, testSuites.get(packageInfo
//                        .getPkgPath()), groups, excludeGroups);
//            }
//        }
//    }

    private void execute() {
        testSuites = TesterinaRegistry.getInstance().getTestSuites();
        if (testSuites.isEmpty()) {
            throw new BallerinaException("No test functions found in the provided ballerina files.");
        }

        AtomicBoolean shouldSkip = new AtomicBoolean();

        testSuites.forEach((packageName, suite) -> {
            suite.getInitFunction().invoke();

            outStream.println("Executing test suite for package: " + packageName);
//            for (ProgramFile programFile : suite.getProgramFiles()) {
//                AnnotationProcessor.injectMocks(mockFunctionsMap, programFile);
//            }

            suite.getBeforeSuiteFunctions().forEach(test -> {
                String errorMsg;
                try {
                    test.invoke();
                } catch (BallerinaException e) {
                    shouldSkip.set(true);
                    errorMsg = String.format("Failed to execute before test suite function [%s] of test suite " +
                                             "package [%s]. Cause: %s", test.getName(), packageName, e.getMessage());
                    errStream.println(errorMsg);
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
                            errStream.println(errorMsg);
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
                        errStream.println(errorMsg);
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
                                        } else {
                                            // cannot happen due to validations done at annotation processor
                                        }
                                    }
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
                    errStream.println(errorMsg);
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
                            test.getTestFunction().getName(), packageName, e.getMessage());
                    errStream.println(error);
                }

                // run the afterEach tests
                suite.getAfterEachFunctions().forEach(afterEachTest -> {
                    String errorMsg2;
                    try {
                        afterEachTest.invoke();
                    } catch (BallerinaException e) {
                        errorMsg2 = String.format("Failed to execute after each test function" + " [%s] for the test " +
                                                  "[%s] of test suite package [%s]. Cause: %s", afterEachTest.getName(),
                                test.getTestFunction().getName(), packageName, e.getMessage());
                        errStream.println(errorMsg2);
                    }
                });
            });
        });
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


