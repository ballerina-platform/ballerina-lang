/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.bala.functions;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test function signatures and calling with optional and named params.
 * 
 * @since 0.975.0
 */
public class FunctionSignatureInBalaTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/functions/test_different_function_signatures.bal");
        resultNegative = BCompileUtil
                .compile("test-src/bala/test_bala/functions/test_different_function_signatures_negative.bal");
    }

    @Test
    public void testInvokeFunctionInOrder1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFunctionInOrder1");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testInvokeFunctionInOrder2() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFunctionInOrder2");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testInvokeFunctionWithoutRestArgs() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFunctionWithoutRestArgs");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");

        Assert.assertTrue(returns[5] instanceof BValueArray);
        Assert.assertEquals(returns[5].stringValue(), "[]");
    }

    @Test
    public void testInvokeFunctionWithoutSomeNamedArgs() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFunctionWithoutSomeNamedArgs");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 5);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe");

        Assert.assertTrue(returns[5] instanceof BValueArray);
        Assert.assertEquals(returns[5].stringValue(), "[]");
    }

    @Test
    public void testInvokeFunctionWithRequiredArgsOnly() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFunctionWithRequiredArgsOnly");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 5);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe");

        Assert.assertTrue(returns[5] instanceof BValueArray);
        Assert.assertEquals(returns[5].stringValue(), "[]");
    }

    @Test
    public void testInvokeFunctionWithAnyFunctionTypeParam() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyFunction");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(returns[1] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testInvokeFuncWithoutRestParams() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithoutRestParams");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testInvokeFuncWithOnlyNamedParams1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyNamedParams1");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testInvokeFuncWithOnlyNamedParams2() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyNamedParams2");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 6.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testInvokeFuncWithOnlyNamedParams3() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyNamedParams3");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 6.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 7);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe");
    }

    @Test
    public void testInvokeFuncWithOnlyRestParam1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyRestParam1");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[]");
    }

    @Test
    public void testInvokeFuncWithOnlyRestParam2() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyRestParam2");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[10, 20, 30]");
    }

    @Test
    public void testInvokeFuncWithOnlyRestParam3() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyRestParam3");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[10, 20, 30]");
    }

    @Test
    public void testInvokeFuncWithAnyRestParam1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithAnyRestParam1");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[10, 20, 30]");
    }

    @Test
    public void funcInvocAsRestArgs() {
        BValue[] returns = BRunUtil.invoke(result, "funcInvocAsRestArgs");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testFuncWithUnionTypedDefaultParam() {
        BValue[] returns = BRunUtil.invoke(result, "testFuncWithUnionTypedDefaultParam");
        Assert.assertEquals(returns[0].stringValue(), "John");
    }

    @Test
    public void testFuncWithNilDefaultParamExpr() {
        BValue[] returns = BRunUtil.invoke(result, "testFuncWithNilDefaultParamExpr");
        Assert.assertNull(returns[0]);
        Assert.assertNull(returns[1]);
    }

    @Test
    public void testAttachedFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testAttachedFunction");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 110);
    }

    @Test(description = "Test object function with defaultableParam")
    public void defaultValueForObjectFunctionParam() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultableParamInnerFunc");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 60);
        Assert.assertEquals(returns[1].stringValue(), "hello world");
    }

    @Test(description = "Test object outer function with defaultable param")
    public void defaultValueForObjectOuterFunctionParam() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultableParamOuterFunc");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
        Assert.assertEquals(returns[1].stringValue(), "hello world");
    }

    @Test
    public void testInvocationWithArgVarargMix() {
        BRunUtil.invoke(result, "testInvocationWithArgVarargMix");
    }

    @Test
    public void testNegativeFunctionInvocations() {
        int i = 0;
        validateError(resultNegative, i++, "missing required parameter 'b' in call to 'functionWithAllTypesParams()'",
                4, 12);
        validateError(resultNegative, i++, "positional argument not allowed after named arguments", 4, 56);
        validateError(resultNegative, i++, "positional argument not allowed after named arguments", 4, 72);
        validateError(resultNegative, i++, "positional argument not allowed after named arguments", 4, 76);
        validateError(resultNegative, i++, "missing required parameter 'b' in call to 'functionWithAllTypesParams()'",
                9, 12);
        validateError(resultNegative, i++, "positional argument not allowed after named arguments", 9, 56);
        validateError(resultNegative, i++, "rest argument not allowed after named arguments", 9, 72);
        validateError(resultNegative, i++, "arguments not allowed after rest argument", 9, 82);
        validateError(resultNegative, i++, "rest argument not allowed after named arguments", 13, 78);
        validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 17, 53);
        validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 17, 61);

        validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 24, 16);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean[]', found '[int,boolean," +
                "boolean]'", 24, 30);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'string'", 26, 28);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'int'", 28, 31);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'string'", 28, 34);
        validateError(resultNegative, i++, "variable assignment is required", 30, 5);
        validateError(resultNegative, i++, "incompatible types: expected '([int,boolean...]|record {| int i; |})', " +
                "found '[float,string...]'", 30, 16);
        validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 33, 13);
        validateError(resultNegative, i++, "incompatible types: expected '([float,boolean...]|record {| float f?; |})" +
                "', found '[int,boolean,boolean]'", 33, 19);
        validateError(resultNegative, i++, "incompatible types: expected '([string,float,boolean...]|record {| string" +
                " s; float f?; |})', found '[float,string...]'", 35, 16);
        validateError(resultNegative, i++, "rest argument not allowed after named arguments", 41, 24);
        validateError(resultNegative, i++, "incompatible types: expected '([float,boolean...]|record {| float f?; |})" +
                "', found 'boolean[]'", 43, 27);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'float'", 49, 24);

        validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 58, 16);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean[]', found '[int,boolean," +
                "boolean]'", 58, 28);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'string'", 60, 26);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'int'", 62, 29);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'string'", 62, 32);
        validateError(resultNegative, i++, "variable assignment is required", 64, 5);
        validateError(resultNegative, i++, "incompatible types: expected '([int,boolean...]|record {| int i; |})', " +
                "found '[float,string...]'", 64, 14);
        validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 67, 12);
        validateError(resultNegative, i++, "incompatible types: expected '([float,boolean...]|record {| float f?; |})" +
                "', found '[int,boolean,boolean]'", 67, 18);
        validateError(resultNegative, i++, "incompatible types: expected '([string,float,boolean...]|record {| string" +
                " s; float f?; |})', found '[float,string...]'", 69, 15);
        validateError(resultNegative, i++, "rest argument not allowed after named arguments", 77, 22);
        validateError(resultNegative, i++, "incompatible types: expected '([float,boolean...]|record {| float f?; |})" +
                "', found 'boolean[]'", 79, 26);
        validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'float'", 85, 22);

        Assert.assertEquals(i,  resultNegative.getErrorCount());
    }
}
