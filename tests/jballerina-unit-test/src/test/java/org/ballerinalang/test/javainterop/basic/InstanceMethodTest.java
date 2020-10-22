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

import org.ballerinalang.core.model.types.BErrorType;
import org.ballerinalang.core.model.types.BHandleType;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnNothing", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
        Assert.assertEquals(testIns.getCounter(), new Integer(1));
    }

    @Test(description = "Test invoking a java instance function that accepts and return nothing but has a throws")
    public void testVoidWithThrows() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnVoidThrows", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0].getType() instanceof BErrorType);
        Assert.assertEquals(((BError) returns[0]).getReason(), "java.lang.InterruptedException");

        returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnVoidThrowsReturn", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test invoking a java instance function with return type error|handle")
    public void handleOrErrorReturn() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

        BValue[] returns = BRunUtil.invoke(result, "testHandleOrErrorReturn", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].getType() instanceof BHandleType);
        Assert.assertEquals(((Integer) ((BHandleValue) returns[0]).getValue()).intValue(), 70);

        returns = BRunUtil.invoke(result, "testHandleOrErrorReturnThrows", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].getType().getName(), "error");
        Assert.assertEquals(((BError) returns[0]).getReason(), "java.lang.InterruptedException");
    }

    @Test(description = "Test invoking a java instance function with return type error|handle and java Object return")
    public void handleOrErrorWithObjectReturn() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

        BValue[] returns = BRunUtil.invoke(result, "handleOrErrorWithObjectReturn", args);
        Assert.assertEquals(returns.length, 1);
        Object value = ((BHandleValue) returns[0]).getValue();
        Assert.assertEquals((int) value, 70);

        returns = BRunUtil.invoke(result, "handleOrErrorWithObjectReturnThrows", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].getType().getName(), "error");
        Assert.assertEquals(((BError) returns[0]).getReason(), "java.lang.InterruptedException");
    }

    @Test(description = "Test invoking a java instance function with return type error|<primitive>")
    public void testPrimitiveOrErrorReturn() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

        BValue[] returns = BRunUtil.invoke(result, "testPrimitiveOrErrorReturn", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getType().getName(), "float");
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 55.0);

        returns = BRunUtil.invoke(result, "testPrimitiveOrErrorReturnThrows", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].getType().getName(), "error");
        Assert.assertEquals(((BError) returns[0]).getReason(), "java.lang.InterruptedException");
    }

    @Test(description = "Test invoking a java instance function with return type error|<union>")
    public void testUnionWithErrorReturn() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

        BValue[] returns = BRunUtil.invoke(result, "testUnionWithErrorReturnByte", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getType().getName(), "byte");
        Assert.assertEquals(((BByte) returns[0]).byteValue(), '5');

        returns = BRunUtil.invoke(result, "testUnionWithErrorReturnThrows", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].getType().getName(), "error");
        Assert.assertEquals(((BError) returns[0]).getReason(), "java.lang.InterruptedException");

        returns = BRunUtil.invoke(result, "testUnionWithErrorReturnHandle", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].getType().getName(), "handle");
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), "handle ret");
    }

    @Test(description = "Test invoking a java instance function that accepts and return nothing")
    public void testInteropFunctionWithDifferentName() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);
        BValue[] returns = BRunUtil.invoke(result, "testInteropFunctionWithDifferentName", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
        Assert.assertEquals(testIns.getCounter(), new Integer(1));
    }

    @Test(description = "Test invoking a java instance function that accepts nothing and returns a something")
    public void testAcceptNothingButReturnSomething() {
        InstanceMethods testIns = new InstanceMethods();
        testIns.setCounterValue(21);
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);
        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingButReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), 21);
    }

    @Test(description = "Test invoking a java instance function that accepts something, but returns nothing")
    public void testAcceptSomethingButReturnNothing() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[2];
        args[0] = new BHandleValue(testIns);
        args[1] = new BHandleValue(22);
        BValue[] returns = BRunUtil.invoke(result, "testAcceptSomethingButReturnNothing", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
        Assert.assertEquals(testIns.getCounter(), new Integer(22));
    }


    @Test(description = "Test invoking a java instance function that accepts and returns a something")
    public void testAcceptSomethingAndReturnSomething() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[2];
        args[0] = new BHandleValue(testIns);
        args[1] = new BHandleValue(25);
        BValue[] returns = BRunUtil.invoke(result, "testAcceptSomethingAndReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), 25);
    }

    @Test(description = "Test instance java method that accepts two parameters")
    public void testAcceptTwoParamsAndReturnSomething() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[3];
        args[0] = new BHandleValue(testIns);
        args[1] = new BHandleValue(20);
        args[2] = new BHandleValue(30);
        BValue[] returns = BRunUtil.invoke(result, "testAcceptTwoParamsAndReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), 50);
    }

    @Test(description = "Test content of a exception return")
    public void testErrorDetail() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

        BValue[] returns = BRunUtil.invoke(result, "errorDetail", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getType().getName(), "error");
        Assert.assertEquals(((BError) returns[0]).getReason(),
                "org.ballerinalang.nativeimpl.jvm.tests.JavaInteropTestCheckedException");
        Assert.assertEquals(((BMap) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                "Custom error");
        BError cause = (BError) ((BMap) ((BError) returns[0]).getDetails()).get("cause");
        Assert.assertEquals(cause.getType().getName(), "error");
        Assert.assertEquals(cause.getReason(), "java.lang.Throwable");
        Assert.assertEquals(((BMap) cause.getDetails()).get("message").stringValue(), "Interop Throwable");
    }

    @Test(description = "Test content of a exception return")
    public void testUncheckedErrorDetail() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

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
}
