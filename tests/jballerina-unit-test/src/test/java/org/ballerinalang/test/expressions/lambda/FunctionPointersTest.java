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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for Function pointers and lambda.
 *
 * @since 0.90
 */
public class FunctionPointersTest {

    private CompileResult fpProgram, privateFPProgram, globalProgram, structProgram, closureFPProgram;

    @BeforeClass
    public void setup() {
        fpProgram = BCompileUtil.compile("test-src/expressions/lambda/function-pointers.bal");
        privateFPProgram = BCompileUtil.compile(this, "test-src/expressions/lambda/FunctionPointersProject",
                "private-function-pointers");
        globalProgram = BCompileUtil.compile("test-src/expressions/lambda/global-function-pointers.bal");
        structProgram = BCompileUtil.compile("test-src/expressions/lambda/struct-function-pointers.bal");
        closureFPProgram =
                BCompileUtil.compile("test-src/expressions/lambda/function-pointer-with-closure.bal");
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
    public void testInTypeGuard() {
        invokeFunctionPointerProgram(fpProgram, "testInTypeGuard", 0);
    }

    @Test
    public void testPrivateFunctionPointerTest1() {
        invokeFunctionPointerProgram(privateFPProgram, "test1", 3);
    }

    @Test
    public void testPrivateFunctionPointerTest2() {
        invokeFunctionPointerProgram(privateFPProgram, "test2", 3);
    }

    @Test
    public void testInvokingLambdasWithSameName() {
        BValue[] returns = BRunUtil.invoke(privateFPProgram, "getCombinedString");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "foo bar");
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

    @Test(description = "Test global function type defs with closures")
    public void testGlobalFunctionTypeDefWithClosures() {
        BRunUtil.invoke(globalProgram, "testGlobalFunctionTypeDefWithClosures");
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

    @Test(expectedExceptions = BLangRuntimeException.class)
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

    @Test
    public void testClassTypeAsParamtype() {
        BRunUtil.invoke(fpProgram, "testGetMemberFunctionAsAField");
    }

    @Test
    public void testMemberTakenAsAFieldWithRestArgs() {
        BRunUtil.invoke(fpProgram, "testMemberTakenAsAFieldWithRestArgs");
    }

    @Test
    public void testFunctionPointerNative() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/function-pointer-native.bal");
        BValue[] returns = BRunUtil.invoke(result, "test1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "1500526800000");
    }

    @Test
    public void testFunctionPointerAsFuncParam() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFunctionPointerAsFuncParam");
        Assert.assertNotNull(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 13);

        Assert.assertNotNull(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Total: 6 USD");
    }

    @Test
    public void testAnyToFuncPointerConversion_1() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testAnyToFuncPointerConversion_1");
        Assert.assertNotNull(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testFuncPointerConversion() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFuncPointerConversion");
        Assert.assertNotNull(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError " +
                    "\\{\"message\":\"incompatible types: " +
                    "'function \\(Student\\) returns \\(int\\)' cannot be cast to 'function \\(Person\\)" +
                    " returns \\(int\\)'.*")
    public void testAnyToFuncPointerConversion_2() {
        BRunUtil.invoke(fpProgram, "testAnyToFuncPointerConversion_2");
    }

    @Test(description = "Test assigning a function pointer to any and casting it back")
    public void testAnyToFunctionPointer() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/fp2any.bal");
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test1");
    }

    @Test
    public void testFunctionPointerWithAClosure() {
        BValue[] returns = BRunUtil.invoke(closureFPProgram, "testArrayFunctionInfer");
        Assert.assertEquals(returns[0].stringValue(), "abccde");
    }

    @Test
    public void testInvoke() {
        BValue[] returns = BRunUtil.invoke(closureFPProgram, "invokeApplyFunction");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 6);
    }

    @Test(description = "Test function pointers with subtyping")
    public void testSubTypingWithAny() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testSubTypingWithAny");
        Assert.assertEquals(returns[0].stringValue(), "12");
    }

    @Test(description = "Test global function pointers defined using var")
    public void testGlobalFunctionPointerVar() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testGlobalFunctionPointerVar");
        Assert.assertEquals(returns[0].stringValue(), "f1");
    }

    @Test(description = "Test global function pointers that are defined with type")
    public void testGlobalFunctionPointerTyped() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testGlobalFunctionPointerTyped");
        Assert.assertEquals(returns[0].stringValue(), "f2");
    }

    @Test(description = "Test passing a no-return function pointer as a nil-returning function pointer")
    public void testVoidFunctionAsUnionReturnFunction() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testVoidFunctionAsUnionReturnFunction");
        Assert.assertEquals(returns[0].stringValue(), "value - updated through lambda");
    }

    @Test(description = "Test function pointers defaultable params")
    public void testDefaultParams() {
        BRunUtil.invoke(fpProgram, "testDefaultParams");
    }
}
