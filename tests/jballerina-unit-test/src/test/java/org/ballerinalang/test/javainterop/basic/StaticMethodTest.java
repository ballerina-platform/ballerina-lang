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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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

    @Test(dataProvider = "nullReturnFunctions")
    public void testReturnNothing(String funcName) {
        Object returns = BRunUtil.invoke(result, funcName);
        Assert.assertNull(returns);
    }

    @DataProvider(name = "nullReturnFunctions")
    public Object[] getNullReturnFunctions() {
        return new Object[][]{
                {"testAcceptNothingAndReturnNothing"}, {"testInteropFunctionWithDifferentName"}
        };
    }

    @Test
    public void testErrorOrTupleReturn() {
        Object val = BRunUtil.invoke(result, "testErrorOrTupleReturn");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertNull(returns.get(0));
        Assert.assertNull(returns.get(1));
    }

    @Test(description = "Test invoking a java static function that accepts nothing and returns a Date")
    public void testAcceptNothingButReturnDate() {
        Object returns = BRunUtil.invoke(result, "testAcceptNothingButReturnDate");
        Assert.assertTrue(((HandleValue) returns).getValue() instanceof Date);
    }

    @Test(description = "Test invoking a java static function that accepts nothing and returns a string")
    public void testAcceptNothingButReturnString() {
        Object returns = BRunUtil.invoke(result, "testAcceptNothingButReturnString");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "hello world");
    }

    @Test
    public void testStringParamAndReturn() {
        Object[] args = new Object[1];
        args[0] = StringUtils.fromString("Royce");
        Object returns = BRunUtil.invoke(result, "stringParamAndReturn", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Royce and Hadrian");
    }

    @Test(description = "Test invoking a java static function that accepts and returns a Date")
    public void testAcceptSomethingAndReturnSomething() {
        Object[] args = new Object[1];
        Date argValue = new Date();
        args[0] = new HandleValue(argValue);
        Object returns = BRunUtil.invoke(result, "testAcceptSomethingAndReturnSomething", args);
        Assert.assertTrue(((HandleValue) returns).getValue() instanceof Date);
        Assert.assertEquals(((HandleValue) returns).getValue(), argValue);
    }

    @Test(description = "Test static java method that accepts two parameters")
    public void testJavaInteropFunctionThatAcceptsTwoParameters() {
        Object[] args = new Object[2];
        args[0] = new HandleValue("1");
        args[1] = new HandleValue("2");
        Object returns = BRunUtil.invoke(result, "testAcceptTwoParamsAndReturnSomething", args);
        Assert.assertEquals(((HandleValue) returns).getValue(), "12");
    }

    @Test(description = "Test static java method that accepts three parameters")
    public void testJavaInteropFunctionThatAcceptsThreeParameters() {
        Object[] args = new Object[3];
        args[0] = new HandleValue(1);
        args[1] = new HandleValue(2);
        args[2] = new HandleValue(3);
        Object returns = BRunUtil.invoke(result, "testAcceptThreeParamsAndReturnSomething", args);
        
        Assert.assertEquals(((HandleValue) returns).getValue(), 6);
    }

    @Test(description = "Test static java method that returns error value as objects")
    public void testReturnObjectValueOrError() {
        Object returns = BRunUtil.invoke(result, "getObjectOrError");
        
        Assert.assertEquals(((BError) returns).getMessage(), "some reason");
    }

    @Test(description = "Test static java method that returns error value or MapValue")
    public void testMapValueOrErrorReturn() {
        Object returns = BRunUtil.invoke(result, "testUnionReturn");
        Assert.assertEquals(returns.toString(),
                "{\"resources\":[{\"path\":\"basePath\",\"method\":\"Method string\"}]}");
    }

    @Test
    public void testFuncWithAsyncDefaultParamExpression() {
        Object returns = BRunUtil.invoke(result, "testFuncWithAsyncDefaultParamExpression");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 145L);
    }

    @Test
    public void testUsingParamValues() {
        Object returns = BRunUtil.invoke(result, "testUsingParamValues");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 290L);
    }

    @Test
    public void testDecimalParamAndReturn() {
        Object[] args = new Object[1];
        args[0] = ValueCreator.createDecimalValue("100");
        Object returns = BRunUtil.invoke(result, "testDecimalParamAndReturn", args);
        Assert.assertTrue(returns instanceof BDecimal);
        Assert.assertEquals(returns.toString(), "199.7");
    }

    @Test(expectedExceptions = BLangTestException.class,
          expectedExceptionsMessageRegExp = ".*Invalid update of record field: modification not allowed on readonly " +
                  "value.*")
    public void testCreateRawDetails() {
        BRunUtil.invoke(result, "testCreateRawDetails");
    }

    @Test(expectedExceptions = BLangTestException.class,
          expectedExceptionsMessageRegExp = ".*Invalid update of record field: modification not allowed on readonly " +
                  "value.*")
    public void testCreateDetails() {
        BRunUtil.invoke(result, "testCreateDetails");
    }

    @Test(dataProvider = "functionNamesProvider")
    public void testInvokeFunctions(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "functionNamesProvider")
    public Object[] getFunctionNames() {
        return new String[]{"testBalEnvSlowAsyncVoidSig", "testBalEnvFastAsyncVoidSig", "testBalEnvSlowAsync",
                "testBalEnvFastAsync", "testReturnNullString", "testReturnNotNullString", "testStaticResolve",
                "testStringCast", "testGetCurrentModule", "testGetDefaultValueWithBEnv", "testCreateStudentUsingType",
                "testCreateStudent", "testDefaultDecimalArgs", "testDefaultDecimalArgsAddition",
                "testJavaNullPointerException", "testBalEnvAcceptingMethodRetType", "testBundleFuncArgsToBArray"};
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
