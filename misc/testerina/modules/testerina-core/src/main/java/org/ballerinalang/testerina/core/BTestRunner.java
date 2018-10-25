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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.core.entity.TesterinaReport;
import org.ballerinalang.testerina.core.entity.TesterinaResult;
import org.ballerinalang.testerina.util.TesterinaUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * BTestRunner entity class.
 */
public class BTestRunner {

    private static PrintStream errStream = System.err;
    private static PrintStream outStream = System.out;
    private TesterinaReport tReport = new TesterinaReport();
    private TesterinaRegistry registry = TesterinaRegistry.getInstance();
    private List<String> sourcePackages = new ArrayList<>();

    /**
     * Executes a given set of ballerina program files.
     *
     * @param sourceRoot      source root
     * @param sourceFilePaths List of @{@link Path} of ballerina files
     * @param groups          List of groups to be included
     */
    public void runTest(String sourceRoot, Path[] sourceFilePaths, List<String> groups) {
        runTest(sourceRoot, sourceFilePaths, groups, true);
    }

    /**
     * Executes a given set of ballerina program files when running tests using the test command.
     *
     * @param sourceRoot      source root
     * @param sourceFilePaths List of @{@link Path} of ballerina files
     * @param groups          List of groups to be included/excluded
     * @param shouldIncludeGroups    flag to specify whether to include or exclude provided groups
     */
    public void runTest(String sourceRoot, Path[] sourceFilePaths, List<String> groups, boolean shouldIncludeGroups) {
        registry.setGroups(groups);
        registry.setShouldIncludeGroups(shouldIncludeGroups);
        compileAndBuildSuites(sourceRoot, sourceFilePaths);
        // Filter the test suites
        filterTestSuites();
        // execute the test programs
        execute(false);
    }

    /**
     * Executes a given set of ballerina program files when running tests using the build command.
     *
     * @param packageList map containing bLangPackage nodes along with their compiled program files
     */
    public void runTest(Map<BLangPackage, CompiledBinaryFile.ProgramFile> packageList) {
        registry.setGroups(Collections.emptyList());
        registry.setShouldIncludeGroups(true);
        buildSuites(packageList);
        // Filter the test suites
        filterTestSuites();
        // execute the test programs
        execute(true);
    }

    /**
     * Filter the test suites for only the modules given. Since when compiling modules in the test annotation
     * processor we create test suites for the import modules as well. To avoid them from running we have to filter
     * the only the modules provided from the test suite
     */
    private void filterTestSuites() {
        registry.setTestSuites(registry.getTestSuites().entrySet()
                                       .stream()
                                       .filter(map -> sourcePackages.contains(map.getKey()))
                                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    }
    /**
     * lists the groups available in tests.
     *
     * @param sourceRoot      source root of the project
     * @param sourceFilePaths module or program file paths
     */
    public void listGroups(String sourceRoot, Path[] sourceFilePaths) {
        //Build the test suites
        compileAndBuildSuites(sourceRoot, sourceFilePaths);
        List<String> groupList = getGroupList();
        if (groupList.size() == 0) {
            outStream.println("There are no groups available!");
        } else {
            outStream.println("Following groups are available : ");
            outStream.println(groupList);
        }
    }

    /**
     * Returns a distinct list of groups in test functions.
     *
     * @return a list of groups
     */
    public List<String> getGroupList() {

        Map<String, TestSuite> testSuites = registry.getTestSuites();
        if (testSuites.isEmpty()) {
            throw new BallerinaException("No test functions found in the provided ballerina files.");
        }
        List<String> groupList = new ArrayList<>();
        testSuites.forEach((packageName, suite) -> {
            suite.getTests().forEach(test -> {
                if (test.getGroups().size() > 0) {
                    groupList.addAll(test.getGroups());
                }
            });
        });
        return groupList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Compiles the source and populate the registry with suites when executing tests using the test command.
     * @param sourceRoot source root
     * @param sourceFilePaths List of @{@link Path} of ballerina files
     */
    private void compileAndBuildSuites(String sourceRoot, Path[] sourceFilePaths) {
        outStream.println("Compiling tests");
        if (sourceFilePaths.length == 0) {
            outStream.println("    No tests found");
            return;
        }
        // Reuse the same compiler context so that modules already compiled and persisted in the module cache are not
        // compiled again.
        CompilerContext compilerContext = BCompileUtil.createCompilerContext(sourceRoot, CompilerPhase.CODE_GEN);
        Arrays.stream(sourceFilePaths).forEach(sourcePackage -> {
            // compile
            CompileResult compileResult = BCompileUtil.compileWithTests(compilerContext, sourcePackage.toString(),
                CompilerPhase.CODE_GEN);
            // print errors
            for (Diagnostic diagnostic : compileResult.getDiagnostics()) {
                errStream.println(diagnostic.getKind().toString().toLowerCase(Locale.ENGLISH) + ":"
                                          + diagnostic.getPosition() + " " + diagnostic.getMessage());
            }
            if (compileResult.getErrorCount() > 0) {
                throw new BLangCompilerException("compilation contains errors");
            }

            String packageName = TesterinaUtils.getFullModuleName(sourcePackage.toString());
            // Add test suite to registry if not added.
            addTestSuite(packageName);
            // Keeps a track of the sources that are being tested
            sourcePackages.add(packageName);

            // Set the source file name if the module is the default package
            if (packageName.equals(Names.DEFAULT_PACKAGE.value)) {
                registry.getTestSuites().get(packageName).setSourceFileName(sourcePackage.toString());
            }

            // set the debugger
            ProgramFile programFile = compileResult.getProgFile();
            processProgramFile(programFile);
        });
        registry.setTestSuitesCompiled(true);
    }

    /**
     * Populate the registry with suites using the compiled program file when executing tests with the build command.
     *
     * @param packageList map containing bLangPackage nodes along with their compiled program files
     */
    private void buildSuites(Map<BLangPackage, CompiledBinaryFile.ProgramFile> packageList) {
        packageList.forEach((sourcePackage, compiledBinaryFile) -> {
            String packageName = TesterinaUtils.getFullModuleName(sourcePackage.packageID.getName().getValue());
            // Add a test suite to registry if not added. In this module there are no tests to be executed. But we need
            // to say that there are no tests found in the module to be executed
            addTestSuite(packageName);
            // Keeps a track of the sources that are being built
            sourcePackages.add(packageName);
            ProgramFile pFile = LauncherUtils.getExecutableProgram(compiledBinaryFile);
            processProgramFile(pFile);
        });

        registry.setTestSuitesCompiled(true);
    }

    /**
     * Add test suite to registry if not added.
     * @param packageName module name
     */
    private void addTestSuite(String packageName) {
        registry.getTestSuites().computeIfAbsent(packageName, func -> new TestSuite(packageName));
    }

    /**
     * Process the program file i.e. the executable program generated.
     *
     * @param programFile program file generated
     */
    private void processProgramFile(ProgramFile programFile) {
        // set the debugger
        Debugger debugger = new Debugger(programFile);
        TesterinaUtils.initDebugger(programFile, debugger);
        registry.addProgramFile(programFile);

        // process the compiled files
        ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
        processorServiceLoader.forEach(plugin -> {
            if (plugin instanceof TestAnnotationProcessor) {
                try {
                    ((TestAnnotationProcessor) plugin).packageProcessed(programFile);
                } catch (Exception e) {
                    errStream.println("error: validation failed. Cause: " + e.getMessage());
                    throw new BallerinaException(e);
                }
            }
        });
    }

    /**
     * Run all tests.
     * @param buildWithTests build with tests or just execute tests
     */
    private void execute(boolean buildWithTests) {
        Map<String, TestSuite> testSuites = registry.getTestSuites();
        outStream.println();
        outStream.println("Running tests");
        // Check if the test suite is empty
        if (testSuites.isEmpty()) {
            outStream.println("    No tests found");
            // We need a new line to show a clear separation between the outputs of 'Running Tests' and
            // 'Generating Executable'
            if (buildWithTests) {
                outStream.println();
            }
            return;
        }

        AtomicBoolean shouldSkip = new AtomicBoolean();
        LinkedList<String> keys = new LinkedList<>(testSuites.keySet());
        Collections.sort(keys);

        keys.forEach(packageName -> {
            TestSuite suite = testSuites.get(packageName);
            // For single bal files
            if (packageName.equals(Names.DOT.value)) {
                // If there is a source file name print it and then execute the tests
                outStream.println("    " + suite.getSourceFileName());
            } else {
                outStream.println("    " + packageName);
            }
            // Check if there are tests in the test suite
            if (suite.getTests().size() == 0) {
                outStream.println("\tNo tests found\n");
                return;
            }
            shouldSkip.set(false);
            TestAnnotationProcessor.injectMocks(suite);
            tReport.addPackageReport(packageName);
            if (suite.getInitFunction() != null && TesterinaUtils.isPackageInitialized(packageName)) {
                suite.getInitFunction().invoke();
            }

            suite.getBeforeSuiteFunctions().forEach(test -> {
                String errorMsg;
                try {
                    test.invoke();
                } catch (Throwable e) {
                    shouldSkip.set(true);
                    errorMsg = "\t[fail] " + test.getName() + " [before test suite function]" + ":\n\t    "
                            + TesterinaUtils.formatError(e.getMessage());
                    errStream.println(errorMsg);
                }
            });
            List<String> failedOrSkippedTests = new ArrayList<>();
            suite.getTests().forEach(test -> {
                AtomicBoolean shouldSkipTest = new AtomicBoolean(false);
                if (!shouldSkip.get() && !shouldSkipTest.get()) {
                    // run the beforeEach tests
                    suite.getBeforeEachFunctions().forEach(beforeEachTest -> {
                        String errorMsg;
                        try {
                            beforeEachTest.invoke();
                        } catch (Throwable e) {
                            shouldSkipTest.set(true);
                            errorMsg = String.format("\t[fail] " + beforeEachTest.getName() +
                                                     " [before each test function for the test %s] :\n\t    %s",
                                                     test.getTestFunction().getName(),
                                                     TesterinaUtils.formatError(e.getMessage()));
                            errStream.println(errorMsg);
                        }
                    });
                }
                if (!shouldSkip.get() && !shouldSkipTest.get()) {
                    // run before tests
                    String errorMsg;
                    try {
                        if (test.getBeforeTestFunctionObj() != null) {
                            test.getBeforeTestFunctionObj().invoke();
                        }
                    } catch (Throwable e) {
                        shouldSkipTest.set(true);
                        errorMsg = String.format("\t[fail] " + test.getBeforeTestFunctionObj().getName() +
                                                 " [before test function for the test %s] :\n\t    %s",
                                                 test.getTestFunction().getName(),
                                                 TesterinaUtils.formatError(e.getMessage()));
                        errStream.println(errorMsg);
                    }
                }
                // run the test
                TesterinaResult functionResult = null;
                try {
                    if (isTestDependsOnFailedFunctions(test.getDependsOnTestFunctions(), failedOrSkippedTests)) {
                      shouldSkipTest.set(true);
                    }

                    // Check whether the this test depends on any failed or skipped functions
                    if (!shouldSkip.get() && !shouldSkipTest.get()) {
                        BValue[] valueSets = null;
                        if (test.getDataProviderFunction() != null) {
                            valueSets = test.getDataProviderFunction().invoke();
                        }
                        if (valueSets == null) {
                            test.getTestFunction().invoke();
                            // report the test result
                            functionResult = new TesterinaResult(test.getTestFunction().getName(), true, shouldSkip
                                .get(), null);
                            tReport.addFunctionResult(packageName, functionResult);
                        } else {
                            List<BValue[]> argList = extractArguments(valueSets);
                            argList.forEach(arg -> {
                                test.getTestFunction().invoke(arg);
                                TesterinaResult result = new TesterinaResult(test.getTestFunction().getName(), true,
                                        shouldSkip.get(), null);
                                tReport.addFunctionResult(packageName, result);
                            });
                        }
                    } else {
                        // If the test function is skipped lets add it to the failed test list
                        failedOrSkippedTests.add(test.getTestFunction().getName());
                        // report the test result
                        functionResult = new TesterinaResult(test.getTestFunction().getName(), false, true, null);
                        tReport.addFunctionResult(packageName, functionResult);
                    }
                } catch (Throwable e) {
                    // If the test function is skipped lets add it to the failed test list
                    failedOrSkippedTests.add(test.getTestFunction().getName());
                    // report the test result
                    functionResult = new TesterinaResult(test.getTestFunction().getName(), false, shouldSkip.get(),
                                                         e.getMessage());
                    tReport.addFunctionResult(packageName, functionResult);
                }

                // run the after tests
                String error;
                try {
                    if (test.getAfterTestFunctionObj() != null) {
                        test.getAfterTestFunctionObj().invoke();
                    }
                } catch (Throwable e) {
                    error = String.format("\t[fail] " + test.getAfterTestFunctionObj().getName() +
                                          " [after test function for the test %s] :\n\t    %s",
                                          test.getTestFunction().getName(),
                                          TesterinaUtils.formatError(e.getMessage()));
                    errStream.println(error);
                }

                // run the afterEach tests
                suite.getAfterEachFunctions().forEach(afterEachTest -> {
                    String errorMsg2;
                    try {
                        afterEachTest.invoke();
                    } catch (Throwable e) {
                        errorMsg2 = String.format("\t[fail] " + afterEachTest.getName() +
                                                  " [after each test function for the test %s] :\n\t    %s",
                                                  test.getTestFunction().getName(),
                                                  TesterinaUtils.formatError(e.getMessage()));
                        errStream.println(errorMsg2);
                    }
                });
            });
            TestAnnotationProcessor.resetMocks(suite);

            // Run After suite functions
            suite.getAfterSuiteFunctions().forEach(func -> {
                String errorMsg;
                try {
                    func.invoke();
                } catch (Throwable e) {
                    errorMsg = String.format("\t[fail] " + func.getName() + " [after test suite function] :\n\t    " +
                                                     "%s", TesterinaUtils.formatError(e.getMessage()));
                    errStream.println(errorMsg);
                }
            });
            // print module test results
            tReport.printTestSuiteSummary(packageName);

            // Call module stop and test stop function
            suite.getInitFunction().invokeStopFunctions();
        });
    }

    private boolean isTestDependsOnFailedFunctions(List<String> failedOrSkippedTests, List<String> dependentTests) {
        return failedOrSkippedTests.stream().parallel().anyMatch(dependentTests::contains);
    }

    /**
     * Extract function arguments from the values sets.
     * @param valueSets user provided value sets
     * @return a list of function arguments
     */
    private List<BValue[]> extractArguments(BValue[] valueSets) {
        List<BValue[]> argsList = new ArrayList<>();

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
                        argsList.add(args);
                    } else {
                        // cannot happen due to validations done at annotation processor
                    }
                }
            } else {
                argsList.add(new BValue[]{value});
            }
        }
        return argsList;
    }

    /**
     * Return the Test report of program runner.
     *
     * @return {@link TesterinaReport} object
     */
    public TesterinaReport getTesterinaReport() {
        return tReport;
    }

}

