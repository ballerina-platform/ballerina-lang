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
package org.ballerinalang.test.functions;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test function signatures and calling with optional and named params.
 */
public class FunctionSignatureTest {
    CompileResult result;
    CompileResult pkgResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/functions/different-function-signatures.bal");
        pkgResult = BCompileUtil.compile("test-src/functions/testproj");
    }

    @Test
    public void testInvokeFunctionInOrder1() {
        Object arr = BRunUtil.invoke(result, "testInvokeFunctionInOrder1");
        BArray returns = (BArray) arr;
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
        Object arr = BRunUtil.invoke(result, "testInvokeFunctionInOrder2");
        BArray returns = (BArray) arr;
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
    public void testInvokeFunctionInMixOrder1() {
        Object arr = BRunUtil.invoke(result, "testInvokeFunctionInMixOrder1");
        BArray returns = (BArray) arr;
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
    public void testInvokeFunctionInMixOrder2() {
        Object arr = BRunUtil.invoke(result, "testInvokeFunctionInMixOrder2");
        BArray returns = (BArray) arr;
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
    public void testInvokeFunctionWithoutSomeNamedArgs() {
        Object arr = BRunUtil.invoke(result, "testInvokeFunctionWithoutSomeNamedArgs");
        BArray returns = (BArray) arr;
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
        Object arr = BRunUtil.invoke(result, "testInvokeFunctionWithRequiredArgsOnly");
        BArray returns = (BArray) arr;
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

        Assert.assertTrue(returns.get(5) instanceof BArray);
        Assert.assertEquals(returns.get(5).toString(), "[]");
    }

    @Test
    public void testInvokeFunctionWithAllParamsAndRestArgs() {
        Object arr = BRunUtil.invoke(result, "testInvokeFunctionWithAllParamsAndRestArgs");
        BArray returns = (BArray) arr;
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 20.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "John1");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 6L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Doe1");

        Assert.assertTrue(returns.get(5) instanceof  BArray);
        Assert.assertEquals(returns.get(5).toString(), "[40,50,60]");
    }

    @Test
    public void testInvokeFuncWithoutRestParamsAndMissingDefaultableParam() {
        Object arr = BRunUtil.invoke(result, "testInvokeFuncWithoutRestParamsAndMissingDefaultableParam");
        BArray returns = (BArray) arr;
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
        Object arr = BRunUtil.invoke(result, "testInvokeFuncWithOnlyNamedParams1");
        BArray returns = (BArray) arr;
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
        Object arr = BRunUtil.invoke(result, "testInvokeFuncWithOnlyNamedParams2");
        BArray returns = (BArray) arr;
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
        Object arr = BRunUtil.invoke(result, "testInvokeFuncWithOnlyNamedParams3");
        BArray returns = (BArray) arr;
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
        Object returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyRestParam1");
        Assert.assertTrue(returns instanceof  BArray);
        Assert.assertEquals(returns.toString(), "[]");
    }

    @Test
    public void testInvokeFuncWithOnlyRestParam2() {
        Object returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyRestParam2");
        Assert.assertTrue(returns instanceof  BArray);
        Assert.assertEquals(returns.toString(), "[10,20,30]");
    }

    @Test
    public void testInvokeFuncWithOnlyRestParam3() {
        Object returns = BRunUtil.invoke(result, "testInvokeFuncWithOnlyRestParam3");
        Assert.assertTrue(returns instanceof  BArray);
        Assert.assertEquals(returns.toString(), "[10,20,30]");
    }

    @Test
    public void testInvokeFuncWithAnyRestParam1() {
        Object returns = BRunUtil.invoke(result, "testInvokeFuncWithAnyRestParam1");
        Assert.assertTrue(returns instanceof  BArray);
        Assert.assertEquals(((BArray) returns).get(0).toString(), "[10,20,30]");
    }

    @Test
    public void funcInvocAsRestArgs() {
        Object arr = BRunUtil.invoke(result, "funcInvocAsRestArgs");
        BArray returns = (BArray) arr;
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

        Assert.assertTrue(returns.get(5) instanceof  BArray);
        Assert.assertEquals(returns.get(5).toString(), "[1,2,3,4]");
    }

    @Test
    public void testInvokePkgFunctionInMixOrder() {
        Object arr = BRunUtil.invoke(pkgResult, "testInvokePkgFunctionInMixOrder");
        BArray returns = (BArray) arr;
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
    public void testInvokePkgFunctionInOrderWithRestParams() {
        Object arr = BRunUtil.invoke(pkgResult, "testInvokePkgFunctionInOrderWithRestParams");
        BArray returns = (BArray) arr;
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

        Assert.assertTrue(returns.get(5) instanceof  BArray);
        Assert.assertEquals(returns.get(5).toString(), "[40,50,60]");
    }

    @Test
    public void testInvokePkgFunctionWithRequiredArgsOnly() {
        Object arr = BRunUtil.invoke(pkgResult, "testInvokePkgFunctionWithRequiredArgsOnly");
        BArray returns = (BArray) arr;
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

    @Test()
    public void testOptionalArgsInNativeFunc() {
        CompileResult result = BCompileUtil.compile("test-src/functions/testproj");
        Object arr = BRunUtil.invoke(result, "testOptionalArgsInNativeFunc");
        BArray returns = (BArray) arr;

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 78L);

        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 89.0);

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "John");

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 5L);

        Assert.assertTrue(returns.get(4) instanceof BString);
        Assert.assertEquals(returns.get(4).toString(), "Doe");
    }

    @Test
    public void testFuncWithUnionTypedDefaultParam() {
        Object returns = BRunUtil.invoke(result, "testFuncWithUnionTypedDefaultParam");
        Assert.assertEquals(returns.toString(), "John");
    }

    @Test
    public void testFuncWithNilDefaultParamExpr() {
        Object arr = BRunUtil.invoke(result, "testFuncWithNilDefaultParamExpr");
        BArray returns = (BArray) arr;
        Assert.assertNull(returns.get(0));
        Assert.assertNull(returns.get(1));
    }

    @Test
    public void testAttachedFunction() {
        Object arr = BRunUtil.invoke(result, "testAttachedFunction");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0), 100L);
        Assert.assertEquals(returns.get(1), 110L);
    }

    @Test(description = "Test object function with defaultableParam")
    public void defaultValueForObjectFunctionParam() {
        Object arr = BRunUtil.invoke(result, "testDefaultableParamInnerFunc");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 60L);
        Assert.assertEquals(returns.get(1).toString(), "inner default world");
    }

    public static ArrayValue mockedNativeFuncWithOptionalParams(long a, double b,
                                                                io.ballerina.runtime.api.values.BString c, long d,
                                                                io.ballerina.runtime.api.values.BString e) {
        BTupleType tupleType = new BTupleType(
                Arrays.asList(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_FLOAT, PredefinedTypes.TYPE_STRING,
                              PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_STRING));
        ArrayValue tuple = new TupleValueImpl(tupleType);
        tuple.add(0, Long.valueOf(a));
        tuple.add(1, Double.valueOf(b));
        tuple.add(2, c);
        tuple.add(3, Long.valueOf(d));
        tuple.add(4, e);
        return tuple;
    }

    @Test(description = "Test1: function signature which has a function typed param with only rest param")
    public void testFunctionWithFunctionTypedParamWithOnlyRestParam1() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithRest1");
    }

    @Test(description = "Test2: function signature which has a function typed param with only rest param")
    public void testFunctionWithFunctionTypedParamWithOnlyRestParam2() {
        BRunUtil.invoke(result, "testFunctionOfFunctionTypedParamWithRest2");
    }

    @Test(description = "Test function with actual never return")
    public void testFuncWithActualNeverReturn() {
        BRunUtil.invoke(result, "testFuncWithActualNeverReturn");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        pkgResult = null;
    }
}
