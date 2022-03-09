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
package org.ballerinalang.test.javainterop.primitivetypes;

import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains test cases to test Java interop functions accepting Java primitive values as parameters.
 *
 * @since 1.0.0
 */
public class PrimitiveConversionsInFunctionParamsTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(
                "test-src/javainterop/primitive_types/primitive_conversion_in_function_params.bal");
    }

    @Test(description = "Test functions that pass ballerina byte to Java primitive types")
    public void testCreateJBoxedValuesFromBByte() {
        byte byteValue = 100;
        Object[] args = new Object[1];
        args[0] = (byteValue);

        Object returns = BRunUtil.invoke(result, "testCreateJShortFromBByte", args);
        Short aShort = (Short) ((HandleValue) returns).getValue();
        Assert.assertEquals(aShort.byteValue(), byteValue);

        returns = BRunUtil.invoke(result, "testCreateJCharacterFromBByte", args);
        Character character = (Character) ((HandleValue) returns).getValue();
        Assert.assertEquals((byte) character.charValue(), byteValue);

        returns = BRunUtil.invoke(result, "testCreateJIntegerFromBByte", args);
        Integer integer = (Integer) ((HandleValue) returns).getValue();
        Assert.assertEquals(integer.byteValue(), byteValue);

        returns = BRunUtil.invoke(result, "testCreateJLongFromBByte", args);
        Long longValue = (Long) ((HandleValue) returns).getValue();
        Assert.assertEquals(longValue.byteValue(), byteValue);

        returns = BRunUtil.invoke(result, "testCreateJFloatFromBByte", args);
        Float floatValue = (Float) ((HandleValue) returns).getValue();
        Assert.assertEquals(floatValue, (float) byteValue);

        returns = BRunUtil.invoke(result, "testCreateJDoubleFromBByte", args);
        Double doubleValue = (Double) ((HandleValue) returns).getValue();
        Assert.assertEquals(doubleValue, (double) byteValue);
    }

    @Test(description = "Test functions that pass ballerina int to Java primitive types")
    public void testCreateJBoxedValuesFromBInt() {
        long value = 4;
        Object[] args = new Object[1];
        args[0] = (value);

        Object returns = BRunUtil.invoke(result, "testCreateJByteFromBInt", args);
        Byte aByte = (Byte) ((HandleValue) returns).getValue();
        Assert.assertEquals(aByte.longValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJShortFromBInt", args);
        Short aShort = (Short) ((HandleValue) returns).getValue();
        Assert.assertEquals(aShort.longValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJCharacterFromBInt", args);
        Character character = (Character) ((HandleValue) returns).getValue();
        Assert.assertEquals(character.charValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJIntFromBInt", args);
        Integer integer = (Integer) ((HandleValue) returns).getValue();
        Assert.assertEquals(integer.longValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJFloatFromBInt", args);
        Float floatValue = (Float) ((HandleValue) returns).getValue();
        Assert.assertEquals(floatValue.longValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJDoubleFromBInt", args);
        Double doubleValue = (Double) ((HandleValue) returns).getValue();
        Assert.assertEquals(doubleValue.longValue(), value);
    }

    @Test(description = "Test functions that pass ballerina float to Java primitive types")
    public void testCreateJBoxedValuesFromBFloat() {
        double value = 4.0;
        Object[] args = new Object[1];
        args[0] = (value);

        Object returns = BRunUtil.invoke(result, "testCreateJByteFromBFloat", args);
        Byte aByte = (Byte) ((HandleValue) returns).getValue();
        Assert.assertEquals(aByte.doubleValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJShortFromBFloat", args);
        Short aShort = (Short) ((HandleValue) returns).getValue();
        Assert.assertEquals(aShort.doubleValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJCharacterFromBFloat", args);
        Character character = (Character) ((HandleValue) returns).getValue();
        Assert.assertEquals((double) character, value);

        returns = BRunUtil.invoke(result, "testCreateJIntFromBFloat", args);
        Integer integer = (Integer) ((HandleValue) returns).getValue();
        Assert.assertEquals(integer.doubleValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJLongFromBFloat", args);
        Long aLong = (Long) ((HandleValue) returns).getValue();
        Assert.assertEquals(aLong.doubleValue(), value);

        returns = BRunUtil.invoke(result, "testCreateJFloatFromBFloat", args);
        Float floatValue = (Float) ((HandleValue) returns).getValue();
        Assert.assertEquals(floatValue.doubleValue(), value);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
