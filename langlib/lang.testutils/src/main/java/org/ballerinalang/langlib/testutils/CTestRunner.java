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

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BNullType;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;

/**
 * Invokes the test functions in the Test Suite.
 *
 * @since 2.0.0
 */

public class CTestRunner {
    private static final PrintStream outStream = System.out;
    private static final PrintStream errStream = System.err;
    public String functionClassName;

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
        if (cTests.getTestFunctions().size() == 0) {
            outStream.println("\tNo tests steps found\n");
            return;
        }
        CompileResult compileResult = BCompileUtil.compile(cTests.getPath());
        this.functionClassName = cTests.getPath();
        if (!compileResult.toString().equals(CTestConstants.TEST_COMPILATION_PASS_STATUS)) {
            // TODO: implement function for negative tests
            throw new RuntimeException(this.functionClassName + " compilation failed! ");
        } else {
            for (TestFunction testFunction : cTests.getTestFunctions()) {
                invokeTestSteps(compileResult, testFunction);
            }
        }
    }

    public void invokeTestSteps(CompileResult compileResult, TestFunction testFunction) {
        String functionName = testFunction.getFunctionName();
        if (!testFunction.isAssertParamFlag() && !testFunction.isParamFlag()) {
            invokeFunction(compileResult, functionName);
        } else if (!testFunction.isAssertParamFlag() && testFunction.isParamFlag()) {
            invokeFunctionWithParam(compileResult, functionName, testFunction.getParameters());
        } else if (testFunction.isAssertParamFlag() && !testFunction.isParamFlag()) {
            invokeFunctionWithAssert(compileResult, functionName, testFunction.getAssertVal(),
                    testFunction.getPanicFlag());
        } else {
            invokeFunctionWithArgs(compileResult, functionName, testFunction.getParameters(),
                    testFunction.getAssertVal(), testFunction.getPanicFlag());
        }
    }

    private static void invokeFunction(CompileResult compileResult, String functionName) {
        Object[] status = BRunUtil.cInvoke(compileResult, functionName, new Object[0], new Class<?>[0], true);
        if (status[1].equals(CTestConstants.TEST_FAILED_STATUS)) {
            CTestSuite.failedTestCount++;
        }
    }

    private static void invokeFunctionWithParam(CompileResult compileResult, String functionName,
                                                List<Map<String, String>> parmas) {
        int numberOfParams = parmas.size();
        Object[] args = new Object[numberOfParams];
        Class<?>[] jvmParamType = getJvmParams(parmas, args);
        Object[] status = BRunUtil.cInvoke(compileResult, functionName, args, jvmParamType, true);
        if (status[1].equals(CTestConstants.TEST_FAILED_STATUS)) {
            CTestSuite.failedTestCount++;
        }
    }

    private void invokeFunctionWithAssert(CompileResult compileResult, String functionName, List<Map<String,
            String>> assertVal, boolean panicFlag) {
        String[] functionDetails = {this.functionClassName, functionName};
        Object[] results = BRunUtil.cInvoke(compileResult, functionName, new Object[0], new Class<?>[0], panicFlag);
        validateResult(results[0], assertVal, functionDetails, results[1].toString());
    }

    private void invokeFunctionWithArgs(CompileResult compileResult, String functionName,
                            List<Map<String, String>> parmas, List<Map<String, String>> assertVal, boolean panicFlag) {
        String[] functionDetails = {this.functionClassName, functionName};
        int numberOfParams = parmas.size();
        Object[] args = new Object[numberOfParams];
        Class<?>[] jvmParamType = getJvmParams(parmas, args);
        Object[] results = BRunUtil.cInvoke(compileResult, functionName, args, jvmParamType, panicFlag);
        validateResult(results[0], assertVal, functionDetails, results[1].toString());
    }

    private static Class<?>[] getJvmParams(List<Map<String, String>> parmas, Object[] args) {
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
        return getJvmParamTypes(paramType, paramValue, args);
    }

    // check whether the return value of a function match with the expected value
    private static void validateResult(Object actualValue, List<Map<String, String>> assertVariable,
                                       String[] functionDetails, String status) {
        if (status.equals(CTestConstants.TEST_FAILED_STATUS)) {
            CTestSuite.failedTestCount++;
        } else {
            Object[] expectedValue = new Object[1];
            getJvmParams(assertVariable, expectedValue);
            assertValueEqual(actualValue, expectedValue[0], functionDetails);
        }
    }

    // get the JVM values and Types for the xml values
    private static Class<?>[] getJvmParamTypes(String[] paramType, String[] paramValue, Object[] args) {
        Class<?>[] paramTypes = new Class<?>[paramType.length];
        for (int i = 0; i < paramType.length; i++) {
            String type = paramType[i];
            switch (type) {
                case CTestConstants.TEST_INT_TAG:
                    paramTypes[i] = Long.class;
                    args[i] = Long.parseLong(paramValue[i]);
                    break;
                case CTestConstants.TEST_ERROR_TAG:     // currently error value is considered as a string value
                case CTestConstants.TEST_STRING_TAG:
                    paramTypes[i] = String.class;
                    args[i] = paramValue[i];
                    break;
                case CTestConstants.TEST_BOOLEAN_TAG:
                    paramTypes[i] = Boolean.class;
                    args[i] = Boolean.parseBoolean(paramValue[i]);
                    break;
                case CTestConstants.TEST_DECIMAL_TAG:
                    paramTypes[i] = DecimalValue.class;
                    args[i] = new DecimalValue(new BigDecimal(paramValue[i], MathContext.DECIMAL128).setScale(
                            1, BigDecimal.ROUND_HALF_EVEN));
                    break;
                case CTestConstants.TEST_FLOAT_TAG:
                    paramTypes[i] = Double.class;
                    args[i] = Double.parseDouble(paramValue[i]);
                    break;
                case CTestConstants.TEST_NIL_TAG:
                    paramTypes[i] = BNullType.class;
                    args[i] = null;
                    break;
                default:
                    throw new RuntimeException("unknown param type: " + type);
            }
        }
        return paramTypes;
    }

    private static void assertValueEqual(Object actual, Object expected, String[] functionDetails) {
        if (!TypeChecker.isEqual(expected, actual)) {
            String msg = "{ballerina/lang.test}AssertionError" +
                    " expected: [" + expected + "] but found: [" + actual + "]";
            errStream.println(functionDetails[0] + "-> " + functionDetails[1] + " failed \n " + msg);
            CTestSuite.failedTestCount++;
        }
    }
}
