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

package org.ballerinalang.test.runtime;

import com.google.gson.Gson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.BooleanType;
import io.ballerina.runtime.api.types.ByteType;
import io.ballerina.runtime.api.types.DecimalType;
import io.ballerina.runtime.api.types.FloatType;
import io.ballerina.runtime.api.types.IntegerType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.StringType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.XmlType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.ObjectValue;
import io.ballerina.runtime.internal.values.XmlValue;
import org.ballerinalang.test.runtime.entity.Test;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.entity.TesterinaFunction;
import org.ballerinalang.test.runtime.entity.TesterinaReport;
import org.ballerinalang.test.runtime.entity.TesterinaResult;
import org.ballerinalang.test.runtime.exceptions.BallerinaTestException;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.test.runtime.util.TesterinaUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.ballerinalang.test.runtime.util.TesterinaConstants.DATA_KEY_SEPARATOR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;

/**
 * BTestRunner entity class.
 */
public class BTestRunner {

    public static final String MODULE_INIT_CLASS_NAME = "$_init";
    private static final String FILE_NAME_PERIOD_SEPARATOR = "$$$";
    private static final String INIT_FUNCTION_NAME = ".<init>";
    private static final String START_FUNCTION_NAME = ".<start>";
    private static final String STOP_FUNCTION_NAME = ".<stop>";
    private static final String TEST_INIT_FUNCTION_NAME = ".<testinit>";
    private static final String TEST_START_FUNCTION_NAME = ".<teststart>";
    private static final String TEST_STOP_FUNCTION_NAME = ".<teststop>";
    private static final String CONFIGURATION_CLASS_NAME = "$configurationMapper";
    private static final String CONFIG_FILE_NAME = "Config.toml";

    private PrintStream errStream;
    private PrintStream outStream;
    private TesterinaReport tReport;

    private List<String> specialCharacters = new ArrayList<>(Arrays.asList(",", "\\n", "\\r", "\\t", "\n", "\r", "\t",
            "\"", "\\", "!", "`"));
    private List<String> bracketCharacters = new ArrayList<>(Arrays.asList("{", "}", "[", "]", "(", ")"));
    private List<String> regexSpecialCharacters = new ArrayList<>(Arrays.asList("{", "}", "[", "]", "(", ")", "+",
            "^", "|"));
    /**
     * Create Test Runner with given loggers.
     *
     * @param outStream The info log stream.
     * @param errStream The error log strem.
     */
    public BTestRunner(PrintStream outStream, PrintStream errStream) {
        this.outStream = outStream;
        this.errStream = errStream;
        tReport = new TesterinaReport(this.outStream);
    }

    /**
     * Executes a given set of ballerina program files when running tests using the build command.
     *
     * @param suite test meta data for module
     */
    public void runTest(TestSuite suite, ClassLoader classLoader)  {
        int[] testExecutionOrder = checkCyclicDependencies(suite.getTests());
        List<Test> sortedTests = orderTests(suite.getTests(), testExecutionOrder);
        suite.setTests(sortedTests);
        // execute the test programs
        execute(suite, classLoader);
    }

    private static List<Test> orderTests(List<Test> tests, int[] testExecutionOrder) {
        List<Test> sortedTests = new ArrayList<>();
        for (int idx : testExecutionOrder) {
            sortedTests.add(tests.get(idx));
        }
        return sortedTests;
    }

    private static int[] checkCyclicDependencies(List<Test> tests) {
        int numberOfNodes = tests.size();
        int[] indegrees = new int[numberOfNodes];
        int[] sortedElts = new int[numberOfNodes];

        List<Integer>[] dependencyMatrix = new ArrayList[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            dependencyMatrix[i] = new ArrayList<>();
        }
        List<String> testNames = tests.stream()
                .map(Test::getTestName)
                .collect(Collectors.toList());

        int i = 0;
        for (Test test : tests) {
            if (!test.getDependsOnTestFunctions().isEmpty()) {
                for (String dependsOnFn : test.getDependsOnTestFunctions()) {
                    int idx = testNames.indexOf(dependsOnFn);
                    if (idx == -1) {
                        String message = String.format("Test [%s] depends on function [%s], but it is either " +
                                "disabled or not included.", test, dependsOnFn);
                        throw new BallerinaTestException(message);
                    }
                    dependencyMatrix[i].add(idx);
                }
            }
            i++;
        }

        // fill in degrees
        for (int j = 0; j < numberOfNodes; j++) {
            List<Integer> dependencies = dependencyMatrix[j];
            for (int node : dependencies) {
                indegrees[node]++;
            }
        }

        // Create a queue and enqueue all vertices with indegree 0
        Stack<Integer> stack = new Stack<>();
        for (i = 0; i < numberOfNodes; i++) {
            if (indegrees[i] == 0) {
                stack.add(i);
            }
        }

        // Initialize count of visited vertices
        int cnt = 0;

        // Create a vector to store result (A topological ordering of the vertices)
        Vector<Integer> topOrder = new Vector<Integer>();
        while (!stack.isEmpty()) {
            // Extract front of queue (or perform dequeue) and add it to topological order
            int u = stack.pop();
            topOrder.add(u);

            // Iterate through all its neighbouring nodes of dequeued node u and decrease their in-degree by 1
            for (int node : dependencyMatrix[u]) {
                // If in-degree becomes zero, add it to queue
                if (--indegrees[node] == 0) {
                    stack.push(node);
                }
            }
            cnt++;
        }

        // Check if there was a cycle
        if (cnt != numberOfNodes) {
            String message = "Cyclic test dependency detected";
            throw new BallerinaTestException(message);
        }

        i = numberOfNodes - 1;
        for (int elt : topOrder) {
            sortedElts[i] = elt;
            i--;
        }

        return sortedElts;
    }

    /**
     * Run all tests.
     *
     */
    private void execute(TestSuite suite, ClassLoader testClassLoader) {
        // Check if there are tests in the test suite
        if (suite.getTests().size() == 0) {
            outStream.println();
            outStream.println("\t\tNo tests found\n");
            return;
        }

        AtomicBoolean shouldSkip = new AtomicBoolean();
        AtomicBoolean shouldSkipAfterSuite = new AtomicBoolean();
        String packageName = suite.getPackageName();
        ClassLoader classLoader = testClassLoader;
        // Load module init class
        String initClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(),
                suite.getPackageID(),
                suite.getVersion(),
                MODULE_INIT_CLASS_NAME);
        Class<?> initClazz;
        try {
            initClazz = classLoader.loadClass(initClassName);
        } catch (Throwable e) {
            throw new BallerinaTestException("failed to load init class :" + initClassName);
        }
        Class<?> configClazz;
        String configClassName = TesterinaUtils
                .getQualifiedClassName(suite.getOrgName(), suite.getPackageID(), suite.getVersion(),
                        CONFIGURATION_CLASS_NAME);
        try {
            configClazz = classLoader.loadClass(configClassName);
        } catch (Throwable e) {
            throw new BallerinaTestException("failed to load configuration class :" + configClassName);
        }
        Scheduler scheduler = new Scheduler(4, false);
        Scheduler initScheduler = new Scheduler(4, false);
        Class<?> testInitClazz = null;
        // For single bal files
        boolean hasTestablePackage = !packageName.equals(TesterinaConstants.DOT);
        if (hasTestablePackage) {
            // Load test init class
            String testClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(), suite.getPackageID(),
                    suite.getVersion(), suite.getPackageID().replace(".", FILE_NAME_PERIOD_SEPARATOR));
            try {
                testInitClazz = classLoader.loadClass(testClassName);
            } catch (Throwable e) {
                throw new BallerinaTestException("failed to load Test init class :" + testClassName);
            }
        }
        // 'shouldSkip' sets to true if beforeSuite, beforeEach or afterEach functions fail
        shouldSkip.set(false);
        shouldSkipAfterSuite.set(false);
        tReport.addPackageReport(packageName);
        tReport.setReportRequired(suite.isReportRequired());
        // Initialize the test suite.
        // This will init and start the test module.
        startSuite(suite, initScheduler, initClazz, testInitClazz, configClazz, hasTestablePackage);
        // Run Before suite functions
        executeBeforeSuiteFunctions(suite, classLoader, scheduler, shouldSkip, shouldSkipAfterSuite);
        // Run Tests
        executeTests(suite, packageName, classLoader, scheduler, shouldSkip);
        // Run After suite functions
        executeAfterSuiteFunctions(suite, classLoader, scheduler, shouldSkipAfterSuite);
        // Call module stop and test stop function
        stopSuite(scheduler, initClazz, testInitClazz, hasTestablePackage);
        // print module test results
        tReport.printTestSuiteSummary(packageName);
    }

    private void startSuite(TestSuite suite, Scheduler initScheduler, Class<?> initClazz, Class<?> testInitClazz,
                            Class<?> configClazz, boolean hasTestablePackage) {
        TesterinaFunction init = new TesterinaFunction(initClazz, INIT_FUNCTION_NAME, initScheduler);
        TesterinaFunction start = new TesterinaFunction(initClazz, START_FUNCTION_NAME, initScheduler);
        TesterinaFunction configInit = new TesterinaFunction(configClazz, "$configureInit", initScheduler);
        // As the init function we need to use $moduleInit to initialize all the dependent modules
        // properly.

        Object response = configInit.directInvoke(new Class[]{String[].class, Path[].class, String.class},
                new Object[]{new String[]{}, getConfigPaths(suite), null});
        if (response instanceof Throwable) {
            throw new BallerinaTestException("Configurable initialization for test suite failed due to " +
                    formatErrorMessage((Throwable) response), (Throwable) response);
        }

        init.setName("$moduleInit");
        response = init.invoke();
        if (response instanceof Throwable) {
            throw new BallerinaTestException("Dependant module initialization for test suite failed due to " +
                    formatErrorMessage((Throwable) response), (Throwable) response);
        }
        // Now we initialize the init of testable module.
        if (hasTestablePackage) {
            TesterinaFunction testInit =
                    new TesterinaFunction(testInitClazz, TEST_INIT_FUNCTION_NAME, initScheduler);
            response = testInit.invoke();
            if (response instanceof Throwable) {
                throw new BallerinaTestException("Test module initialization for test suite failed due to " +
                        formatErrorMessage((Throwable) response), (Throwable) response);
            }
        }
        // As the start function we need to use $moduleStart to start all the dependent modules
        // properly.
        start.setName("$moduleStart");
        start.invoke();
        // Invoke start function of the testable module
        if (hasTestablePackage) {
            TesterinaFunction testStart =
                    new TesterinaFunction(testInitClazz, TEST_START_FUNCTION_NAME, initScheduler);
            response = testStart.invoke();
            if (response instanceof BError || response instanceof Throwable || response instanceof Error) {
                throw new BallerinaTestException("Test module invocation for test suite failed due to " +
                        formatErrorMessage((Throwable) response), (Throwable) response);
            }
        }
        // Once the start function finish we will re start the scheduler with immortal true
        initScheduler.setImmortal(true);
        Thread immortalThread = new Thread(initScheduler::start, "module-start");
        immortalThread.setDaemon(true);
        immortalThread.start();
    }

    private Path[] getConfigPaths(TestSuite testSuite) {
        String moduleName = testSuite.getModuleName();
        Path configFilePath = Paths.get(testSuite.getSourceRootPath());
        if (!moduleName.equals(testSuite.getPackageName())) {
            configFilePath = configFilePath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName);
        }
        configFilePath = configFilePath.resolve(ProjectConstants.TEST_DIR_NAME).resolve(CONFIG_FILE_NAME);
        if (!Files.exists(configFilePath)) {
            return new Path[] {};
        } else {
            return new Path[] {configFilePath};
        }
    }

    private void executeBeforeSuiteFunctions(TestSuite suite, ClassLoader classLoader, Scheduler scheduler,
                                             AtomicBoolean shouldSkip, AtomicBoolean shouldSkipAfterSuite) {
        suite.getBeforeSuiteFunctionNames().forEach(test -> {
            String errorMsg;
            try {
                Object value = invokeTestFunction(suite, test, classLoader, scheduler);
                if (value instanceof BError || value instanceof  Exception || value instanceof Error) {
                    throw (Throwable) value;
                }
            } catch (Throwable e) {
                shouldSkip.set(true);
                shouldSkipAfterSuite.set(true);
                errorMsg = "\t[fail] " + test + " [before test suite function]" + ":\n\t    "
                        + formatErrorMessage(e);
                errStream.println(errorMsg);
            }
        });
    }

    private void executeTests(TestSuite suite, String packageName, ClassLoader classLoader, Scheduler scheduler,
                              AtomicBoolean shouldSkip) {
        List<String> failedOrSkippedTests = new ArrayList<>();
        List<String> failedAfterFuncTests = new ArrayList<>();
        Map<String, AtomicBoolean> shouldSkipAfterGroups = new HashMap<>();
        for (String group : suite.getGroups().keySet()) {
            shouldSkipAfterGroups.put(group, new AtomicBoolean(false));
        }
        suite.getTests().forEach(test -> {
            AtomicBoolean shouldSkipTest = new AtomicBoolean(false);

            // execute the before groups functions
            executeBeforeGroupFunctions(test, suite, classLoader, scheduler, shouldSkip,
                    shouldSkipTest, shouldSkipAfterGroups);

            // run the before each tests
            executeBeforeEachFunction(test, suite, classLoader, scheduler, shouldSkip);
            // run the before tests
            executeBeforeFunction(test, suite, classLoader, scheduler, shouldSkip, shouldSkipTest);
            // run the test
            executeFunction(test, suite, packageName, classLoader, scheduler, shouldSkip, shouldSkipTest,
                    failedOrSkippedTests, failedAfterFuncTests);
            // run the after tests
            executeAfterFunction(test, suite, classLoader, scheduler, shouldSkip, shouldSkipTest, failedAfterFuncTests);
            // run the after each tests
            executeAfterEachFunction(test, suite, classLoader, scheduler, shouldSkip, shouldSkipTest);
            // execute the after groups functions
            executeAfterGroupFunctions(test, suite, classLoader, scheduler, shouldSkip, shouldSkipAfterGroups);
        });
    }

    private void executeBeforeGroupFunctions(Test test, TestSuite suite, ClassLoader classLoader, Scheduler scheduler,
                                             AtomicBoolean shouldSkip, AtomicBoolean shouldSkipTest,
                                             Map<String, AtomicBoolean> shouldSkipAfterGroups) {
        if (!shouldSkip.get()) {
            for (String groupName : test.getGroups()) {
                if (!suite.getGroups().get(groupName).getBeforeGroupsFunctions().isEmpty()
                        && !suite.getGroups().get(groupName).isFirstTestExecuted()) {
                    // run before tests
                    String errorMsg;
                    for (String beforeGroupFunc : suite.getGroups().get(groupName).getBeforeGroupsFunctions()) {
                        try {
                            Object value = invokeTestFunction(suite, beforeGroupFunc, classLoader, scheduler);
                            if (value instanceof BError || value instanceof Exception || value instanceof Error) {
                                throw (Throwable) value;
                            }
                        } catch (Throwable e) {
                            //shouldSkip.set(true);
                            shouldSkipTest.set(true);
                            shouldSkipAfterGroups.put(groupName, new AtomicBoolean(true));
                            errorMsg = String.format("\t[fail] " + beforeGroupFunc +
                                            " [before test group function for the test %s] :\n\t    %s", test,
                                    formatErrorMessage(e));
                            errStream.println(errorMsg);
                        }
                    }

                }
            }
        }
    }

    private void executeBeforeEachFunction(Test test, TestSuite suite, ClassLoader classLoader, Scheduler scheduler,
                                           AtomicBoolean shouldSkip) {
        if (!shouldSkip.get()) {
            // run the beforeEach tests
            suite.getBeforeEachFunctionNames().forEach(beforeEachTest -> {
                String errorMsg;
                try {
                    Object value = invokeTestFunction(suite, beforeEachTest, classLoader, scheduler);
                    if (value instanceof BError || value instanceof  Exception || value instanceof Error) {
                        throw (Throwable) value;
                    }
                } catch (Throwable e) {
                    shouldSkip.set(true);
                    errorMsg = String.format("\t[fail] " + beforeEachTest +
                                    " [before each test function for the test %s] :\n\t    %s",
                            test,
                            formatErrorMessage(e));
                    errStream.println(errorMsg);
                }
            });
        }
    }

    private void executeBeforeFunction(Test test, TestSuite suite, ClassLoader classLoader, Scheduler scheduler,
                                       AtomicBoolean shouldSkip, AtomicBoolean shouldSkipTest)  {
        if (!shouldSkip.get() && !shouldSkipTest.get()) {
            // run before tests
            String errorMsg;
            try {
                if (test.getBeforeTestFunction() != null) {
                    Object value = invokeTestFunction(suite, test.getBeforeTestFunction(), classLoader, scheduler);
                    if (value instanceof BError || value instanceof  Exception || value instanceof Error) {
                        throw (Throwable) value;
                    }
                }
            } catch (Throwable e) {
                shouldSkipTest.set(true);
                errorMsg = String.format("\t[fail] " + test.getBeforeTestFunction() +
                                " [before test function for the test %s] :\n\t    %s",
                        test, formatErrorMessage(e));
                errStream.println(errorMsg);
            }
        }
    }

    /**
     * Get key values from the given Map.
     *
     * @param dataMap BMap
     * @return List<String>
     */
    private List<String> getKeyValues(BMap dataMap) {
        List<String> keyValues = new ArrayList<>();
        if (((BMapType) dataMap.getType()).getConstrainedType() instanceof TupleType) {
            for (BString key : (BString[]) dataMap.getKeys()) {
                keyValues.add(key.getValue());
            }
        }
        return keyValues;
    }

    /**
     * Check if the given key is included in the provided list of cases.
     *
     * @param suite TestSuite
     * @param testName String
     * @param key String
     * @return boolean
     */
    private boolean isIncludedKey(TestSuite suite, String testName, String key) {
        boolean isIncluded = false;
        List<String> keyList = suite.getDataKeyValues().get(testName);
        for (String keyValue : keyList) {
            String pattern = encode(keyValue, regexSpecialCharacters).replace(TesterinaConstants.WILDCARD, DOT +
                    TesterinaConstants.WILDCARD);
            String decodedKey = encode(key, regexSpecialCharacters);
            if (pattern.equals(decodedKey)) {
                isIncluded = true;
            } else {
                isIncluded = Pattern.matches(pattern, decodedKey);
            }
            if (isIncluded) {
                break;
            }
        }
        return isIncluded;
    }

    private void invokeDataDrivenTest(TestSuite suite, String testName, String key, ClassLoader classLoader,
                                      Scheduler scheduler, AtomicBoolean shouldSkip, String packageName, Object[] arg,
                                      Class<?>[] argTypes, List<String> failedOrSkippedTests) {
        Object valueSets;
        if (suite.isSingleDDTExecution()) {
            if (isIncludedKey(suite, testName, key)) {
                valueSets = invokeTestFunction(suite, testName, classLoader, scheduler, argTypes, arg);
                computeFunctionResult(testName + DATA_KEY_SEPARATOR + key, packageName, shouldSkip,
                        failedOrSkippedTests, valueSets);
            }
        } else {
            valueSets = invokeTestFunction(suite, testName, classLoader, scheduler, argTypes,
                    arg);
            computeFunctionResult(testName + DATA_KEY_SEPARATOR + key,
                    packageName, shouldSkip, failedOrSkippedTests, valueSets);
        }
    }

    private void executeFunction(Test test, TestSuite suite, String packageName, ClassLoader classLoader,
                                 Scheduler scheduler, AtomicBoolean shouldSkip, AtomicBoolean shouldSkipTest,
                                 List<String> failedOrSkippedTests, List<String> failedAfterFuncTests) {
        TesterinaResult functionResult;

        if (isTestDependsOnFailedFunctions(test.getDependsOnTestFunctions(), failedOrSkippedTests) ||
                isTestDependsOnFailedFunctions(test.getDependsOnTestFunctions(), failedAfterFuncTests)) {
            shouldSkipTest.set(true);
        }

        // Check whether the this test depends on any failed or skipped functions
        if (!shouldSkip.get() && !shouldSkipTest.get()) {
            Object valueSets = null;
            if (test.getDataProvider() != null) {
                valueSets = invokeTestFunction(suite, test.getDataProvider(), classLoader, scheduler);
            }
            if (valueSets == null) {
                valueSets = invokeTestFunction(suite, test.getTestName(), classLoader, scheduler);
                computeFunctionResult(test.getTestName(), packageName, shouldSkip, failedOrSkippedTests, valueSets);
            } else {
                if (valueSets instanceof BMap) {
                    // Handle map data sets
                    if (((BMap) valueSets).isEmpty()) {
                        computeFunctionResult(test.getTestName(), packageName, shouldSkip, failedOrSkippedTests,
                                new Error("The provided data set is empty."));
                    } else {
                        List<String> keyValues = getKeyValues((BMap) valueSets);
                        Class<?>[] argTypes = extractArgumentTypes((BMap) valueSets);
                        List<Object[]> argList = extractArguments((BMap) valueSets);
                        for (int i = 0, argListSize = argList.size(); i < argListSize; i++) {
                            invokeDataDrivenTest(suite, test.getTestName(), escapeSpecialCharacters(keyValues.get(i)),
                                    classLoader, scheduler, shouldSkip, packageName, argList.get(i), argTypes,
                                    failedOrSkippedTests);
                        }
                    }
                } else if (valueSets instanceof BArray) {
                    if (((BArray) valueSets).isEmpty()) {
                        computeFunctionResult(test.getTestName(), packageName, shouldSkip, failedOrSkippedTests,
                                new Error("The provided data set is empty."));
                    } else {
                        // Handle array data sets
                        Class<?>[] argTypes = extractArgumentTypes((BArray) valueSets);
                        List<Object[]> argList = extractArguments((BArray) valueSets);
                        for (int i = 0, argListSize = argList.size(); i < argListSize; i++) {
                            invokeDataDrivenTest(suite, test.getTestName(), String.valueOf(i), classLoader, scheduler,
                                    shouldSkip, packageName, argList.get(i), argTypes, failedOrSkippedTests);
                        }
                    }
                } else if (valueSets instanceof Error || valueSets instanceof Exception) {
                    computeFunctionResult(test.getTestName(), packageName, shouldSkip, failedOrSkippedTests,
                            valueSets);
                } else {
                    computeFunctionResult(test.getTestName(), packageName, shouldSkip, failedOrSkippedTests,
                            new Error("The provided data set does not match the supported formats."));
                }
            }
        } else {
            // If the test function is skipped lets add it to the failed test list
            failedOrSkippedTests.add(test.getTestName());
            // report the test result
            functionResult = new TesterinaResult(test.getTestName(), false, true, null);
            tReport.addFunctionResult(packageName, functionResult);
        }
        for (String groupName : test.getGroups()) {
            suite.getGroups().get(groupName).incrementExecutedCount();
        }

        if (!packageName.equals(TesterinaConstants.DOT)) {
            Path sourceRootPath = Paths.get(suite.getSourceRootPath()).resolve(TesterinaConstants.TARGET_DIR_NAME);
            Path jsonPath = Paths.get(sourceRootPath.toString(), TesterinaConstants.RERUN_TEST_JSON_FILE);
            File jsonFile = new File(jsonPath.toString());
            writeFailedTestsToJson(failedOrSkippedTests, jsonFile);
        }

    }

    private void computeFunctionResult(String testName, String packageName, AtomicBoolean shouldSkip,
                                       List<String> failedOrSkippedTests, Object valueSets) {
        TesterinaResult functionResult;
        if (valueSets instanceof BError) {
            failedOrSkippedTests.add(testName);
            functionResult = new TesterinaResult(testName, false, shouldSkip.get(),
                    formatErrorMessage((BError) valueSets));
            tReport.addFunctionResult(packageName, functionResult);
        } else if (valueSets instanceof Exception) {
            failedOrSkippedTests.add(testName);
            functionResult = new TesterinaResult(testName, false, shouldSkip.get(),
                    formatErrorMessage((Exception) valueSets));
            tReport.addFunctionResult(packageName, functionResult);
        } else if (valueSets instanceof Error) {
            failedOrSkippedTests.add(testName);
            functionResult = new TesterinaResult(testName, false, shouldSkip.get(),
                    formatErrorMessage((Error) valueSets));
            tReport.addFunctionResult(packageName, functionResult);
        } else {
            functionResult = new TesterinaResult(testName, true, shouldSkip.get(), null);
            tReport.addFunctionResult(packageName, functionResult);
        }
    }

    private void executeAfterFunction(Test test, TestSuite suite, ClassLoader classLoader, Scheduler scheduler,
                                      AtomicBoolean shouldSkip, AtomicBoolean shouldSkipTest,
                                      List<String> failedAfterFuncTests)  {
        if (!shouldSkip.get() && !shouldSkipTest.get()) {
            try {
                if (test.getAfterTestFunction() != null) {
                    Object value = invokeTestFunction(suite, test.getAfterTestFunction(), classLoader, scheduler);
                    if (value instanceof BError || value instanceof  Exception || value instanceof Error) {
                        throw (Throwable) value;
                    }
                }
            } catch (Throwable e) {
                failedAfterFuncTests.add(test.getTestName());
                String error = String.format("\t[fail] " + test + " [after test function for the test %s] :\n\t    %s",
                        test, formatErrorMessage(e));
                errStream.println(error);
            }
        }
    }

    private void executeAfterEachFunction(Test test, TestSuite suite, ClassLoader classLoader, Scheduler scheduler,
                                          AtomicBoolean shouldSkip, AtomicBoolean shouldSkipTest) {
        if (!shouldSkip.get()) {
            suite.getAfterEachFunctionNames().forEach(afterEachTest -> {
                try {
                    Object value = invokeTestFunction(suite, afterEachTest, classLoader, scheduler);
                    if (value instanceof BError || value instanceof  Exception || value instanceof Error) {
                        throw (Throwable) value;
                    }
                } catch (Throwable e) {
                    shouldSkip.set(true);
                    String errorMsg = String.format("\t[fail] " + afterEachTest +
                                    " [after each test function for the test %s] :\n\t    %s",
                            test, formatErrorMessage(e));
                    errStream.println(errorMsg);
                }
            });
        }
    }

    private void executeAfterGroupFunctions(Test test, TestSuite suite, ClassLoader classLoader, Scheduler scheduler,
                                            AtomicBoolean shouldSkip,
                                            Map<String, AtomicBoolean> shouldSkipAfterGroups) {
        for (String groupName : test.getGroups()) {
            if (!suite.getGroups().get(groupName).getAfterGroupsFunctions().isEmpty()
                    && suite.getGroups().get(groupName).isLastTestExecuted()) {
                // run before tests
                suite.getGroups().get(groupName).getAfterGroupsFunctions().forEach((afterGroupFunc, alwaysRun) -> {
                    if (!(shouldSkipAfterGroups.get(groupName).get() || shouldSkip.get()) || alwaysRun.get()) {
                        try {
                            Object value = invokeTestFunction(suite, afterGroupFunc, classLoader, scheduler);
                            if (value instanceof BError || value instanceof Exception || value instanceof Error) {
                                throw (Throwable) value;
                            }
                        } catch (Throwable e) {
                            String errorMsg = String.format("\t[fail] " + afterGroupFunc +
                                            " [after test group function for the test %s] :\n\t    %s", test,
                                    formatErrorMessage(e));
                            errStream.println(errorMsg);
                        }
                    }
                });
            }
        }
    }

    private void executeAfterSuiteFunctions(TestSuite suite, ClassLoader classLoader, Scheduler scheduler,
                                            AtomicBoolean shouldSkipAfterSuite) {
        suite.getAfterSuiteFunctionNames().forEach((func, alwaysRun) -> {
            if (!shouldSkipAfterSuite.get() || alwaysRun.get()) {
                String errorMsg;
                try {
                    Object value = invokeTestFunction(suite, func, classLoader, scheduler);
                    if (value instanceof BError || value instanceof  Exception || value instanceof Error) {
                        throw (Throwable) value;
                    }
                } catch (Throwable e) {
                    errorMsg = String.format("\t[fail] " + func + " [after test suite function] :\n\t    " +
                            "%s", formatErrorMessage(e));
                    errStream.println(errorMsg);
                }
            }
        });
    }

    private void stopSuite(Scheduler scheduler, Class<?> initClazz, Class<?> testInitClazz,
                           boolean hasTestablePackage) {
        TesterinaFunction stop = new TesterinaFunction(initClazz, STOP_FUNCTION_NAME, scheduler);
        // Invoke stop function of the testable module.
        if (hasTestablePackage) {
            TesterinaFunction testStop =
                    new TesterinaFunction(testInitClazz, TEST_STOP_FUNCTION_NAME, scheduler);
            testStop.scheduler = scheduler;
            testStop.invoke();
        }
        stop.setName("$moduleStop");
        stop.directInvoke(new Class<?>[]{Scheduler.ListenerRegistry.class},
                new Object[]{scheduler.getListenerRegistry()});
    }

    private Object invokeTestFunction(TestSuite suite, String functionName, ClassLoader classLoader,
                                      Scheduler scheduler) {
        try {
            Class<?> functionClass = classLoader.loadClass(suite.getTestUtilityFunctions().get(functionName));
            TesterinaFunction testerinaFunction = new TesterinaFunction(functionClass, functionName, scheduler);
            return testerinaFunction.invoke();
        } catch (ClassNotFoundException e) {
            return e;
        }
    }

    public Object invokeTestFunction(TestSuite suite, String functionName, ClassLoader classLoader,
                                     Scheduler scheduler, Class<?>[] types, Object[] args) {
        try {
            Class<?> functionClass = classLoader.loadClass(suite.getTestUtilityFunctions().get(functionName));
            TesterinaFunction testerinaFunction = new TesterinaFunction(functionClass, functionName, scheduler);
            return testerinaFunction.invoke(types, args);
        } catch (ClassNotFoundException e) {
            return e;
        }
    }

    private String formatErrorMessage(Throwable e) {
        try {
            if (e instanceof BError) {
                return ((BError) e).getPrintableStackTrace();
            } else if (e instanceof Exception | e instanceof Error) {
                return TesterinaUtils.getPrintableStackTrace(e);
            } else {
                return TesterinaUtils.getPrintableStackTrace(e);
            }
        } catch (ClassCastException classCastException) {
            // If an unhandled error type is passed to format error message
            return TesterinaUtils.getPrintableStackTrace(e);
        }
    }

    private boolean isTestDependsOnFailedFunctions(List<String> failedOrSkippedTests, List<String> dependentTests) {
        return failedOrSkippedTests.stream().parallel().anyMatch(dependentTests::contains);
    }

    /**
     * Extract function arguments from the data sets.
     *
     * @param bArray user provided array data sets
     * @return a list of function arguments
     */
    private List<Object[]> extractArguments(BArray bArray) {
        List<Object[]> argsList = new ArrayList<>();
        if (bArray.getElementType() instanceof ArrayType) {
            // Ok we have an array of an array
            for (int i = 0; i < bArray.size(); i++) {
                // Iterate array elements and set parameters
                setTestFunctionParams(argsList, (BArray) bArray.get(i));
            }
        } else {
            // Iterate array elements and set parameters
            setTestFunctionParams(argsList, bArray);
        }
        return argsList;
    }

    /**
     * Extract function arguments from the data sets.
     *
     * @param dataMap user provided map data sets
     * @return a list of function arguments
     */
    private List<Object[]> extractArguments(BMap dataMap) {
        List<Object[]> argsList = new ArrayList<>();
        if (((BMapType) dataMap.getType()).getConstrainedType() instanceof TupleType) {
            for (BString keyValue : (BString[]) dataMap.getKeys()) {
                setTestFunctionParams(argsList, dataMap.getArrayValue(keyValue));
            }
        }
        return argsList;
    }

    /**
     * Extract the argument types from a data set.
     *
     * @param bArray user provided array data sets
     * @return a list of class types
     */
    private static Class<?>[] extractArgumentTypes(BArray bArray) {
        List<Class<?>> typeList = new ArrayList<>();
        typeList.add(Strand.class);
        if (bArray.getElementType() instanceof ArrayType) {
            // Iterate elements of first entry in array of array
            // to get the class types
            setTestFunctionSignature(typeList, (BArray) bArray.get(0));
        } else {
            // Iterate elements and get class types
            setTestFunctionSignature(typeList, bArray);
        }
        Class<?>[] typeListArray = new Class[typeList.size()];
        typeList.toArray(typeListArray);
        return typeListArray;
    }

    /**
     * Extract the argument types from a data set.
     *
     * @param dataMap use provided map data sets
     * @return a list of class types
     */
    private static Class<?>[] extractArgumentTypes(BMap dataMap) {
        List<Class<?>> typeList = new ArrayList<>();
        typeList.add(Strand.class);
        if (((BMapType) dataMap.getType()).getConstrainedType() instanceof TupleType) {
            setTestFunctionSignature(typeList, dataMap.getArrayValue(
                    (BString) dataMap.getKeys()[0]));
        }
        Class<?>[] typeListArray = new Class[typeList.size()];
        typeList.toArray(typeListArray);
        return typeListArray;
    }

    private static void setTestFunctionSignature(List<Class<?>> typeList, BArray bArray) {
        if (bArray.getType() instanceof BTupleType) {
            List<Type> types = ((BTupleType) bArray.getType()).getTupleTypes();
            for (Type type : types) {
                Class<?> classMapping = getArgTypeToClassMapping(type);
                typeList.add(classMapping);
                typeList.add(Boolean.TYPE);
            }
        } else {
            Class<?> type = getArgTypeToClassMapping(bArray.getElementType());
            for (int i = 0; i < bArray.size(); i++) {
                // Add the param type.
                typeList.add(type);
                // This is in jvm function signature to denote if args is passed or not.
                typeList.add(Boolean.TYPE);
            }
        }
    }

    private static void setTestFunctionParams(List<Object[]> valueList, BArray bArray) {
        List<Object> params = new ArrayList<>();
        // Add a place holder to Strand
        params.add(new Object());
        if (bArray.getType() instanceof BTupleType) {
            for (int i = 0; i < bArray.size(); i++) {
                // Add the param type.
                params.add(bArray.getRefValue(i));
                // This is in jvm function signature to denote if args is passed or not.
                params.add(Boolean.TRUE);
            }
            valueList.add(params.toArray());
        } else {
            for (int i = 0; i < bArray.size(); i++) {
                // Add the param type.
                params.add(bArray.get(i));
                // This is in jvm function signature to denote if args is passed or not.
                params.add(Boolean.TRUE);
            }
            valueList.add(params.toArray());
        }
    }

    private static Class<?> getArgTypeToClassMapping(Type elementType) {
        Class<?> type;
        // Refer jvm_method_gen.bal getArgTypeSignature for proper type matching
        if (elementType instanceof StringType) {
            type = BString.class;
        } else if (elementType instanceof IntegerType) {
            type = Long.TYPE;
        } else if (elementType instanceof BooleanType) {
            type = Boolean.TYPE;
        } else if (elementType instanceof DecimalType) {
            type = DecimalValue.class;
        } else if (elementType instanceof ByteType) {
            type = Integer.TYPE;
        } else if (elementType instanceof ArrayType || elementType instanceof TupleType) {
            type = ArrayValue.class;
        } else if (elementType instanceof FloatType) {
            type = Double.TYPE;
        } else if (elementType instanceof MapType || elementType instanceof RecordType) {
            type = MapValue.class;
        } else if (elementType instanceof XmlType) {
            type = XmlValue.class;
        } else if (elementType instanceof ObjectType) {
            type = ObjectValue.class;
        } else {
            // default case
            type = Object.class;
        }
        return type;
    }

    /**
     * Return the Test report of program runner.
     *
     * @return {@link TesterinaReport} object
     */
    public TesterinaReport getTesterinaReport() {
        return tReport;
    }

    /**
     * Store the failed tests as an array in the JSON cache.
     * @param failedTests List of failed tests
     * @param jsonFile File to save failed tests
     */
    private void writeFailedTestsToJson(List<String> failedTests, File jsonFile) {
        String errorMsg;

        try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
            try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                String json = gson.toJson(failedTests);
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            } catch (IOException e) {
                errorMsg = "Could not write to Rerun Test json. Rerunning tests will not work";
                errStream.println(errorMsg + ":" + e.getMessage());
            }
        } catch (IOException e) {
            errorMsg = "Could not write to Rerun Test json. Rerunning tests will not work";
            errStream.println(errorMsg + ":" + e.getMessage());
        }
    }

    /**
     * Encode the provided set of special characters in the given key.
     *
     * @param key String
     * @param specialCharacters List<String>
     * @return String
     */
    private String encode(String key, List<String> specialCharacters) {
        String encodedKey = key;
        String encodedValue;
        for (String character : specialCharacters) {
            try {
                if (encodedKey.contains(character)) {
                    encodedValue = URLEncoder.encode(character, StandardCharsets.UTF_8.toString());
                    encodedKey = encodedKey.replace(character, encodedValue);
                }
            } catch (UnsupportedEncodingException e) {
                errStream.println("Error occurred while encoding special characters in the data provider case value '"
                        + key + "'");
            }
        }
        return encodedKey;
    }

    /**
     * Escape special characters in the given key.
     *
     * @param key String
     * @return String
     */
    private String escapeSpecialCharacters(String key) {
        String updatedKey = key;
        if (!isBalanced(updatedKey)) {
            updatedKey = encode(updatedKey, bracketCharacters);
        }
        updatedKey = encode(updatedKey, specialCharacters);
        if (!(updatedKey.startsWith("'") && updatedKey.endsWith("'"))) {
            updatedKey = "'" + updatedKey + "'";
        }
        return updatedKey;
    }

    /**
     * Check if the brackets are balanced in given expression.
     *
     * @param expr String
     * @return boolean
     */
    private boolean isBalanced(String expr) {
        Deque<Character> stack = new ArrayDeque<>();
        for (int i = 0; i < expr.length(); i++) {
            char val = expr.charAt(i);
            if (val == '(' || val == '[' || val == '{') {
                stack.push(val);
                continue;
            }
            if ((val == ')' || val == ']' || val == '}')) {
                if (stack.isEmpty()) {
                    return false;
                }
                char topElement;
                switch (val) {
                    case ')':
                        topElement = stack.pop();
                        if (topElement == '{' || topElement == '[') {
                            return false;
                        }
                        break;

                    case '}':
                        topElement = stack.pop();
                        if (topElement == '(' || topElement == '[') {
                            return false;
                        }
                        break;

                    case ']':
                        topElement = stack.pop();
                        if (topElement == '(' || topElement == '{') {
                            return false;
                        }
                        break;
                }
            } else {
                continue;
            }
        }
        // If the brackets are balanced, stack needs to be empty.
        return stack.isEmpty();
    }
}
