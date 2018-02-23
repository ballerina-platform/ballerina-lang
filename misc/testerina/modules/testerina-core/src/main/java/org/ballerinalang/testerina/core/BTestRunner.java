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
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.testerina.core.entity.TesterinaContext;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.testerina.core.entity.TesterinaReport;
import org.ballerinalang.testerina.core.entity.TesterinaResult;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

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
        ProgramFile[] programFiles = Arrays.stream(sourceFilePaths).map(BTestRunner::buildTestModel)
                .toArray(ProgramFile[]::new);
        Arrays.stream(programFiles)
                .forEachOrdered(programFile -> TesterinaRegistry.getInstance().addProgramFile(programFile));

        executeTestFunctions(programFiles, groups, ignoreGroups);
        tReport.printTestSummary();
    }

    private static ProgramFile buildTestModel(Path sourceFilePath) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, programDirPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");

        // compile
        Compiler compiler = Compiler.getInstance(context);
        compiler.compile(sourceFilePath.toString());
        org.wso2.ballerinalang.programfile.ProgramFile programFile = compiler.getCompiledProgram();

        if (programFile == null) {
            throw new BallerinaException("compilation contains errors");
        }
        ProgramFile progFile = LauncherUtils.getExecutableProgram(programFile);
        progFile.setProgramFilePath(sourceFilePath);
        return progFile;
    }

    private void executeTestFunctions(ProgramFile[] programFiles, List<String> groups, boolean excludeGroups) {
        TesterinaContext tFile = new TesterinaContext(programFiles, groups, excludeGroups);
        ArrayList<TesterinaFunction> testFunctions = tFile.getTestFunctions();
        ArrayList<TesterinaFunction> beforeTestFunctions = tFile.getBeforeTestFunctions();
        ArrayList<TesterinaFunction> afterTestFunctions = tFile.getAfterTestFunctions();

        if (testFunctions.isEmpty()) {
            throw new BallerinaException("No test functions found in the provided ballerina files.");
        }

        //before test
        for (TesterinaFunction tFunction : beforeTestFunctions) {
            try {
                tFunction.invoke();
            } catch (BallerinaException e) {
                outStream.println(
                        "error in '" + tFunction.getName() + "': " + e.getMessage());
            }
        }

        // Executing Tests
        for (TesterinaFunction tFunction : testFunctions) {

            boolean isTestPassed = true;
            // Check whether the function is disabled
            if (!tFunction.getRunTest()) {
                tReport.incrementSkipCount();
                continue;
            }

            // check whether value set is present and execute tests
            List<String[]> valueSets = tFunction.getValueSet();
            String errorMessage = "";
            if (tFunction.getValueSet().size() > 0) {
                for (String[] valueSet : valueSets) {
                    isTestPassed = true;
                    try {
                        // Parsing the args to invoke function
                        tFunction.invoke(Arrays.stream(valueSet).map(s -> new BString(s)).toArray(BString[]::new));
                    } catch (BallerinaException e) {
                        isTestPassed = false;
                        errorMessage = "valueSet: " + Arrays.toString(valueSet) + " " + e.getMessage();
                        outStream.println("test '" + tFunction.getName() + "' failed: " + errorMessage);
                    } catch (Exception e) {
                        isTestPassed = false;
                        errorMessage = "valueSet: " + Arrays.toString(valueSet) + " " + e.getMessage();
                        outStream.println("test '" + tFunction.getName() + "' has an error: " + errorMessage);
                    }
                    // if there are no exception thrown, test is passed
                    TesterinaResult functionResult = new TesterinaResult(tFunction.getName(), isTestPassed,
                            errorMessage);
                    tReport.addFunctionResult(functionResult);
                }
            } else {
                try {
                    tFunction.invoke();
                } catch (BallerinaException e) {
                    isTestPassed = false;
                    errorMessage = e.getMessage();
                    outStream.println("test '" + tFunction.getName() + "' failed: " + errorMessage);
                } catch (Exception e) {
                    isTestPassed = false;
                    errorMessage = e.getMessage();
                    outStream.println("test '" + tFunction.getName() + "' has an error: " + errorMessage);
                }
                // if there are no exception thrown, test is passed
                TesterinaResult functionResult = new TesterinaResult(tFunction.getName(), isTestPassed,
                        errorMessage);
                tReport.addFunctionResult(functionResult);
            }
        }

        //after test
        for (TesterinaFunction tFunction : afterTestFunctions) {
            try {
                tFunction.invoke();
            } catch (Exception e) {
                outStream.println("error in '" + tFunction.getName() + "': " + e.getMessage());
            }
        }
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
