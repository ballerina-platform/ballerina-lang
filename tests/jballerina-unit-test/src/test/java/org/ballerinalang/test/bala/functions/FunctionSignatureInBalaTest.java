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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test function signatures and calling with optional and named params.
 * 
 * @since 0.975.0
 */
public class FunctionSignatureInBalaTest {

    private CompileResult compileResult;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_utils");
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_functions");
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        compileResult =
                BCompileUtil.compile("test-src/bala/test_bala/functions/test_different_function_signatures.bal");
        resultNegative = BCompileUtil
                .compile("test-src/bala/test_bala/functions/test_different_function_signatures_negative.bal");
    }

    @Test
    public void testInvokeFunctionInOrder1() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFunctionInOrder1");
        BArray returns = (BArray) result;
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Alex");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 30L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Bob");
    }

    @Test
    public void testInvokeFunctionInOrder2() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFunctionInOrder2");
        BArray returns = (BArray) result;
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Alex");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 30L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Bob");
    }

    @Test
    public void testInvokeFunctionWithoutRestArgs() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFunctionWithoutRestArgs");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Alex");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 30L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Bob");

        Assert.assertTrue(returns.get(5) instanceof BArray);
        Assert.assertEquals(returns.get(5).toString(), "[]");
    }

    @Test
    public void testInvokeFunctionWithoutSomeNamedArgs() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFunctionWithoutSomeNamedArgs");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Alex");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 5L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Doe");

        Assert.assertTrue(returns.get(5) instanceof  BArray);
        Assert.assertEquals(returns.get(5).toString(), "[]");
    }

    @Test
    public void testInvokeFunctionWithRequiredArgsOnly() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFunctionWithRequiredArgsOnly");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "John");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 5L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Doe");

        Assert.assertTrue(returns.get(5) instanceof  BArray);
        Assert.assertEquals(returns.get(5).toString(), "[]");
    }

    @Test
    public void testInvokeFunctionWithAnyFunctionTypeParam() {
        Object result = BRunUtil.invoke(compileResult, "testAnyFunction");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertFalse((Boolean) returns.get(1));
    }

    @Test
    public void testInvokeFuncWithoutRestParams() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFuncWithoutRestParams");
        BArray returns = (BArray) result;
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "John");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 30L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Bob");
    }

    @Test
    public void testInvokeFuncWithOnlyNamedParams1() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFuncWithOnlyNamedParams1");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Alex");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 30L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Bob");
    }

    @Test
    public void testInvokeFuncWithOnlyNamedParams2() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFuncWithOnlyNamedParams2");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 5L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 6.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Alex");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 30L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Bob");
    }

    @Test
    public void testInvokeFuncWithOnlyNamedParams3() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFuncWithOnlyNamedParams3");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 5L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 6.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "John");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 7L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Doe");
    }

    @Test
    public void testInvokeFuncWithOnlyRestParam1() {
        Object returns = BRunUtil.invoke(compileResult, "testInvokeFuncWithOnlyRestParam1");
        Assert.assertTrue(returns instanceof  BArray);
        Assert.assertEquals(returns.toString(), "[]");
    }

    @Test
    public void testInvokeFuncWithOnlyRestParam2() {
        Object returns = BRunUtil.invoke(compileResult, "testInvokeFuncWithOnlyRestParam2");
        Assert.assertTrue(returns instanceof  BArray);
        Assert.assertEquals(returns.toString(), "[10,20,30]");
    }

    @Test
    public void testInvokeFuncWithOnlyRestParam3() {
        Object returns = BRunUtil.invoke(compileResult, "testInvokeFuncWithOnlyRestParam3");
        Assert.assertTrue(returns instanceof  BArray);
        Assert.assertEquals(returns.toString(), "[10,20,30]");
    }

    @Test
    public void testInvokeFuncWithAnyRestParam1() {
        Object result = BRunUtil.invoke(compileResult, "testInvokeFuncWithAnyRestParam1");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof  BArray);
        Assert.assertEquals(returns.get(0).toString(), "[10,20,30]");
        Assert.assertTrue(returns.get(1) instanceof BMap);
        Assert.assertEquals(returns.get(1).toString(), "{\"name\":\"John\"}");
    }

    @Test
    public void funcInvocAsRestArgs() {
        Object result = BRunUtil.invoke(compileResult, "funcInvocAsRestArgs");
        BArray returns = (BArray) result;

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Alex");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 30L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Bob");
    }

    @Test
    public void testFuncWithUnionTypedDefaultParam() {
        Object returns = BRunUtil.invoke(compileResult, "testFuncWithUnionTypedDefaultParam");
        Assert.assertEquals(returns.toString(), "John");
    }

    @Test
    public void testFuncWithNilDefaultParamExpr() {
        Object result = BRunUtil.invoke(compileResult, "testFuncWithNilDefaultParamExpr");
        BArray returns = (BArray) result;
        Assert.assertNull(returns.get(0));
        Assert.assertNull(returns.get(1));
    }

    @Test
    public void testAttachedFunction() {
        Object result = BRunUtil.invoke(compileResult, "testAttachedFunction");
        BArray returns = (BArray) result;
        Assert.assertEquals(returns.get(0), 100L);
        Assert.assertEquals(returns.get(1), 110L);
    }

    @Test(description = "Test object function with defaultableParam")
    public void defaultValueForObjectFunctionParam() {
        Object result = BRunUtil.invoke(compileResult, "testDefaultableParamInnerFunc");
        BArray returns = (BArray) result;

        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 60L);
        Assert.assertEquals(returns.get(1).toString(), "hello world");
    }

    @Test(description = "Test object outer function with defaultable param")
    public void defaultValueForObjectOuterFunctionParam() {
        Object result = BRunUtil.invoke(compileResult, "testDefaultableParamOuterFunc");
        BArray returns = (BArray) result;

        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 50L);
        Assert.assertEquals(returns.get(1).toString(), "hello world");
    }

    @Test
    public void testInvocationWithArgVarargMix() {
        BRunUtil.invoke(compileResult, "testInvocationWithArgVarargMix");
    }

    @Test
    public void testCyclicFuncCallWhenFuncDefinedInModuleWithSameName() {
        BRunUtil.invoke(compileResult, "testCyclicFuncCallWhenFuncDefinedInModuleWithSameName");
    }

    @Test
    public void testFuncCallingFuncFromDifferentModuleAsParamDefault() {
        BRunUtil.invoke(compileResult, "testFuncCallingFuncFromDifferentModuleAsParamDefault");
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

    @AfterClass
    public void tearDown() {
        compileResult = null;
        resultNegative = null;
    }
}
