/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.any;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains methods to test the any type implementation success scenarios.
 *
 * @since 0.85
 */
public class BAnyTypeSuccessScenariosTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/any/any-type-success.bal");
    }

    @Test(description = "Test any type as a return value with actual json returning")
    public void testAnyReturnWithJson() {
        Object returns = BRunUtil.invoke(result, "jsonReturnTest", new Object[0]);
        
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"PropertyName\":\"Value\"}", "Invalid json value returned.");
    }

    @Test(description = "Test any type as a return value with actual table returning")
    public void testAnyReturnWithTable() {
        BRunUtil.invoke(result, "tableReturnTestAsAny");
    }

    @Test(description = "Test any type as a return value with actual table returning")
    public void testInputAnyAsTable() {
        BRunUtil.invoke(result, "inputAnyAsTableTest");
    }

    @Test(description = "Test any type as a parameter for function and explicit casting")
    public void testAnyAsParameterForFunction() {
        Object returns = BRunUtil.invoke(result, "anyMethodParameter");

        Assert.assertSame(returns.getClass(), Long.class);
        long intReturn = (long) returns;
        Assert.assertEquals(intReturn, 9, "Invalid integer value returned.");
    }

    @Test(description = "Test any type as a struct parameter with boolean value")
    public void testAnyAsStructParam() {
        Object returns = BRunUtil.invoke(result, "anyInStructTest", new Object[0]);

        Assert.assertSame(returns.getClass(), Boolean.class);
        boolean bBoolean = (boolean) returns;
        Assert.assertEquals(bBoolean, true, "Invalid boolean value returned.");
    }

    @Test(description = "Test float value in any type get casted to int in two steps")
    public void testFloatInAnyCastToInt() {
        Object returns = BRunUtil.invoke(result, "successfulIntCasting", new Object[0]);

        Assert.assertSame(returns.getClass(), Long.class);
        long intVal = (long) returns;
        Assert.assertEquals(intVal, 5, "Invalid int value returned.");
    }

    @Test(description = "Test any to any explicit cast")
    public void testAnyToAnyCast() {
        Object returns = BRunUtil.invoke(result, "anyToAnyExplicitCasting", new Object[0]);

        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"PropertyName\":\"Value\"}", "Invalid json value returned.");
    }

    @Test(description = "Test Multiple returns with any")
    public void testMultipleReturnWithAny() {
        BArray returns = (BArray) BRunUtil.invoke(result, "multipleReturnWithAny", new Object[0]);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertSame(returns.get(1).getClass(), Long.class);
        long intVal = (long) returns.get(1);
        Assert.assertEquals(returns.get(0).toString(), "{\"PropertyName\":\"Value\"}", "Invalid json value returned.");
        Assert.assertEquals(intVal, 7, "Invalid int value returned.");
    }

    @Test(description = "Test multiple params with any")
    public void testMultipleParamWithAny() {
        Object returns = BRunUtil.invoke(result, "multipleParamWithAny", new Object[0]);

        Assert.assertSame(returns.getClass(), Long.class);
        long intVal = (long) returns;
        Assert.assertEquals(intVal, 5, "Invalid int value returned.");
    }

    @Test(description = "Test variable init with any")
    public void variableDefTest() {
        Object returns = BRunUtil.invoke(result, "variableDefTest", new Object[0]);

        Assert.assertSame(returns.getClass(), Long.class);
        long intVal = (long) returns;
        Assert.assertEquals(intVal, 5, "Invalid int value returned.");
    }

    @Test(description = "Test any variable assignment with float")
    public void assignmentTest() {
        Object returns = BRunUtil.invoke(result, "assignmentTest", new Object[0]);

        Assert.assertSame(returns.getClass(), Double.class);
        double floatVal = (double) returns;
        Assert.assertEquals(floatVal, 44.3d, "Invalid float value returned.");
    }

    @Test(description = "Test any type as a struct parameter with boolean value")
    public void testAnyArrayWithMapArray() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "anyArrayWithMapArray", args);

        Assert.assertTrue(returns instanceof BArray);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
