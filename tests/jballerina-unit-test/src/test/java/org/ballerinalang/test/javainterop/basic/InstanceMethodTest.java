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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BHandleType;
import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Test cases for java interop instance function invocations.
 *
 * @since 1.0.0
 */
public class InstanceMethodTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/basic/instance_method_tests.bal");
    }

    @Test(description = "Test invoking a java instance function that accepts and return nothing")
    public void testAcceptNothingAndReturnNothing() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);

        Object returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnNothing", args);

        Assert.assertNull(returns);
        Assert.assertEquals(testIns.getCounter(), new Integer(1));
    }

    @Test(description = "Test invoking a java instance function that accepts and return nothing but has a throws")
    public void testVoidWithThrows() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);

        Object returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnVoidThrows", args);

        Assert.assertNotNull(returns);
        Assert.assertTrue(getType(returns) instanceof BErrorType);
        Assert.assertEquals(((BError) returns).getMessage(), "java.lang.InterruptedException");

        returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnVoidThrowsReturn", args);
        Assert.assertNull(returns);
    }

    @Test(description = "Test invoking a java instance function with return type error|handle")
    public void handleOrErrorReturn() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);

        Object returns = BRunUtil.invoke(result, "testHandleOrErrorReturn", args);
        Assert.assertTrue(getType(returns) instanceof BHandleType);
        Assert.assertEquals(((HandleValue) returns).getValue(), 70);

        returns = BRunUtil.invoke(result, "testHandleOrErrorReturnThrows", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(getType(returns).getName(), "error");
        Assert.assertEquals(((BError) returns).getMessage(), "java.lang.InterruptedException");
    }

    @Test(description = "Test invoking a java instance function with return type error|handle and java Object return")
    public void handleOrErrorWithObjectReturn() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);

        Object returns = BRunUtil.invoke(result, "handleOrErrorWithObjectReturn", args);

        Object value = ((HandleValue) returns).getValue();
        Assert.assertEquals((int) value, 70);

        returns = BRunUtil.invoke(result, "handleOrErrorWithObjectReturnThrows", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(getType(returns).getName(), "error");
        Assert.assertEquals(((BError) returns).getMessage(), "java.lang.InterruptedException");
    }

    @Test(description = "Test invoking a java instance function with return type error|<primitive>")
    public void testPrimitiveOrErrorReturn() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);

        Object returns = BRunUtil.invoke(result, "testPrimitiveOrErrorReturn", args);

        Assert.assertEquals(getType(returns).getName(), "float");
        Assert.assertEquals(returns, 55.0);

        returns = BRunUtil.invoke(result, "testPrimitiveOrErrorReturnThrows", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(getType(returns).getName(), "error");
        Assert.assertEquals(((BError) returns).getMessage(), "java.lang.InterruptedException");
    }

    @Test(description = "Test invoking a java instance function with return type error|<union>")
    public void testUnionWithErrorReturn() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);

        Object returns = BRunUtil.invoke(result, "testUnionWithErrorReturnByte", args);

        Assert.assertEquals(getType(returns).getName(), "byte");
        Assert.assertEquals(((Integer) returns).byteValue(), (byte) '5');

        returns = BRunUtil.invoke(result, "testUnionWithErrorReturnThrows", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(getType(returns).getName(), "error");
        Assert.assertEquals(((BError) returns).getMessage(), "java.lang.InterruptedException");

        returns = BRunUtil.invoke(result, "testUnionWithErrorReturnHandle", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(getType(returns).getName(), "handle");
        Assert.assertEquals(((HandleValue) returns).getValue(), "handle ret");
    }

    @Test(description = "Test invoking a java instance function that accepts and return nothing")
    public void testInteropFunctionWithDifferentName() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);
        Object returns = BRunUtil.invoke(result, "testInteropFunctionWithDifferentName", args);

        Assert.assertNull(returns);
        Assert.assertEquals(testIns.getCounter(), new Integer(1));
    }

    @Test(description = "Test invoking a java instance function that accepts nothing and returns a something")
    public void testAcceptNothingButReturnSomething() {
        InstanceMethods testIns = new InstanceMethods();
        testIns.setCounterValue(21);
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);
        Object returns = BRunUtil.invoke(result, "testAcceptNothingButReturnSomething", args);

        Assert.assertEquals(((HandleValue) returns).getValue(), 21);
    }

    @Test(description = "Test invoking a java instance function that accepts something, but returns nothing")
    public void testAcceptSomethingButReturnNothing() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[2];
        args[0] = new HandleValue(testIns);
        args[1] = new HandleValue(22);
        Object returns = BRunUtil.invoke(result, "testAcceptSomethingButReturnNothing", args);

        Assert.assertNull(returns);
        Assert.assertEquals(testIns.getCounter(), new Integer(22));
    }

    @Test(description = "Test invoking a java instance function that accepts and returns a something")
    public void testAcceptSomethingAndReturnSomething() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[2];
        args[0] = new HandleValue(testIns);
        args[1] = new HandleValue(25);
        Object returns = BRunUtil.invoke(result, "testAcceptSomethingAndReturnSomething", args);

        Assert.assertEquals(((HandleValue) returns).getValue(), 25);
    }

    @Test(description = "Test instance java method that accepts two parameters")
    public void testAcceptTwoParamsAndReturnSomething() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[3];
        args[0] = new HandleValue(testIns);
        args[1] = new HandleValue(20);
        args[2] = new HandleValue(30);
        Object returns = BRunUtil.invoke(result, "testAcceptTwoParamsAndReturnSomething", args);

        Assert.assertEquals(((HandleValue) returns).getValue(), 50);
    }

    @Test(description = "Test content of a exception return")
    public void testErrorDetail() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);

        Object returns = BRunUtil.invoke(result, "errorDetail", args);

        Assert.assertEquals(getType(returns).getName(), "error");
        Assert.assertEquals(((BError) returns).getMessage(),
                "org.ballerinalang.nativeimpl.jvm.tests.JavaInteropTestCheckedException");
        Assert.assertEquals(((BMap) ((BError) returns).getDetails()).get(StringUtils.fromString("message")).toString(),
                "Custom error");
        BError cause = (BError) ((BMap) ((BError) returns).getDetails()).get(StringUtils.fromString("cause"));
        Assert.assertEquals(getType(cause).getName(), "error");
        Assert.assertEquals(cause.getMessage(), "java.lang.Throwable");
        Assert.assertEquals(((BMap) cause.getDetails()).get(StringUtils.fromString("message")).toString(),
                "Interop Throwable");
    }

    @Test(description = "Test content of a exception return")
    public void testUncheckedErrorDetail() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);

        try {
            BRunUtil.invoke(result, "uncheckedErrorDetail", args);
            Assert.fail("Unchecked exception not thrown");
        } catch (Throwable e) {
            Assert.assertTrue(e.getMessage().contains("error: java.lang.RuntimeException"));
            Assert.assertTrue(e.getMessage().contains("{\"message\":\"Unchecked Exception"));
            Assert.assertTrue(e.getMessage().contains("\"cause\":error(\"java.lang.Throwable\"," +
                    "message=\"Unchecked cause\""));
        }
    }

    @Test(description = "When instance and static methods have the same name resolve instance method based on the " +
            "parameter type")
    public void testInstanceResolve() {
        BRunUtil.invoke(result, "testInstanceResolve");
    }

    @Test
    public void testGetCurrentModule() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);
        BRunUtil.invoke(result, "testGetCurrentModule", args);
    }

    @Test
    public void testBalEnvAcceptingMethodRetType() {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);
        BRunUtil.invoke(result, "testBalEnvAcceptingMethodRetType", args);
    }

    @Test(dataProvider = "unionWithErrorTestFunctions")
    public void testUnionWithErrorReturnArrays(String function) {
        InstanceMethods testIns = new InstanceMethods();
        Object[] args = new Object[1];
        args[0] = new HandleValue(testIns);
        BRunUtil.invoke(result, function, args);
    }

    @DataProvider(name = "unionWithErrorTestFunctions")
    public Object[] unionWithErrorTestFunctions() {
        return new String[]{
                "testUnionWithErrorReturnByteArray",
                "testAnyOrErrorReturnStringArray"
        };
    }

    @Test
    public void testInteropCallToAbstractClassMethod() {
        BRunUtil.invoke(result, "testInteropCallToAbstractClassMethod");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
