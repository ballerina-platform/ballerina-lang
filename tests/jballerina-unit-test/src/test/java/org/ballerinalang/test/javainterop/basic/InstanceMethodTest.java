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

import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BHandleValue;
import org.ballerinalang.model.values.BValue;
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
        // TODO ....... WHY NULL return HERERRRRR......?????*^#&*^#*&$^#*&^

        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
        Assert.assertEquals(testIns.getCounter(), new Integer(1));
    }

    @Test(description = "Test invoking a java instance function that accepts and return nothing")
    public void testVoidWithThrows() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);

        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnVoidThrows", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0].getType() instanceof BErrorType);
        Assert.assertEquals(((BError) returns[0]).getReason(), "java.lang.InterruptedException");
    }

    @Test(description = "Test invoking a java instance function that accepts and return nothing")
    public void testInteropFunctionWithDifferentName() {
        InstanceMethods testIns = new InstanceMethods();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(testIns);
        BValue[] returns = BRunUtil.invoke(result, "testInteropFunctionWithDifferentName", args);
        // TODO ....... WHY NULL return HERERRRRR......?????*^#&*^#*&$^#*&^
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
}
