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

import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.nativeimpl.jvm.tests.ClassWithDefaultConstructor;
import org.ballerinalang.nativeimpl.jvm.tests.ClassWithOneParamConstructor;
import org.ballerinalang.nativeimpl.jvm.tests.ClassWithTwoParamConstructor;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for java interop constructor invocations.
 *
 * @since 1.0.0
 */
public class ConstructorTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/basic/constructor_tests.bal");
    }

    @Test(description = "Test invoking a java constructor that accepts nothing")
    public void testAcceptNothing() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultConstructor");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue().getClass(), ClassWithDefaultConstructor.class);
        ClassWithDefaultConstructor createdClass =
                (ClassWithDefaultConstructor) ((BHandleValue) returns[0]).getValue();
        Assert.assertEquals(createdClass.getValue().intValue(), 11);
    }

    @Test(description = "Test invoking a java constructor that accepts one parameter")
    public void testAcceptOneParam() {
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue("Ballerina");
        BValue[] returns = BRunUtil.invoke(result, "testOneParamConstructor", args);
        Assert.assertEquals(returns.length, 1);
        ClassWithOneParamConstructor createdClass =
                (ClassWithOneParamConstructor) ((BHandleValue) returns[0]).getValue();
        Assert.assertEquals(createdClass.getValue(), "Hello Ballerina");
    }

    @Test(description = "Test invoking a java constructor that accepts two parameters")
    public void testAcceptTowParam() {
        BValue[] args = new BValue[2];
        args[0] = new BHandleValue("Bye ");
        args[1] = new BHandleValue("Ballerina");
        BValue[] returns = BRunUtil.invoke(result, "testTwoParamConstructor", args);
        Assert.assertEquals(returns.length, 1);
        ClassWithTwoParamConstructor createdClass =
                (ClassWithTwoParamConstructor) ((BHandleValue) returns[0]).getValue();
        Assert.assertEquals(createdClass.getValue(), "Bye Ballerina");
    }
}
