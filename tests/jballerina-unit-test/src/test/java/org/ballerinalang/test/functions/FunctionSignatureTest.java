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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        pkgResult = BCompileUtil.compile(this, "test-src/functions/TestProj", "a.b");
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
    public void testInvokeFunctionInMixOrder1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFunctionInMixOrder1");
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
    public void testInvokeFunctionInMixOrder2() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFunctionInMixOrder2");
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
    public void testInvokeFunctionWithAllParamsAndRestArgs() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFunctionWithAllParamsAndRestArgs");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John1");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 6);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe1");

        Assert.assertTrue(returns[5] instanceof BValueArray);
        Assert.assertEquals(returns[5].stringValue(), "[40, 50, 60]");
    }

    @Test
    public void testInvokeFuncWithoutRestParamsAndMissingDefaultableParam() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeFuncWithoutRestParamsAndMissingDefaultableParam");
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

        Assert.assertTrue(returns[5] instanceof BValueArray);
        Assert.assertEquals(returns[5].stringValue(), "[1, 2, 3, 4]");
    }

    @Test
    public void testInvokePkgFunctionInMixOrder() {
        BValue[] returns = BRunUtil.invoke(pkgResult, "testInvokePkgFunctionInMixOrder");
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
    public void testInvokePkgFunctionInOrderWithRestParams() {
        BValue[] returns = BRunUtil.invoke(pkgResult, "testInvokePkgFunctionInOrderWithRestParams");
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
        Assert.assertEquals(returns[5].stringValue(), "[40, 50, 60]");
    }

    @Test
    public void testInvokePkgFunctionWithRequiredArgsOnly() {
        BValue[] returns = BRunUtil.invoke(pkgResult, "testInvokePkgFunctionWithRequiredArgsOnly");
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

    @Test()
    public void testOptionalArgsInNativeFunc() {
        CompileResult result = BCompileUtil.compile(this, "test-src/functions/TestProj", "foo.bar");
        BValue[] returns = BRunUtil.invoke(result, "testOptionalArgsInNativeFunc");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 78);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 89.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 5);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe");
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
        Assert.assertEquals(returns[1].stringValue(), "inner default world");
    }

    public static ArrayValue mockedNativeFuncWithOptionalParams(long a, double b,
                                                                org.ballerinalang.jvm.api.values.BString c, long d,
                                                                org.ballerinalang.jvm.api.values.BString e) {
        BTupleType tupleType = new BTupleType(
                Arrays.asList(BTypes.typeInt, BTypes.typeFloat, BTypes.typeString, BTypes.typeInt, BTypes.typeString));
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
}
