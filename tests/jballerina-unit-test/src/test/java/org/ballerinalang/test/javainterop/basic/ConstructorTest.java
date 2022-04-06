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

import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.nativeimpl.jvm.tests.ClassWithDefaultConstructor;
import org.ballerinalang.nativeimpl.jvm.tests.ClassWithOneParamConstructor;
import org.ballerinalang.nativeimpl.jvm.tests.ClassWithTwoParamConstructor;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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

    @Test(description = "Test invoking a java constructor that accepts nothing", dataProvider =
            "AcceptNothingFunctionNamesProvider")
    public void testAcceptNothing(String funcName) {
        Object returns = BRunUtil.invoke(result, funcName);

        Assert.assertEquals(((HandleValue) returns).getValue().getClass(), ClassWithDefaultConstructor.class);
        ClassWithDefaultConstructor createdClass =
                (ClassWithDefaultConstructor) ((HandleValue) returns).getValue();
        Assert.assertEquals((long) createdClass.getValue(), 11L);
    }

    @Test(description = "Test invoking a java constructor that accepts one parameter", dataProvider =
            "AcceptOneParamFunctionNamesProvider")
    public void testAcceptOneParam(String funcName) {
        Object[] args = new Object[1];
        args[0] = new HandleValue("Ballerina");
        Object returns = BRunUtil.invoke(result, funcName, args);
        ClassWithOneParamConstructor createdClass =
                (ClassWithOneParamConstructor) ((HandleValue) returns).getValue();
        Assert.assertEquals(createdClass.getValue(), "Hello Ballerina");
    }

    @Test(description = "Test invoking a java constructor that accepts two parameters", dataProvider =
            "AcceptTwoParamsFunctionNamesProvider")
    public void testAcceptTowParam(String funcName) {
        Object[] args = new Object[2];
        args[0] = new HandleValue("Bye ");
        args[1] = new HandleValue("Ballerina");
        Object returns = BRunUtil.invoke(result, funcName, args);
        
        ClassWithTwoParamConstructor createdClass =
                (ClassWithTwoParamConstructor) ((HandleValue) returns).getValue();
        Assert.assertEquals(createdClass.getValue(), "Bye Ballerina");
    }

    @DataProvider(name = "AcceptNothingFunctionNamesProvider")
    public Object[] getAcceptNothingFunctionNames() {
        return new String[]{"testDefaultConstructor", "testDefaultConstructorForClass"};
    }

    @DataProvider(name = "AcceptOneParamFunctionNamesProvider")
    public Object[] getAcceptOneParamFunctionNames() {
        return new String[]{"testOneParamConstructor", "testOneParamConstructorForClass"};
    }

    @DataProvider(name = "AcceptTwoParamsFunctionNamesProvider")
    public Object[] getAcceptTwoParamsFunctionNames() {
        return new String[]{"testTwoParamConstructor", "testTwoParamConstructorForClass"};
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
