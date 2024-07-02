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

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.ballerinalang.test.runtime.entity.Test;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.exceptions.BallerinaTestException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
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
import static io.ballerina.runtime.api.constants.RuntimeConstants.MODULE_INIT_CLASS_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.ANON_ORG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.GET_TEST_EXEC_STATE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.IDENTIFIER_END_INDEX;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.IDENTIFIER_START_INDEX;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TESTERINA_MAIN_METHOD;

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
            errStream.println("Error occurred while deleting the dir : " + path + " with error : "
                                      + e.getMessage());
        }
    }

    /**
     * Execute tests in build.
     *
     * @param sourceRootPath source root path
     * @param testSuite test meta data
     */
    public static int executeTests(Path sourceRootPath, TestSuite testSuite,
                                    ClassLoader classLoader, String[] args, PrintStream out) throws RuntimeException {
        try {
            int exitStatus = execute(testSuite, classLoader, args, out);
            cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
            return exitStatus;
        } catch (BallerinaTestException e) {
            if (e.getMessage() != null) {
                errStream.println("error: " + e.getMessage());
            }
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("test execution failed due to runtime exception");
        }
    }

    private static int execute(TestSuite suite, ClassLoader classLoader, String[] args, PrintStream out) {
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
        String suiteExecuteFilePath = suite.getExecuteFilePath();
        if (suiteExecuteFilePath.isEmpty()) {
            out.println("\tNo tests found");
            return 0;
        }

        // This will run the main method of the test module.
        startSuite(initClazz, args);
        return getTestExecutionState(initClazz);
    }

    private static void startSuite(Class<?> initClazz, String[] args) {
        // Call test module main
        Object response = runTestModuleMain(initClazz, args, String[].class);
        if (response instanceof Throwable throwable) {
            throw new BallerinaTestException("dependant module execution for test suite failed due to " +
                    RuntimeUtils.formatErrorMessage(throwable), throwable);
        }
    }

    private static Object runTestModuleMain(Class<?> initClazz, String[] args, Class<?>... parameterTypes) {

        try {
            final Method method = initClazz.getDeclaredMethod(TESTERINA_MAIN_METHOD, parameterTypes);
            return method.invoke(null, (Object) args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof BError) {
                return targetException;
            }
            return targetException;
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
            return new BallerinaTestException("Failed to invoke the function '" + TESTERINA_MAIN_METHOD + " due to " +
                    RuntimeUtils.formatErrorMessage(e), e);
        }
    }

    private static int getTestExecutionState(Class<?> initClazz) {
        try {
            final Method method = initClazz.getDeclaredMethod(GET_TEST_EXEC_STATE);
            return Math.toIntExact((long) method.invoke(null));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new BallerinaTestException("Failed to invoke the function '" + GET_TEST_EXEC_STATE + " due to " +
                    RuntimeUtils.formatErrorMessage(e), e);
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
        errorMsg = errorMsg.replace("\n", "\n\t");
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

    public static String decodeIdentifier(String encodedIdentifier) {
        if (encodedIdentifier == null) {
            return encodedIdentifier;
        }
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < encodedIdentifier.length()) {
            if (encodedIdentifier.charAt(index) == '$' && index + 4 < encodedIdentifier.length()) {
                if (isUnicodePoint(encodedIdentifier, index)) {
                    sb.append((char) Integer.parseInt(encodedIdentifier.substring(index + IDENTIFIER_START_INDEX,
                            index + IDENTIFIER_END_INDEX)));
                    index += IDENTIFIER_END_INDEX;
                } else {
                    sb.append(encodedIdentifier.charAt(index));
                    index++;
                }
            } else {
                sb.append(encodedIdentifier.charAt(index));
                index++;
            }
        }
        return sb.toString();
    }

    private static boolean isUnicodePoint(String encodedName, int index) {
        return (containsOnlyDigits(encodedName.substring(index + IDENTIFIER_START_INDEX,
                index + IDENTIFIER_END_INDEX)));
    }

    private static boolean containsOnlyDigits(String digitString) {
        for (int i = 0; i < digitString.length(); i++) {
            if (!Character.isDigit(digitString.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
