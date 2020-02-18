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
import org.ballerinalang.test.launcher.entity.TestJsonData;
import org.ballerinalang.test.launcher.entity.TestSuite;
import org.ballerinalang.test.launcher.entity.TesterinaFunction;
import org.ballerinalang.test.launcher.entity.TesterinaReport;
import org.ballerinalang.test.launcher.entity.TesterinaResult;
import org.ballerinalang.testerina.util.TesterinaUtils;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.tool.util.BFileUtil;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
    private List<String> sourcePackages = new ArrayList<>();

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



    /**
     * Executes a given set of ballerina program files when running tests using the build command.
     *
     * @param testJsonData test meta data for module
     */
    public void runTest(TestJsonData testJsonData) {
        TestSuite suite = buildSuites(testJsonData);
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

    /**
     * Populate the registry with suites using the compiled program file when executing tests with the build command.
     *
     * @param metaData test meta data
     */
    private TestSuite buildSuites(TestJsonData metaData) {
        String packageName;
        if (metaData.getPackageID().getName().getValue().equals(".")) {
            packageName = metaData.getPackageID().getName().getValue();
        } else {
            packageName = TesterinaUtils.getFullModuleName(metaData.getPackageID().getName().getValue());
        }
        // Add a test suite to registry if not added. In this module there are no tests to be executed. But we need
        // to say that there are no tests found in the module to be executed

        // Keeps a track of the sources that are being built
        sourcePackages.add(packageName);
        return processTestMetaData(metaData);
    }

    /**
     * Process the program file i.e. the executable program generated.
     *
     * @param testJsonData program file generated
     */
    private TestSuite processTestMetaData(TestJsonData testJsonData) {
        // By default the test init function is set as the init function of the test suite
        // FunctionInfo initFunction = programFile.getEntryPackage().getTestInitFunctionInfo();
        // But if there is no test init function, then the package init function is set as the init function of the
        // test suite
        //if (initFunction == null) {
        //    initFunction = programFile.getEntryPackage().getInitFunctionInfo();
        //}
        String initClassName = BFileUtil.getQualifiedClassName(testJsonData.getPackageID().orgName.value,
                testJsonData.getPackageID().name.value,
                MODULE_INIT_CLASS_NAME);

        try {
            URL[] dependencyUrls = new URL[testJsonData.getDependencyJarPaths().length + 1];
            dependencyUrls[0] = Paths.get(testJsonData.getJarPath()).toUri().toURL();
            int i = 1;
            for (String dependencyJarPath : testJsonData.getDependencyJarPaths()) {
                dependencyUrls[i++] = (Paths.get(dependencyJarPath).toUri().toURL());
            }

            URLClassLoader urlClassLoader = new URLClassLoader(dependencyUrls);
            Class<?> initClazz = urlClassLoader.loadClass(initClassName);
            TestSuite suite = new TestSuite(testJsonData.getPackageName());
            suite.setInitFunction(new TesterinaFunction(initClazz,
                                                        testJsonData.getInitFunctionName()));
            suite.setStartFunction(new TesterinaFunction(initClazz,
                    testJsonData.getStartFunctionName()));
            suite.setStopFunction(new TesterinaFunction(initClazz,
                    testJsonData.getStopFunctionName()));
            // add all functions of the package as utility functions
            for (Map.Entry<String, String> entry : testJsonData.getCallableFunctionNames().entrySet()) {
                String functionName = entry.getKey();
                String functionClassName = entry.getValue();
                Class<?> functionClass = urlClassLoader.loadClass(functionClassName);

                suite.addTestUtilityFunction(new TesterinaFunction(functionClass, functionName));
            }

            // Add all functions of the test function to test suite
            String testClassName = BFileUtil.getQualifiedClassName(testJsonData.getPackageID().orgName.value,
                    testJsonData.getPackageID().name.value,
                    testJsonData.getPackageID().name.value);

            Class<?> testInitClazz = urlClassLoader.loadClass(testClassName);
            suite.setTestInitFunction(new TesterinaFunction(testInitClazz,
                    testJsonData.getTestInitFunctionName()));
            suite.setTestStartFunction(new TesterinaFunction(testInitClazz,
                    testJsonData.getTestStartFunctionName()));
            suite.setTestStopFunction(new TesterinaFunction(testInitClazz,
                    testJsonData.getTestStopFunctionName()));

            for (Map.Entry<String, String> entry : testJsonData.getTestFunctionNames().entrySet()) {
                String functionName = entry.getKey();
                String functionClassName = entry.getValue();
                Class<?> functionClass = urlClassLoader.loadClass(functionClassName);

                suite.addTestUtilityFunction(new TesterinaFunction(functionClass, functionName));
            }

            resolveFunctions(suite);
            int[] testExecutionOrder = checkCyclicDependencies(suite.getTests());
            List<Test> sortedTests = orderTests(suite.getTests(), testExecutionOrder);
            suite.setTests(sortedTests);
            suite.setClassLoader(urlClassLoader);
            return suite;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException(e.toString());
        }
    }

    private static List<Test> orderTests(List<Test> tests, int[] testExecutionOrder) {
        List<Test> sortedTests = new ArrayList<>();
//        outStream.println("Test execution order: ");
        for (int idx : testExecutionOrder) {
            sortedTests.add(tests.get(idx));
//            outStream.println(sortedTests.get(sortedTests.size() - 1).getTestFunction().getName());
        }
//        outStream.println("**********************");
        return sortedTests;
    }

    /**
     * Resolve function names to {@link TesterinaFunction}s.
     *
     * @param suite {@link TestSuite} whose functions to be resolved.
     */
    private static void resolveFunctions(TestSuite suite) {
        List<TesterinaFunction> functions = suite.getTestUtilityFunctions();
        List<String> functionNames = functions.stream()
                .map(TesterinaFunction::getName)
                .collect(Collectors.toList());
        for (Test test : suite.getTests()) {
            if (test.getTestName() != null && functionNames.contains(test.getTestName())) {
                test.setTestFunction(functions.stream().filter(e -> e.getName().equals(test
                        .getTestName())).findFirst().get());
            }

            if (test.getBeforeTestFunction() != null) {
                if (functionNames.contains(test.getBeforeTestFunction())) {
                    test.setBeforeTestFunctionObj(functions.stream().filter(e -> e.getName().equals(test
                            .getBeforeTestFunction())).findFirst().get());
                } else {
                    String msg = String.format("Cannot find the specified before function : [%s] for testerina " +
                            "function : [%s]", test.getBeforeTestFunction(), test.getTestName());
                    throw LauncherUtils.createLauncherException(msg);
                }
            }
            if (test.getAfterTestFunction() != null) {
                if (functionNames.contains(test.getAfterTestFunction())) {
                    test.setAfterTestFunctionObj(functions.stream().filter(e -> e.getName().equals(test
                            .getAfterTestFunction())).findFirst().get());
                } else {
                    String msg = String.format("Cannot find the specified after function : [%s] for testerina " +
                            "function : [%s]", test.getBeforeTestFunction(), test.getTestName());
                    throw LauncherUtils.createLauncherException(msg);
                }
            }

            if (test.getDataProvider() != null && functionNames.contains(test.getDataProvider())) {
                String dataProvider = test.getDataProvider();
                test.setDataProviderFunction(functions.stream().filter(e -> e.getName().equals(test.getDataProvider()
                )).findFirst().map(func -> {
                    // TODO these validations are not working properly with the latest refactoring
//                    if (func.getbFunction().getReturnTypeNode() != null) {
//                        BLangType bType = func.getbFunction().getReturnTypeNode();
//                        /*if (bType.getTag() == TypeTags.ARRAY_TAG) {
//                            BArrayType bArrayType = (BArrayType) bType;
//                            int tag = bArrayType.getElementType().getTag();
//                            if (!(tag == TypeTags.ARRAY_TAG || tag == TypeTags.TUPLE_TAG)) {
//                                String message = String.format("Data provider function [%s] should return an " +
//                                        "array of arrays or an array of tuples.", dataProvider);
//                                throw LauncherUtils.createLauncherException(message);
//                            }
//                        } else {
//                            String message = String.format("Data provider function [%s] should return an array of " +
//                                    "arrays or an array of tuples.", dataProvider);
//                            throw LauncherUtils.createLauncherException(message);
//                        }*/
//                    } else {
//                        String message = String.format("Data provider function [%s] should have only one " +
//                                "return type.", dataProvider);
//                        throw LauncherUtils.createLauncherException(message);
//                    }
                    return func;
                }).get());

                if (test.getDataProviderFunction() == null) {
                    String message = String.format("Data provider function [%s] cannot be found.", dataProvider);
                    throw LauncherUtils.createLauncherException(message);
                }
            }
            for (String dependsOnFn : test.getDependsOnTestFunctions()) {
                if (functions.stream().noneMatch(func -> func.getName().equals(dependsOnFn))) {
                    throw LauncherUtils.createLauncherException("Cannot find the specified dependsOn function : "
                            + dependsOnFn);
                }
                test.addDependsOnTestFunction(functions.stream().filter(e -> e.getName().equals(dependsOnFn))
                        .findFirst().get());
            }
        }

        // resolve mock functions
        suite.getMockFunctionNamesMap().forEach((id, functionName) -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addMockFunctionObj(id, function);
        });

        suite.getBeforeSuiteFunctionNames().forEach(functionName -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addBeforeSuiteFunctionObj(function);
        });

        suite.getAfterSuiteFunctionNames().forEach(functionName -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addAfterSuiteFunctionObj(function);
        });

        suite.getBeforeEachFunctionNames().forEach(functionName -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addBeforeEachFunctionObj(function);
        });

        suite.getAfterEachFunctionNames().forEach(functionName -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addAfterEachFunctionObj(function);
        });

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
                                ".", test.getTestFunction().getName(), dependsOnFn);
                        throw LauncherUtils.createLauncherException(message);
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
            throw LauncherUtils.createLauncherException(message);
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
    private void execute(TestSuite suite) {
        AtomicBoolean shouldSkip = new AtomicBoolean();
        String packageName = suite.getSuiteName();
        // For single bal files
        if (packageName.equals(Names.DOT.value)) {
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
        String suiteError;
        try {
            suite.start();
        } catch (Throwable e) {
            shouldSkip.set(true);
            suiteError = "\t[fail] Error while initializing test suite: "
                    + formatErrorMessage(e);
            errStream.println(suiteError);
            shouldSkip.set(true);
        }

        suite.getBeforeSuiteFunctions().forEach(test -> {
            String errorMsg;
            try {
                test.invoke();
            } catch (Throwable e) {
                shouldSkip.set(true);
                errorMsg = "\t[fail] " + test.getName() + " [before test suite function]" + ":\n\t    "
                        + formatErrorMessage(e);
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
                                                 formatErrorMessage(e));
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
                                             formatErrorMessage(e));
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
                    Object valueSets = null;
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
                        Class[] argTypes = extractArgumentTypes(valueSets);
                        List<Object[]> argList = extractArguments(valueSets);
                        argList.forEach(arg -> {
                            test.getTestFunction().invoke(argTypes, arg);
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
                                                     formatErrorMessage(e));
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
                                      formatErrorMessage(e));
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
                                              formatErrorMessage(e));
                    errStream.println(errorMsg2);
                }
            });
        });

        // Run After suite functions
        suite.getAfterSuiteFunctions().forEach(func -> {
            String errorMsg;
            try {
                func.invoke();
            } catch (Throwable e) {
                errorMsg = String.format("\t[fail] " + func.getName() + " [after test suite function] :\n\t    " +
                                                 "%s", formatErrorMessage(e));
                errStream.println(errorMsg);
            }
        });
        // Call module stop and test stop function
        suite.stop();

        // print module test results
        tReport.printTestSuiteSummary(packageName);
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
    private static Class[] extractArgumentTypes(Object valueSets) {
        List<Class> typeList = new ArrayList<>();
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
        Class[] typeListArray = new Class[typeList.size()];
        typeList.toArray(typeListArray);
        return typeListArray;
    }

    private static void setTestFunctionSignature(List<Class> typeList, ArrayValue arrayValue) {
        Class type = getArgTypeToClassMapping(arrayValue.getElementType());
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

    private static Class getArgTypeToClassMapping(BType elementType) {
        Class type;
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

