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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        privateFPProgram = BCompileUtil.compile("test-src/expressions/lambda/function_pointers_project");
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
        Object returns = BRunUtil.invoke(fpProgram, "test2");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "sum is 3");
    }

    @Test
    public void testFunctionPointerAsParameter() {
      invokeFunctionPointerProgram(fpProgram, "test3", 4);
    }

    @Test
    public void testLambdaAsReturnParameter() {
        Object returns = BRunUtil.invoke(fpProgram, "test4");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "hello world.");
    }

    @Test
    public void testFunctionPointerAsReturnParameter() {
        Object returns = BRunUtil.invoke(fpProgram, "test5");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "test5 string1.0");
    }

    @Test
    public void testUnionLambdaAsReturnParameter() {
        BRunUtil.invoke(fpProgram, "testUnionLambdaAsReturnParameter");
    }


    @Test
    public void testNestedFunctionPointersAsParameters() {
        Object returns = BRunUtil.invoke(fpProgram, "test6");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "test6 test6 1.0");
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
        Object returns = BRunUtil.invoke(privateFPProgram, "getCombinedString");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "foo bar");
    }

    private void invokeFunctionPointerProgram(CompileResult programToRun, String functionName, long valueToAssert) {
        Object returns = BRunUtil.invoke(programToRun, functionName);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, valueToAssert);
    }

    @Test
    public void testGlobalFP() {
        Object returns;
        // testing function pointer.
        returns = BRunUtil.invoke(globalProgram, "test1");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "test1");
    }

    @Test
    public void testGlobalFPAsLambda() {
        Object returns;
        // lambda.
        returns = BRunUtil.invoke(globalProgram, "test2");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "test2true");
    }

    @Test
    public void testGlobalFPAssignment() {
        Object arr;
        // assign function pointer and invoke.
        arr = BRunUtil.invoke(globalProgram, "test3");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 3);
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0).toString(), "test3");
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(1).toString(), "test3");
        Assert.assertNotNull(returns.get(2));
        Assert.assertEquals(returns.get(2).toString(), "3test");
    }

    @Test
    public void testGlobalFPWithLocalFP() {
        Object returns;
        // Check global and local variable.
        returns = BRunUtil.invoke(globalProgram, "test5");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "falsetest5");
    }

    @Test
    public void testGlobalFPByAssigningLocalFP() {
        Object returns;
        // assign local ref to global and invoke.
        returns = BRunUtil.invoke(globalProgram, "test6");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "truetest6");
    }

    @Test(description = "Test global function type defs with closures")
    public void testGlobalFunctionTypeDefWithClosures() {
        BRunUtil.invoke(globalProgram, "testGlobalFunctionTypeDefWithClosures");
    }

    @Test
    public void testStructFP() {
        Object arr = BRunUtil.invoke(structProgram, "test1");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0).toString(), "bob white");
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(1).toString(), "smith, tom");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testStructFPNullReference() {
        BRunUtil.invoke(structProgram, "test2");
    }

    @Test
    public void testFPWithStruct() {
        Object returns = BRunUtil.invoke(structProgram, "test3");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "white, bob");
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
        Object returns = BRunUtil.invoke(result, "test1");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "foobar");
    }

    @Test
    public void testFunctionPointerAsFuncParam() {
        Object arr = BRunUtil.invoke(fpProgram, "testFunctionPointerAsFuncParam");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 13L);

        Assert.assertNotNull(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "Total: 6 USD");
    }

    @Test
    public void testAnyToFuncPointerConversion_1() {
        Object returns = BRunUtil.invoke(fpProgram, "testAnyToFuncPointerConversion_1");
        Assert.assertNotNull(returns instanceof Long);
        Assert.assertEquals(returns, 5L);
    }

    @Test
    public void testFuncPointerConversion() {
        Object returns = BRunUtil.invoke(fpProgram, "testFuncPointerConversion");
        Assert.assertNotNull(returns instanceof Long);
        Assert.assertEquals(returns, 40L);
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError " +
                    "\\{\"message\":\"incompatible types: " +
                    "'isolated function \\(Student\\) returns \\(int\\)' cannot be cast to 'function \\(Person\\)" +
                    " returns \\(int\\)'.*")
    public void testAnyToFuncPointerConversion_2() {
        BRunUtil.invoke(fpProgram, "testAnyToFuncPointerConversion_2");
    }

    @Test(description = "Test assigning a function pointer to any and casting it back")
    public void testAnyToFunctionPointer() {
        CompileResult result = BCompileUtil.compile("test-src/expressions/lambda/fp2any.bal");
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "test1");
    }

    @Test
    public void testFunctionPointerWithAClosure() {
        Object returns = BRunUtil.invoke(closureFPProgram, "testArrayFunctionInfer");
        Assert.assertEquals(returns.toString(), "abccde");
    }

    @Test
    public void testInvoke() {
        Object returns = BRunUtil.invoke(closureFPProgram, "invokeApplyFunction");
        Assert.assertEquals(returns, 6L);
    }

    @Test(description = "Test function pointers with subtyping")
    public void testSubTypingWithAny() {
        Object returns = BRunUtil.invoke(fpProgram, "testSubTypingWithAny");
        Assert.assertEquals(returns.toString(), "12");
    }

    @Test(description = "Test global function pointers defined using var")
    public void testGlobalFunctionPointerVar() {
        Object returns = BRunUtil.invoke(fpProgram, "testGlobalFunctionPointerVar");
        Assert.assertEquals(returns.toString(), "f1");
    }

    @Test(description = "Test global function pointers that are defined with type")
    public void testGlobalFunctionPointerTyped() {
        Object returns = BRunUtil.invoke(fpProgram, "testGlobalFunctionPointerTyped");
        Assert.assertEquals(returns.toString(), "f2");
    }

    @Test(description = "Test passing a no-return function pointer as a nil-returning function pointer")
    public void testVoidFunctionAsUnionReturnFunction() {
        Object returns = BRunUtil.invoke(fpProgram, "testVoidFunctionAsUnionReturnFunction");
        Assert.assertEquals(returns.toString(), "value - updated through lambda");
    }

    @Test(description = "Test function pointers defaultable params")
    public void testDefaultParams() {
        BRunUtil.invoke(fpProgram, "testDefaultParams");
    }

    @Test(description = "Test compile time errors for redeclared symbols")
    public void testRedeclaredSymbolsNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/fps_hiding_block_scope_symbols.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "redeclared symbol 'y'", 3, 26);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'y'", 11, 55);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'z'", 11, 58);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'y'", 34, 13);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'a'", 42, 32);
        BAssertUtil.validateError(result, i++, "redeclared symbol 'a'", 47, 32);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        fpProgram = null;
        closureFPProgram = null;
        privateFPProgram = null;
        globalProgram = null;
        structProgram = null;
    }
}
