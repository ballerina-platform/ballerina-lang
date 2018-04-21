/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.expressions.lambda;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for Function pointers and lambda.
 *
 * @since 0.90
 */
public class FunctionPointersTest {

    CompileResult fpProgram, privateFPProgram, globalProgram, structProgram;

    @BeforeClass
    public void setup() {
        fpProgram = BCompileUtil.compile("test-src/expressions/lambda/function-pointers.bal");
        privateFPProgram = BCompileUtil.compile(this, "test-src/expressions/lambda", "private-function-pointers");
        globalProgram = BCompileUtil.compile("test-src/expressions/lambda/global-function-pointers.bal");
        structProgram = BCompileUtil.compile("test-src/expressions/lambda/struct-function-pointers.bal");
    }

    @Test
    public void testFunctionPointerAsVariable() {
        invokeFunctionPointerProgram(fpProgram, "test1", 3);
    }

    @Test
    public void testFunctionPointerAsLambda() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "test2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "sum is 3");
    }

    @Test
    public void testFunctionPointerAsParameter() {
        invokeFunctionPointerProgram(fpProgram, "test3", 4);
    }

    @Test
    public void testLambdaAsReturnParameter() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "test4");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "hello world.");
    }

    @Test
    public void testFunctionPointerAsReturnParameter() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "test5");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test5 string1.0");
    }


    @Test
    public void testNestedFunctionPointersAsParameters() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "test6");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test6 test6 1.0");
    }

    @Test
    public void testFuncWithArrayParams() {
        invokeFunctionPointerProgram(fpProgram, "testFuncWithArrayParams", 0);
    }

    @Test
    public void testPrivateFunctionPointerTest1() {
        invokeFunctionPointerProgram(privateFPProgram, "test1", 3);
    }

    @Test
    public void testPrivateFunctionPointerTest2() {
        invokeFunctionPointerProgram(privateFPProgram, "test2", 3);
    }

    private void invokeFunctionPointerProgram(CompileResult programToRun, String functionName, int valueToAssert) {
        BValue[] returns = BRunUtil.invoke(programToRun, functionName);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), valueToAssert);
    }

    @Test
    public void testGlobalFP() {
        BValue[] returns;
        // testing function pointer.
        returns = BRunUtil.invoke(globalProgram, "test1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test1");
    }

    @Test
    public void testGlobalFPAsLambda() {
        BValue[] returns;
        // lambda.
        returns = BRunUtil.invoke(globalProgram, "test2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test2true");
    }

    @Test
    public void testGlobalFPAssignment() {
        BValue[] returns;
        // assign function pointer and invoke.
        returns = BRunUtil.invoke(globalProgram, "test3");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 3);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test3");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "test3");
        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(returns[2].stringValue(), "3test");
    }

    @Test
    public void testGlobalFPWithLocalFP() {
        BValue[] returns;
        // Check global and local variable.
        returns = BRunUtil.invoke(globalProgram, "test5");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "falsetest5");
    }

    @Test
    public void testGlobalFPByAssigningLocalFP() {
        BValue[] returns;
        // assign local ref to global and invoke.
        returns = BRunUtil.invoke(globalProgram, "test6");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "truetest6");
    }

    @Test
    public void testStructFP() {
        BValue[] returns = BRunUtil.invoke(structProgram, "test1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "bob white");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "smith, tom");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = "" +
            ".*NullReferenceException.*")
    public void testStructFPNullReference() {
        BRunUtil.invoke(structProgram, "test2");
    }

    @Test
    public void testFPWithStruct() {
        BValue[] returns = BRunUtil.invoke(structProgram, "test3");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "white, bob");
    }

    @Test()
    public void testFunctionPointerNative() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/function-pointer-native.bal");
        BValue[] returns = BRunUtil.invoke(result, "test1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "1500526800000");
    }

    @Test()
    public void testFunctionPointerNullCheck() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/function-pointer-null-check.bal");
        BValue[] returns = BRunUtil.invoke(result, "checkFunctionPointerNullEqual");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);

        returns = BRunUtil.invoke(result, "checkFunctionPointerNullNotEqual");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

}
