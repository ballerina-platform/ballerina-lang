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

package org.ballerinalang.test.launcher;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BBooleanType;
import org.ballerinalang.jvm.types.BByteType;
import org.ballerinalang.jvm.types.BDecimalType;
import org.ballerinalang.jvm.types.BFloatType;
import org.ballerinalang.jvm.types.BIntegerType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStringType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BXMLType;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.test.launcher.entity.Test;
import org.ballerinalang.test.launcher.entity.TestSuite;
import org.ballerinalang.test.launcher.entity.TesterinaFunction;
import org.ballerinalang.test.launcher.entity.TesterinaReport;
import org.ballerinalang.test.launcher.entity.TesterinaResult;
import org.ballerinalang.test.launcher.util.TesterinaConstants;
import org.ballerinalang.test.launcher.util.TesterinaUtils;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * BTestRunner entity class.
 */
public class BTestRunner {

    public static final String MODULE_INIT_CLASS_NAME = "___init";

    private PrintStream errStream;
    private PrintStream outStream;
    private TesterinaReport tReport;
    /**
     * Create Test Runner instance.
     */
    public BTestRunner() {
        this(System.out, System.err);
    }

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

    public void runTest(String sourceRoot, Path[] sourceFilePaths, List<String> groups) {
        runTest(sourceRoot, sourceFilePaths, groups, Boolean.TRUE);
    }

    /**
     * Executes a given set of ballerina program files.
     *
     * @param sourceRoot        source root
     * @param sourceFilePaths   List of @{@link Path} of ballerina files
     * @param groups            List of groups to be included
     * @param enableExpFeatures Flag indicating to enable the experimental features
     */
    public void runTest(String sourceRoot, Path[] sourceFilePaths, List<String> groups, boolean enableExpFeatures) {
    // Todo: Remove after migrating testerina tests
    }

    /**
     * Executes a given set of ballerina program files when running tests using the test command.
     *
     * @param sourceRoot          source root
     * @param sourceFilePaths     List of @{@link Path} of ballerina files
     * @param groups              List of groups to be included/excluded
     * @param shouldIncludeGroups flag to specify whether to include or exclude provided groups
     * @param enableExpFeatures   Flag indicating to enable the experimental features
     */
    public void runTest(String sourceRoot, Path[] sourceFilePaths, List<String> groups, boolean shouldIncludeGroups,
                        boolean enableExpFeatures) {
        // Todo: Remove after migrating testerina tests
    }

    /**
     * Executes a given set of ballerina program files when running tests using the build command.
     *
     * @param suite test meta data for module
     */
    public void runTest(TestSuite suite) throws ClassNotFoundException {
        // validate test suite
        validateTestSuite(suite);
        int[] testExecutionOrder = checkCyclicDependencies(suite.getTests());
        List<Test> sortedTests = orderTests(suite.getTests(), testExecutionOrder);
        suite.setTests(sortedTests);
        // execute the test programs
        execute(suite);
    }

    /**
     * lists the groups available in tests.
     *
     * @param testSuite testSuite
     */
    public void listGroups(TestSuite testSuite) {
        List<String> groupList = getGroupList(testSuite);
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
     * @param testSuite testSuite
     * @return a list of groups
     */
    public List<String> getGroupList(TestSuite testSuite) {
        List<String> groupList = new ArrayList<>();
        for (Test test : testSuite.getTests()) {
            if (test.getGroups().size() > 0) {
                groupList.addAll(test.getGroups());
            }
        }
        return groupList.stream().distinct().collect(Collectors.toList());
    }

    private static List<Test> orderTests(List<Test> tests, int[] testExecutionOrder) {
        List<Test> sortedTests = new ArrayList<>();
        for (int idx : testExecutionOrder) {
            sortedTests.add(tests.get(idx));
        }
        return sortedTests;
    }

    /**
     * Resolve function names to {@link TesterinaFunction}s.
     *
     * @param suite {@link TestSuite} whose functions to be resolved.
     */
    private static void validateTestSuite(TestSuite suite) {
        Set<String> functionNames = suite.getTestFunctionNames().keySet();
        for (Test test : suite.getTests()) {
            if (test.getBeforeTestFunction() != null) {
                if (!functionNames.contains(test.getBeforeTestFunction())) {
                    String msg = String.format("Cannot find the specified before function : [%s] for testerina " +
                            "function : [%s]", test.getBeforeTestFunction(), test.getTestName());
                    throw new BallerinaTestException(msg);
                }
            }
            if (test.getAfterTestFunction() != null) {
                if (!functionNames.contains(test.getAfterTestFunction())) {
                    String msg = String.format("Cannot find the specified after function : [%s] for testerina " +
                            "function : [%s]", test.getBeforeTestFunction(), test.getTestName());
                    throw new BallerinaTestException(msg);
                }
            }

            if (test.getDataProvider() != null && !functionNames.contains(test.getDataProvider())) {
                String dataProvider = test.getDataProvider();
                String message = String.format("Data provider function [%s] cannot be found.", dataProvider);
                throw new BallerinaTestException(message);
            }

            for (String dependsOnFn : test.getDependsOnTestFunctions()) {
                if (functionNames.stream().noneMatch(func -> func.equals(dependsOnFn))) {
                    throw new BallerinaTestException("Cannot find the specified dependsOn function : "
                            + dependsOnFn);
                }
            }
        }
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
                        String message = String.format("Test [%s] depends on function [%s], but it couldn't be found" +
                                ".", test, dependsOnFn);
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
        Queue<Integer> q = new LinkedList<Integer>();
        for (i = 0; i < numberOfNodes; i++) {
            if (indegrees[i] == 0) {
                q.add(i);
            }
        }

        // Initialize count of visited vertices
        int cnt = 0;

        // Create a vector to store result (A topological ordering of the vertices)
        Vector<Integer> topOrder = new Vector<Integer>();
        while (!q.isEmpty()) {
            // Extract front of queue (or perform dequeue) and add it to topological order
            int u = q.poll();
            topOrder.add(u);

            // Iterate through all its neighbouring nodes of dequeued node u and decrease their in-degree by 1
            for (int node : dependencyMatrix[u]) {
                // If in-degree becomes zero, add it to queue
                if (--indegrees[node] == 0) {
                    q.add(node);
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
    private void execute(TestSuite suite) throws ClassNotFoundException {
        AtomicBoolean shouldSkip = new AtomicBoolean();
        String packageName = suite.getPackageName();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        // Load module init class
        String initClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(),
                                                                    suite.getPackageID(),
                                                                    MODULE_INIT_CLASS_NAME);
        Class<?> initClazz = classLoader.loadClass(initClassName);

        // Load test init class
        String testClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(),
                                                                    suite.getPackageID(),
                                                                    suite.getPackageID());
        Class<?> testInitClazz = classLoader.loadClass(testClassName);

        Scheduler scheduler = new Scheduler(4, false);
        Scheduler initScheduler = new Scheduler(4, false);

        // For single bal files
        if (packageName.equals(TesterinaConstants.DOT)) {
            // If there is a source file name print it and then execute the tests
            outStream.println("\t" + suite.getSourceFileName());
        } else {
            outStream.println("\t" + packageName);
        }
        // Check if there are tests in the test suite
        if (suite.getTests().size() == 0) {
            outStream.println("\tNo tests found\n");
            return;
        }
        shouldSkip.set(false);
        tReport.addPackageReport(packageName);

        // Initialize the test suite.
        // This will init and start the test module.
        startSuite(suite, initScheduler, initClazz, testInitClazz);
        suite.getBeforeSuiteFunctionNames().forEach(test -> {
            String errorMsg;
            try {
                System.out.println(test);
                invokeTestFunction(suite, test, classLoader, scheduler);
            } catch (Throwable e) {
                shouldSkip.set(true);
                errorMsg = "\t[fail] " + test + " [before test suite function]" + ":\n\t    "
                        + formatErrorMessage(e);
                errStream.println(errorMsg);
            }
        });
        List<String> failedOrSkippedTests = new ArrayList<>();
        suite.getTests().forEach(test -> {
            AtomicBoolean shouldSkipTest = new AtomicBoolean(false);
            if (!shouldSkip.get() && !shouldSkipTest.get()) {
                // run the beforeEach tests
                suite.getBeforeEachFunctionNames().forEach(beforeEachTest -> {
                    String errorMsg;
                    try {
                        invokeTestFunction(suite, beforeEachTest, classLoader, scheduler);
                    } catch (Throwable e) {
                        shouldSkipTest.set(true);
                        errorMsg = String.format("\t[fail] " + beforeEachTest +
                                                         " [before each test function for the test %s] :\n\t    %s",
                                                 test,
                                                 formatErrorMessage(e));
                        errStream.println(errorMsg);
                    }
                });
            }
            if (!shouldSkip.get() && !shouldSkipTest.get()) {
                // run before tests
                String errorMsg;
                try {
                    if (test.getBeforeTestFunction() != null) {
                        invokeTestFunction(suite, test.getBeforeTestFunction(), classLoader, scheduler);
                    }
                } catch (Throwable e) {
                    shouldSkipTest.set(true);
                    errorMsg = String.format("\t[fail] " + test.getBeforeTestFunction() +
                                                     " [before test function for the test %s] :\n\t    %s",
                                             test, formatErrorMessage(e));
                    errStream.println(errorMsg);
                }
            }
            // run the test
            TesterinaResult functionResult;
            try {
                if (isTestDependsOnFailedFunctions(test.getDependsOnTestFunctions(), failedOrSkippedTests)) {
                    shouldSkipTest.set(true);
                }

                // Check whether the this test depends on any failed or skipped functions
                if (!shouldSkip.get() && !shouldSkipTest.get()) {
                    Object valueSets = null;
                    if (test.getDataProvider() != null) {
                        valueSets = invokeTestFunction(suite, test.getDataProvider(), classLoader, scheduler);
                    }
                    if (valueSets == null) {
                        invokeTestFunction(suite, test.getTestName(), classLoader, scheduler);
                        // report the test result
                        functionResult = new TesterinaResult(test.getTestName(), true, shouldSkip
                                .get(), null);
                        tReport.addFunctionResult(packageName, functionResult);
                    } else {
                        Class<?>[] argTypes = extractArgumentTypes(valueSets);
                        List<Object[]> argList = extractArguments(valueSets);
                        for (Object[] arg : argList) {
                            invokeTestFunction(suite, test.getTestName(), classLoader, scheduler, argTypes, arg);
                            TesterinaResult result = new TesterinaResult(test.getTestName(), true,
                                                                         shouldSkip.get(), null);
                            tReport.addFunctionResult(packageName, result);
                        }
                    }
                } else {
                    // If the test function is skipped lets add it to the failed test list
                    failedOrSkippedTests.add(test.getTestName());
                    // report the test result
                    functionResult = new TesterinaResult(test.getTestName(), false, true, null);
                    tReport.addFunctionResult(packageName, functionResult);
                }
            } catch (Throwable e) {
                // If the test function is skipped lets add it to the failed test list
                failedOrSkippedTests.add(test.getTestName());
                // report the test result
                functionResult = new TesterinaResult(test.getTestName(), false, shouldSkip.get(),
                                                     formatErrorMessage(e));
                tReport.addFunctionResult(packageName, functionResult);
            }

            // run the after tests
            String error;
            try {
                if (test.getAfterTestFunction() != null) {
                    invokeTestFunction(suite, test.getAfterTestFunction(), classLoader, scheduler);
                }
            } catch (Throwable e) {
                error = String.format("\t[fail] " + test + " [after test function for the test %s] :\n\t    %s",
                                      test, formatErrorMessage(e));
                errStream.println(error);
            }

            // run the afterEach tests
            suite.getAfterEachFunctionNames().forEach(afterEachTest -> {
                String errorMsg2;
                try {
                    invokeTestFunction(suite, afterEachTest, classLoader, scheduler);
                } catch (Throwable e) {
                    errorMsg2 = String.format("\t[fail] " + afterEachTest +
                                                      " [after each test function for the test %s] :\n\t    %s",
                                              test, formatErrorMessage(e));
                    errStream.println(errorMsg2);
                }
            });
        });

        // Run After suite functions
        suite.getAfterSuiteFunctionNames().forEach(func -> {
            String errorMsg;
            try {
                invokeTestFunction(suite, func, classLoader, scheduler);
            } catch (Throwable e) {
                errorMsg = String.format("\t[fail] " + func + " [after test suite function] :\n\t    " +
                                                 "%s", formatErrorMessage(e));
                errStream.println(errorMsg);
            }
        });
        // Call module stop and test stop function
        stopSuite(suite, scheduler, initClazz, testInitClazz);

        // print module test results
        tReport.printTestSuiteSummary(packageName);
    }

    private void startSuite(TestSuite suite, Scheduler initScheduler, Class<?> initClazz, Class<?> testInitClazz) {

        TesterinaFunction initFunction = new TesterinaFunction(initClazz, suite.getInitFunctionName(),
                                                               initScheduler);
        TesterinaFunction startFunction = new TesterinaFunction(initClazz, suite.getStartFunctionName(),
                                                                initScheduler);
        TesterinaFunction testInitFunction = new TesterinaFunction(testInitClazz, suite.getTestInitFunctionName(),
                                                                   initScheduler);
        TesterinaFunction testStartFunction = new TesterinaFunction(testInitClazz, suite.getTestStartFunctionName(),
                                                                    initScheduler);

        // As the init function we need to use $moduleInit to initialize all the dependent modules
        // properly.
        initFunction.setName("$moduleInit");
        initFunction.invoke();
        // Now we initialize the init of testable module.
        testInitFunction.invoke();
        // As the start function we need to use $moduleStart to start all the dependent modules
        // properly.
        startFunction.setName("$moduleStart");
        startFunction.invoke();

        // Invoke start function of the testable module
        testStartFunction.invoke();

        // Once the start function finish we will re start the scheduler with immortal true
        initScheduler.immortal = true;
        Thread immortalThread = new Thread(initScheduler::start, "module-start");
        immortalThread.setDaemon(true);
        immortalThread.start();
}

    private void stopSuite(TestSuite suite, Scheduler scheduler, Class<?> initClazz, Class<?> testInitClazz) {
        TesterinaFunction stopFunction = new TesterinaFunction(initClazz, suite.getStopFunctionName(), scheduler);
        TesterinaFunction testStopFunction = new TesterinaFunction(testInitClazz, suite.getTestStopFunctionName(),
                                                                   scheduler);
        // Invoke stop function of the testable module.
        testStopFunction.scheduler = scheduler;
        testStopFunction.invoke();
        stopFunction.setName("$moduleStop");
        stopFunction.directInvoke(new Class<?>[]{});
    }

    private Object invokeTestFunction(TestSuite suite, String functionName, ClassLoader classLoader,
                                      Scheduler scheduler) throws ClassNotFoundException {
        Class<?> functionClass = classLoader.loadClass(suite.getTestFunctionNames().get(functionName));
        TesterinaFunction testerinaFunction = new TesterinaFunction(functionClass, functionName, scheduler);
        return testerinaFunction.invoke();
    }

    public void invokeTestFunction(TestSuite suite, String functionName, ClassLoader classLoader,
                                   Scheduler scheduler, Class<?>[] types, Object[] args) throws ClassNotFoundException {
        Class<?> functionClass = classLoader.loadClass(suite.getTestFunctionNames().get(functionName));
        TesterinaFunction testerinaFunction = new TesterinaFunction(functionClass, functionName, scheduler);
        testerinaFunction.invoke(types, args);
    }

    private String formatErrorMessage(Throwable e) {
        String message;
        if (e.getCause() instanceof ErrorValue) {
            try {
                message = ((ErrorValue) e.getCause()).getPrintableStackTrace();
            } catch (ClassCastException castException) {
                // throw the exception to top
                throw new BallerinaException(e);
            }
        } else {
            throw new BallerinaException(e);
        }
        return message;
    }

    private boolean isTestDependsOnFailedFunctions(List<String> failedOrSkippedTests, List<String> dependentTests) {
        return failedOrSkippedTests.stream().parallel().anyMatch(dependentTests::contains);
    }

    /**
     * Extract function arguments from the values sets.
     *
     * @param valueSets user provided value sets
     * @return a list of function arguments
     */
    private List<Object[]> extractArguments(Object valueSets) {
        List<Object[]> argsList = new ArrayList<>();

        if (valueSets instanceof ArrayValue) {
            ArrayValue arrayValue = (ArrayValue) valueSets;
            if (arrayValue.getElementType() instanceof BArrayType) {
                // Ok we have an array of an array
                for (int i = 0; i < arrayValue.size(); i++) {
                    // Iterate array elements and set parameters
                    setTestFunctionParams(argsList, (ArrayValue) arrayValue.get(i));
                }
            } else {
                // Iterate array elements and set parameters
                setTestFunctionParams(argsList, arrayValue);
            }
        }
        return argsList;
    }

    /**
     * Extract the parameter types from a valueset.
     * @param valueSets use provided value sets
     * @return a list of calss types.
     */
    private static Class<?>[] extractArgumentTypes(Object valueSets) {
        List<Class<?>> typeList = new ArrayList<>();
        typeList.add(Strand.class);
        if (valueSets instanceof ArrayValue) {
            ArrayValue arrayValue = (ArrayValue) valueSets;
            if (arrayValue.getElementType() instanceof BArrayType) {
                // Ok we have an array of an array
                // Get the first entry
                // Iterate elements and get class types.
                setTestFunctionSignature(typeList, (ArrayValue) arrayValue.get(0));
            } else {
                // Iterate elements and get class types.
                setTestFunctionSignature(typeList, arrayValue);
            }
        }
        Class<?>[] typeListArray = new Class[typeList.size()];
        typeList.toArray(typeListArray);
        return typeListArray;
    }

    private static void setTestFunctionSignature(List<Class<?>> typeList, ArrayValue arrayValue) {
        Class<?> type = getArgTypeToClassMapping(arrayValue.getElementType());
        for (int i = 0; i < arrayValue.size(); i++) {
            // Add the param type.
            typeList.add(type);
            // This is in jvm function signature to tel if args is passed or not.
            typeList.add(Boolean.TYPE);
        }
    }

    private static void setTestFunctionParams(List<Object[]> valueList, ArrayValue arrayValue) {
        List<Object> params = new ArrayList<>();
        // Add a place holder to Strand
        params.add(new Object());
        for (int i = 0; i < arrayValue.size(); i++) {
            // Add the param type.
            params.add(arrayValue.get(i));
            // This is in jvm function signature to tel if args is passed or not.
            params.add(Boolean.TRUE);
        }
        valueList.add(params.toArray());
    }

    private static Class<?> getArgTypeToClassMapping(BType elementType) {
        Class<?> type;
        // Refer jvm_method_gen.bal getArgTypeSignature for proper type matching
        if (elementType instanceof BStringType) {
            type = String.class;
        } else if (elementType instanceof BIntegerType) {
            type = Long.TYPE;
        } else if (elementType instanceof BBooleanType) {
            type = Boolean.TYPE;
        } else if (elementType instanceof BDecimalType) {
            type = DecimalValue.class;
        } else if (elementType instanceof BByteType) {
            type = Integer.TYPE;
        } else if (elementType instanceof BArrayType || elementType instanceof BTupleType) {
            type = ArrayValue.class;
        } else if (elementType instanceof BFloatType) {
            type = Double.TYPE;
        } else if (elementType instanceof BMapType || elementType instanceof BRecordType) {
            type = MapValue.class;
        } else if (elementType instanceof BXMLType) {
            type = XMLValue.class;
        } else if (elementType instanceof BObjectType) {
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

}

