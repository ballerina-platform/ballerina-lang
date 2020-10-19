/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.javainterop.basic;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.values.MapValueImpl;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Test cases for java interop static function invocations.
 *
 * @since 1.0.0
 */
public class StaticMethodTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/basic/static_method_tests.bal");
    }

    @Test(description = "Test invoking a java static function that accepts and return nothing")
    public void testAcceptNothingAndReturnNothing() {
        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnNothing");



        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test invoking a java static function that accepts and return nothing")
    public void testInteropFunctionWithDifferentName() {
        BValue[] returns = BRunUtil.invoke(result, "testInteropFunctionWithDifferentName");



        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test invoking a java static function that accepts nothing and returns a Date")
    public void testAcceptNothingButReturnDate() {
        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingButReturnDate");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BHandleValue) returns[0]).getValue() instanceof Date);
    }

    @Test(description = "Test invoking a java static function that accepts nothing and returns a string")
    public void testAcceptNothingButReturnString() {
        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingButReturnString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "hello world");
    }

    @Test
    public void testStringParamAndReturn() {
        BValue[] args = new BValue[1];
        args[0] = new BString("Royce");
        BValue[] returns = BRunUtil.invoke(result, "stringParamAndReturn", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Royce and Hadrian");
    }

    @Test(description = "Test invoking a java static function that accepts and returns a Date")
    public void testAcceptSomethingAndReturnSomething() {
        BValue[] args = new BValue[1];
        Date argValue = new Date();
        args[0] = new BHandleValue(argValue);
        BValue[] returns = BRunUtil.invoke(result, "testAcceptSomethingAndReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BHandleValue) returns[0]).getValue() instanceof Date);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), argValue);
    }

    @Test(description = "Test static java method that accepts two parameters")
    public void testJavaInteropFunctionThatAcceptsTwoParameters() {
        BValue[] args = new BValue[2];
        args[0] = new BHandleValue("1");
        args[1] = new BHandleValue("2");
        BValue[] returns = BRunUtil.invoke(result, "testAcceptTwoParamsAndReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), "12");
    }

    @Test(description = "Test static java method that accepts three parameters")
    public void testJavaInteropFunctionThatAcceptsThreeParameters() {
        BValue[] args = new BValue[3];
        args[0] = new BHandleValue(1);
        args[1] = new BHandleValue(2);
        args[2] = new BHandleValue(3);
        BValue[] returns = BRunUtil.invoke(result, "testAcceptThreeParamsAndReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), 6);
    }

    @Test(description = "Test static java method that returns error value as objects")
    public void testReturnObjectValueOrError() {
        BValue[] returns = BRunUtil.invoke(result, "getObjectOrError");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BError) returns[0]).getReason(), "some reason");
    }

    @Test(description = "Test static java method that returns error value or MapValue")
    public void testMapValueOrErrorReturn() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionReturn");
        Assert.assertEquals(returns[0].stringValue(),
                "{\"resources\":[{\"path\":\"basePath\",\"method\":\"Method string\"}]}");

    }

    public static Object returnObjectOrError() {
        return ErrorCreator.createError(StringUtils.fromString("some reason"),
                                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    @Test(description = "Test tuple return with null values")
    public void testTupleReturn() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorOrTupleReturn");
        Assert.assertEquals(returns.length, 2);
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testFuncWithAsyncDefaultParamExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testFuncWithAsyncDefaultParamExpression");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 145);
    }

    @Test
    public void testUsingParamValues() {
        BValue[] returns = BRunUtil.invoke(result, "testUsingParamValues");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 290);
    }

    @Test
    public void testDecimalParamAndReturn() {
        BValue[] args = new BValue[1];
        args[0] = new BDecimal("100");
        BValue[] returns = BRunUtil.invoke(result, "testDecimalParamAndReturn", args);
        Assert.assertTrue(returns[0] instanceof BDecimal);
        Assert.assertEquals(returns[0].stringValue(), "199.7");
    }

    @Test
    public void testBalEnvSlowAsyncVoidSig() {
        BRunUtil.invoke(result, "testBalEnvSlowAsyncVoidSig");
    }

    @Test
    public void testBalEnvFastAsyncVoidSig() {
        BRunUtil.invoke(result, "testBalEnvFastAsyncVoidSig");
    }

    @Test
    public void testBalEnvSlowAsync() {
        BRunUtil.invoke(result, "testBalEnvSlowAsync");
    }

    @Test
    public void testBalEnvFastAsync() {
        BRunUtil.invoke(result, "testBalEnvFastAsync");
    }

    @Test(description = "When instance and static methods have the same name resolve static method based on the " +
            "parameter type")
    public void testStaticResolve() {
        BRunUtil.invoke(result, "testStaticResolve");
    }
}
