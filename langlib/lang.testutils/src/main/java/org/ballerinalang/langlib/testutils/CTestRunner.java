/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.testutils;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Invokes the test functions in the Test Suite.
 *
 * @since 2.0.0
 */

public class CTestRunner {
    private static final PrintStream outStream = System.out;
    private static final PrintStream errStream = System.err;

    public void invokeTestSuite(CTestSuite cTestSuite) {
        if (cTestSuite.getTestGroups().size() == 0) {
            outStream.println("\tNo test group found\n");
            return;
        }
        for (CTestGroup cTestGroup : cTestSuite.getTestGroups()) {
            invokeTestGroup(cTestGroup);
        }
    }

    public void invokeTestGroup(CTestGroup cTestGroup) {
        if (cTestGroup.getTests().size() == 0) {
            outStream.println("\tNo tests found\n");
            return;
        }
        for (CTests cTests : cTestGroup.getTests()) {
            invokeTests(cTests);
        }
    }

    public void invokeTests(CTests cTests) {
        ArrayList<ReportDetails> records = new ArrayList<ReportDetails>();
        if (cTests.getTestFunctions().size() == 0) {
            outStream.println("\tNo tests steps found\n");
            return;
        }
        CompileResult compileResult = BCompileUtil.compile(cTests.getPath());
        if (!compileResult.toString().equals(CTestConstants.TEST_COMPILATION_PASS_STATUS)) {
            // TODO: implement function for negative tests
            throw new RuntimeException(cTests.getPath() + " compilation failed! \n" + compileResult);
        } else {
            for (TestFunction testFunction : cTests.getTestFunctions()) {
                ReportDetails reportDetails = new ReportDetails();
                reportDetails.setClassName(cTests.getPath());
                reportDetails.setFunctionName(testFunction.getFunctionName());
                long startTime = System.currentTimeMillis();
                boolean failFlag = false;
                String returnValue;
                try {
                    returnValue = invokeWithTimeoutWithExecutor(compileResult, testFunction, cTests.getPath());
                } catch (ExecutionException e) {
                    failFlag = true;
                    returnValue = e.getCause().toString();
                } catch (InterruptedException e) {
                    failFlag = true;
                    returnValue = e.getCause().toString();
                }
                if (failFlag) {
                    outStream.println(cTests.getPath() + " -> " + testFunction.getFunctionName() + "failed \n"
                            + returnValue);
                }
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                reportDetails.setExecutionTime(String.valueOf(totalTime));
                reportDetails.setStackTrace(returnValue);
                if (returnValue.equals(CTestConstants.TEST_PASSED_STATUS)) {
                    reportDetails.setTestStatus(CTestConstants.TEST_PASSED_STATUS);
                } else {
                    reportDetails.setTestStatus(CTestConstants.TEST_FAILED_STATUS);
                }
                records.add(reportDetails);
            }
        }
        //generate Html report
        GenerateHtmlReportForTest.generateReport(cTests.getTestName(), records);
    }

    public static String invokeTestSteps(CompileResult compileResult, TestFunction testFunction,
                                         String functionClassName) {
        String functionName = testFunction.getFunctionName();
        String returnValue;
        if (!testFunction.isAssertParamFlag() && !testFunction.isParamFlag()) {
            returnValue = invokeFunction(compileResult, functionName);
        } else if (!testFunction.isAssertParamFlag() && testFunction.isParamFlag()) {
            returnValue = invokeFunctionWithParam(compileResult, functionName, functionClassName,
                    testFunction.getDataProvider());
        } else if (testFunction.isAssertParamFlag() && !testFunction.isParamFlag()) {
            returnValue = invokeFunctionWithAssert(compileResult, functionName, functionClassName,
                    testFunction.getAssertDataProvider(), testFunction.getPanicFlag());
        } else {
            returnValue = invokeFunctionWithArgs(compileResult, functionName, functionClassName,
                    testFunction.getDataProvider(), testFunction.getAssertDataProvider(), testFunction.getPanicFlag());
        }
        return returnValue;
    }

    private static String invokeFunction(CompileResult compileResult, String functionName) {
        Object[] status = BRunUtil.cInvoke(compileResult, functionName, new Object[0], true);
        if (status[1].equals(CTestConstants.TEST_FAILED_STATUS)) {
            CTestSuite.failedTestCount++;
        } else {
            status[0] = CTestConstants.TEST_PASSED_STATUS;
        }
        return status[0].toString();
    }

    private static String invokeFunctionWithParam(CompileResult compileResult, String functionName,
                                          String functionClassName, List<List<Map<String, String>>> dataProvider) {
        Object[] status = new Object[2];
        String[] functionDetails = {functionClassName, functionName};
        for (List<Map<String, String>> parmas:dataProvider) {
            boolean[] pStatus = {true};
            Object[] args = getJvmParams(parmas, pStatus, functionDetails);
            if (pStatus[0]) {
                status = BRunUtil.cInvoke(compileResult, functionName, args, true);
            } else {
                status[0] = args[0];
                status[1] = CTestConstants.TEST_FAILED_STATUS;
            }
            if (status[1].equals(CTestConstants.TEST_FAILED_STATUS)) {
                CTestSuite.failedTestCount++;
                break;
            } else {
                status[0] = CTestConstants.TEST_PASSED_STATUS;
            }
        }
        return status[0].toString();
    }

    private static String invokeFunctionWithAssert(CompileResult compileResult, String functionName,
                   String functionClassName, List<List<Map<String, String>>> assertDataProvider, boolean panicFlag) {
        String[] functionDetails = {functionClassName, functionName};
        String returnValue = null;
        for (List<Map<String, String>> params:assertDataProvider) {
            Object[] results = BRunUtil.cInvoke(compileResult, functionName, new Object[0], panicFlag);
            returnValue = validateResult(results[0], params, functionDetails, results[1].toString());
            if (returnValue == null) {
                if (results[1].equals(CTestConstants.TEST_FAILED_STATUS)) {
                    returnValue = results[0].toString();
                    break;
                } else {
                    returnValue = CTestConstants.TEST_PASSED_STATUS;
                }
            } else {
                break;
            }
        }
        return returnValue;
    }

    private static String invokeFunctionWithArgs(CompileResult compileResult, String functionName,
                                             String functionClassName, List<List<Map<String, String>>> dataProvider,
                                             List<List<Map<String, String>>> assertDataProvider, boolean panicFlag) {
        String[] functionDetails = {functionClassName, functionName};
        Object[] results;
        String returnValue = null;
        int counter = 0;
        for (List<Map<String, String>> params:dataProvider) {
            boolean[] status = {true};
            Object[] args = getJvmParams(params, status, functionDetails);
            if (status[0]) {
                results = BRunUtil.cInvoke(compileResult, functionName, args, panicFlag);
                returnValue = validateResult(results[0], assertDataProvider.get(counter++), functionDetails,
                        results[1].toString());
                if (returnValue == null) {
                    if (results[1].equals(CTestConstants.TEST_FAILED_STATUS)) {
                        returnValue = results[0].toString();
                        break;
                    } else {
                        returnValue = CTestConstants.TEST_PASSED_STATUS;
                    }
                } else {
                    break;
                }
            } else {
                returnValue = args[0].toString();
                break;
            }
        }
        return returnValue;
    }

    private static Object[] getJvmParams(List<Map<String, String>> parmas, boolean[] status, String[] functionDetails) {
        int numberOfParams = parmas.size();
        String[] paramType = new String[numberOfParams];
        String[] paramValue = new String[numberOfParams];
        int i = 0;
        for (Map<String, String> mp : parmas) {
            for (Map.Entry<String, String> entry : mp.entrySet()) {
                paramType[i] = entry.getKey();
                paramValue[i] = entry.getValue();
                i++;
            }
        }
        String[] pStatus = new String[1];
        Object[] parameters = getJvmParamTypes(paramType, paramValue, pStatus);
        if (pStatus[0] != null) {
            errStream.println(functionDetails[0] + " -> " + functionDetails[1] + " failed! \n" + pStatus[0]);
            parameters[0] = pStatus[0];
            status[0] = false;
            CTestSuite.failedTestCount++;
        }
        return parameters;
    }

    // check whether the return value of a function match with the expected value
    private static String validateResult(Object actualValue, List<Map<String, String>> assertVariable,
                                         String[] functionDetails, String status) {
        String returnValue = null;
        if (status.equals(CTestConstants.TEST_FAILED_STATUS)) {
            CTestSuite.failedTestCount++;
        } else {
            // check if the param type is valid
            boolean[] pStatus = {true};
            Object[] expectedValue = getJvmParams(assertVariable, pStatus, functionDetails);
            if (pStatus[0]) {
                returnValue = assertValueEqual(actualValue, expectedValue[0], functionDetails);
            } else {
                returnValue = expectedValue[0].toString();
            }
        }
        return returnValue;
    }

    // get the JVM values and Types for the xml values
    private static Object[] getJvmParamTypes(String[] paramType, String[] paramValue, String[] status) {
        Object[] args = new Object[paramType.length];
        for (int i = 0; i < paramType.length; i++) {
            String type = paramType[i];
            switch (type) {
                case CTestConstants.TEST_INT_TAG:
                    args[i] = Long.parseLong(paramValue[i]);
                    break;
                case CTestConstants.TEST_ERROR_TAG:     // currently error value is considered as a string value
                    args[i] = paramValue[i];
                    break;
                case CTestConstants.TEST_STRING_TAG:
                    args[i] = StringUtils.fromString(paramValue[i]);
                    break;
                case CTestConstants.TEST_BOOLEAN_TAG:
                    args[i] = Boolean.parseBoolean(paramValue[i]);
                    break;
                case CTestConstants.TEST_DECIMAL_TAG:
                    args[i] = new DecimalValue(new BigDecimal(paramValue[i], MathContext.DECIMAL128).setScale(
                            1, BigDecimal.ROUND_HALF_EVEN));
                    break;
                case CTestConstants.TEST_FLOAT_TAG:
                    args[i] = Double.parseDouble(paramValue[i]);
                    break;
                case CTestConstants.TEST_NIL_TAG:
                    args[i] = null;
                    break;
                default:
                    status[0] = "unknown param type: " + type;
            }
        }
        return args;
    }

    private static String assertValueEqual(Object actual, Object expected, String[] functionDetails) {
        String msg = null;
        if (!TypeChecker.isEqual(expected, actual)) {
            msg = CTestConstants.TEST_FAIL_REASON +
                    " expected: [" + expected + "] but found: [" + actual + "]";
            errStream.println(functionDetails[0] + "-> " + functionDetails[1] + " failed \n " + msg);
            CTestSuite.failedTestCount++;
        }
        return msg;
    }

    private static String invokeWithTimeoutWithExecutor(CompileResult compileResult, TestFunction testFunction,
                                            String functionClassName) throws ExecutionException, InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(1,
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread t = Executors.defaultThreadFactory().newThread(r);
                        t.setDaemon(true);
                        return t;
                    }
                });
        Task task = new Task();
        task.setArgs(compileResult, testFunction, functionClassName);
        Future<String> future = exec.submit(task);
        exec.shutdown();
        boolean finished = false;
        finished = exec.awaitTermination(60, TimeUnit.SECONDS);
        if (!finished) {
            exec.shutdownNow();
            outStream.println(functionClassName + " => " + testFunction.getFunctionName() + " failed! ");
            throw new RuntimeException("Time limit exceeded.");
        }
        return future.get();
    }
}

class Task implements Callable<String> {
    private CompileResult compileResult;
    private TestFunction testFunction;
    private String functionClassName;

    public void setArgs(CompileResult compileResult, TestFunction testFunction, String functionClassName) {
        this.compileResult = compileResult;
        this.testFunction = testFunction;
        this.functionClassName = functionClassName;
    }

    @Override
    public String call() {
        return CTestRunner.invokeTestSteps(compileResult, testFunction, functionClassName);
    }
}
