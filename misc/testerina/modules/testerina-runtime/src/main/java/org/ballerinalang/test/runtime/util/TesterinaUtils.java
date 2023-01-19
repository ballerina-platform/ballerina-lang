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

import io.ballerina.identifier.Utils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.scheduling.RuntimeRegistry;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.ballerinalang.test.runtime.entity.Test;
import org.ballerinalang.test.runtime.entity.TestArguments;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.entity.TesterinaFunction;
import org.ballerinalang.test.runtime.exceptions.BallerinaTestException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.ballerina.identifier.Utils.encodeNonFunctionIdentifier;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BLANG_SRC_FILE_SUFFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MODULE_INIT_CLASS_NAME;
import static io.ballerina.runtime.internal.launch.LaunchUtils.startTrapSignalHandler;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.ANON_ORG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;

/**
 * Utility methods.
 */
public class TesterinaUtils {

    private static final PrintStream errStream = System.err;

    private static final String GENERATE_OBJECT_CLASS_PREFIX = ".$value$";
    private static final String GENERATE_PKG_INIT = "___init_";
    private static final String GENERATE_PKG_START = "___start_";
    private static final String GENERATE_PKG_STOP = "___stop_";
    private static final String INIT_FUNCTION_SUFFIX = "..<init>";
    private static final String START_FUNCTION_SUFFIX = ".<start>";
    private static final String STOP_FUNCTION_SUFFIX = ".<stop>";
    private static final String INIT_FUNCTION_NAME = ".<init>";
    private static final String START_FUNCTION_NAME = ".<start>";
    private static final String STOP_FUNCTION_NAME = ".<stop>";
    private static final String CONFIGURATION_CLASS_NAME = "$configurationMapper";
    private static final String CONFIG_FILE_NAME = "Config.toml";

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
    public static void executeTests(Path sourceRootPath, TestSuite testSuite,
                                    ClassLoader classLoader, TestArguments args) throws RuntimeException {
        try {
            execute(testSuite, classLoader, args);
            cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
//            if (testRunner.getTesterinaReport().isFailure()) {
//                throw new RuntimeException("there are test failures");
//            }
        } catch (BallerinaTestException e) {
            if (e.getMessage() != null) {
                errStream.println("error: " + e.getMessage());
            }
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("test execution failed due to runtime exception");
        }
    }

    private static void execute(TestSuite suite, ClassLoader classLoader, TestArguments args) {
        String initClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(),
                suite.getTestPackageID(),
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
                .getQualifiedClassName(suite.getOrgName(), suite.getTestPackageID(), suite.getVersion(),
                        CONFIGURATION_CLASS_NAME);
        try {
            configClazz = classLoader.loadClass(configClassName);
        } catch (Throwable e) {
            throw new BallerinaTestException("failed to load configuration class :" + configClassName);
        }
        String suiteExecuteFilePath = suite.getExecuteFilePath();
        if (suite.getOrgName().equals(ANON_ORG) && suite.getTestPackageID().equals(DOT)) {
            suiteExecuteFilePath = suiteExecuteFilePath.replace(DOT, FILE_NAME_PERIOD_SEPARATOR);
        }
        String testExecuteClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(),
                suite.getTestPackageID(),
                suite.getVersion(),
                suiteExecuteFilePath);
        Class<?> testExecuteClazz;
        try {
            testExecuteClazz = classLoader.loadClass(testExecuteClassName);
        } catch (Throwable e) {
            throw new BallerinaTestException("failed to load test execution class :" + testExecuteClassName);
        }
        Scheduler scheduler = new Scheduler(4, false);
        Scheduler initScheduler = new Scheduler(4, false);

        // start TRAP signal handler which produces the strand dump
        startTrapSignalHandler();

        // This will init and start the test module.
        startSuite(suite, initScheduler, initClazz, configClazz, testExecuteClazz, args);
        // Call module stop and test stop function
        stopSuite(scheduler, initClazz);
    }

    private static void startSuite(TestSuite suite, Scheduler initScheduler, Class<?> initClazz,
                            Class<?> configClazz, Class<?> testExecuteClazz, TestArguments args) {
        TesterinaFunction init = new TesterinaFunction(initClazz, INIT_FUNCTION_NAME, initScheduler);
        TesterinaFunction start = new TesterinaFunction(initClazz, START_FUNCTION_NAME, initScheduler);
        TesterinaFunction configInit = new TesterinaFunction(configClazz, "$configureInit", initScheduler);
        TesterinaFunction testExecute = new TesterinaFunction(testExecuteClazz, "__execute__", initScheduler);
        // As the init function we need to use $moduleInit to initialize all the dependent modules
        // properly.

        Object response = configInit.directInvoke(new Class[]{String[].class, Path[].class, String.class},
                new Object[]{new String[]{}, getConfigPaths(suite), null});
        if (response instanceof Throwable) {
            throw new BallerinaTestException("configurable initialization for test suite failed due to " +
                    formatErrorMessage((Throwable) response), (Throwable) response);
        }

        init.setName("$moduleInit");
        response = init.invoke();
        if (response instanceof Throwable) {
            throw new BallerinaTestException("dependant module initialization for test suite failed due to " +
                    formatErrorMessage((Throwable) response), (Throwable) response);
        }
        // As the start function we need to use $moduleStart to start all the dependent modules
        // properly.
        start.setName("$moduleStart");
        response = start.invoke();
        if (response instanceof Throwable) {
            throw new BallerinaTestException("dependant module start for test suite failed due to error : " +
                    formatErrorMessage((Throwable) response), (Throwable) response);
        }

        response = testExecute.invoke(args.getArgTypes(), args.getArgValues());
        if (response instanceof Throwable) {
            throw new BallerinaTestException();
        }

        // Once the start function finish we will re start the scheduler with immortal true
        initScheduler.setImmortal(true);
        Thread immortalThread = new Thread(initScheduler::start, "module-start");
        immortalThread.setDaemon(true);
        immortalThread.start();
    }

    private static void stopSuite(Scheduler scheduler, Class<?> initClazz) {
        TesterinaFunction stop = new TesterinaFunction(initClazz, STOP_FUNCTION_NAME, scheduler);
        stop.setName("$moduleStop");
        Object response = stop.directInvoke(new Class<?>[]{RuntimeRegistry.class},
                new Object[]{scheduler.getRuntimeRegistry()});
        if (response instanceof Throwable) {
            throw new BallerinaTestException("dependant module stop for test suite failed due to " +
                    formatErrorMessage((Throwable) response), (Throwable) response);
        }
    }

    private static String formatErrorMessage(Throwable e) {
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

    private static Path[] getConfigPaths(TestSuite testSuite) {
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

    /**
     * Format error message.
     *
     * @param errorMsg error message
     * @return formatted error message
     */
    public static String formatError(String errorMsg) {
        StringBuilder newErrMsg = new StringBuilder();
        errorMsg = errorMsg.replaceAll("\n", "\n\t");
        List<String> msgParts = Arrays.asList(errorMsg.split("\n"));
        boolean stackTraceStartFlag = true;

        for (String msg : msgParts) {
            if (msgParts.indexOf(msg) != 0 && !msg.equals("\t    ")) {
                String prefix = "\t\t    \t";
                if (msg.startsWith("\t\t") && stackTraceStartFlag) {
                    prefix = "\n" + prefix;
                    stackTraceStartFlag = false;
                }
                msg = prefix + msg.trim();
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
            className = encodeNonFunctionIdentifier(packageName) + "." + 
                    RuntimeUtils.getMajorVersion(version) + "." + className;
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
                        handleWildCards(filteredList, suite.getTests(), functionDetail[1]);
                    }
                } catch (IndexOutOfBoundsException e) {
                    errStream.println("Error occurred while executing tests. Test list cannot be empty");
                }
            } else {
                handleWildCards(filteredList, suite.getTests(), function);
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

    private static void handleWildCards(List<String> filteredList, List<Test> suiteTests, String function) {
        if (function.contains(TesterinaConstants.WILDCARD)) {
            for (Test test: suiteTests) {
                if (Pattern.matches(function.replace(TesterinaConstants.WILDCARD, DOT + TesterinaConstants.WILDCARD),
                        test.getTestName())) {
                    filteredList.add(test.getTestName());
                }
            }
        } else {
            filteredList.add(function);
        }
    }

    public static List<org.ballerinalang.test.runtime.entity.Test> getSingleExecutionTestsOld(
            List<org.ballerinalang.test.runtime.entity.Test> currentTests, List<String> functions) {
        return Collections.emptyList();
    }

    public static String getPrintableStackTrace(Throwable throwable) {
        String errorMsg = throwable.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(errorMsg);
        // Append function/action/resource name with package path (if any)
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length == 0) {
            return sb.toString();
        }
        sb.append("\n\tat ");
        // print first element
        printStackElement(sb, stackTrace[0], "");
        for (int i = 1; i < stackTrace.length; i++) {
            printStackElement(sb, stackTrace[i], "\n\t   ");
        }
        return sb.toString();
    }

    private static void printStackElement(StringBuilder sb, StackTraceElement stackTraceElement, String tab) {
        String pkgName = Utils.decodeIdentifier(stackTraceElement.getClassName());
        String fileName = stackTraceElement.getFileName();

        if (fileName == null) {
            fileName = "unknown-source";
        }

        // clean file name from pkgName since we print the file name after the method name.
        fileName = fileName.replace(BLANG_SRC_FILE_SUFFIX, "");
        fileName = fileName.replace("/", "-");
        int index = pkgName.lastIndexOf("." + fileName);
        if (index != -1) {
            pkgName = pkgName.substring(0, index);
        }
        // todo we need to seperate orgname and module name with '/'

        sb.append(tab);
        if (!pkgName.equals(MODULE_INIT_CLASS_NAME)) {
            sb.append(pkgName).append(":");
        }

        // Append the method name
        sb.append(Utils.decodeIdentifier(stackTraceElement.getMethodName()));
        // Append the filename
        sb.append("(").append(fileName);
        // Append the line number
        sb.append(":").append(stackTraceElement.getLineNumber()).append(")");
    }

    public static StackTraceElement[] getStackTrace(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        List<StackTraceElement> filteredStack = new LinkedList<>();
        int index = 0;
        for (StackTraceElement stackFrame : stackTrace) {
            Optional<StackTraceElement> stackTraceElement = filterStackTraceElement(stackFrame, index++);
            stackTraceElement.ifPresent(filteredStack::add);
        }
        StackTraceElement[] filteredStackArray = new StackTraceElement[filteredStack.size()];
        return filteredStack.toArray(filteredStackArray);
    }

    private static Optional<StackTraceElement> filterStackTraceElement(StackTraceElement stackFrame, int currentIndex) {
        String fileName = stackFrame.getFileName();
        int lineNo = stackFrame.getLineNumber();
        if (lineNo < 0) {
            return Optional.empty();
        }
        // Handle init function
        String className = stackFrame.getClassName();
        String methodName = stackFrame.getMethodName();
        if (className.equals(MODULE_INIT_CLASS_NAME)) {
            if (currentIndex == 0) {
                return Optional.empty();
            }
            switch (methodName) {
                case GENERATE_PKG_INIT:
                    methodName = INIT_FUNCTION_SUFFIX;
                    break;
                case GENERATE_PKG_START:
                    methodName = START_FUNCTION_SUFFIX;
                    break;
                case GENERATE_PKG_STOP:
                    methodName = STOP_FUNCTION_SUFFIX;
                    break;
                default:
                    return Optional.empty();
            }
            return Optional.of(new StackTraceElement(cleanupClassName(className), methodName, fileName,
                    stackFrame.getLineNumber()));

        }
        if (fileName != null && !fileName.endsWith(BLANG_SRC_FILE_SUFFIX)) {
            // Remove java sources for bal stacktrace if they are not extern functions.
            return Optional.empty();
        }
        return Optional.of(
                new StackTraceElement(cleanupClassName(className), methodName, fileName, stackFrame.getLineNumber()));
    }

    private static String cleanupClassName(String className) {
        return className.replace(GENERATE_OBJECT_CLASS_PREFIX, ".");
    }
}
