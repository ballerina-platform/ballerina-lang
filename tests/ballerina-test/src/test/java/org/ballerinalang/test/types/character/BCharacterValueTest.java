/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.character;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BCharArray;
import org.ballerinalang.model.values.BCharacter;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTypeValue;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class will test the behaviour of character values.
 * <p>
 * Defining a character value
 * char c;
 * c = 'D';
 *
 * @since 0.964
 */

public class BCharacterValueTest {
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/character/character-value.bal");
    }

    @Test(description = "Test character value assignment")
    public void testCharacterValue() {
        BValue[] returns = BRunUtil.invoke(result, "testCharValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharacter.class);
        BCharacter charValue = (BCharacter) returns[0];
        Assert.assertEquals(charValue.charValue(), 'D', "Invalid character value returned.");
    }

    @Test(description = "Test character function parameter")
    public void testCharacterParameter() {
        invokeCharacterInputFunction("testCharParam");
    }

    @Test(description = "Test global character value assignment")
    public void testGlobalCharacter() {
        invokeCharacterInputFunction("testGlobalChar");
    }

    private void invokeCharacterInputFunction(String functionName) {
        char input = 'D';
        BValue[] args = {new BCharacter(input)};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharacter.class);
        BCharacter charValue = (BCharacter) returns[0];
        Assert.assertEquals(charValue.charValue(), input, "Invalid character value returned.");
    }

    @Test(description = "Test character to integer cast")
    public void testCharacterToIntCast() {
        char input = 'D';
        BValue[] args = {new BCharacter(input)};
        BValue[] returns = BRunUtil.invoke(result, "testCharToIntCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), (int) input, "Invalid integer value returned.");
    }

    @Test(description = "Test integer to character cast")
    public void testIntToCharacterCast() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToCharExplicitCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharacter.class);
        BCharacter bCharacter = (BCharacter) returns[0];
        Assert.assertEquals(bCharacter.charValue(), (char) input, "Invalid character value returned.");
    }

    @Test(description = "Test integer to character explicit cast")
    public void testIntToCharacterExplicitCast() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToCharCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharacter.class);
        BCharacter bCharacter = (BCharacter) returns[0];
        Assert.assertEquals(bCharacter.charValue(), (char) input, "Invalid character value returned.");
    }

    @Test(description = "Test character to float cast")
    public void testCharacterToFloatCast() {
        char input = 'H';
        BValue[] args = {new BCharacter(input)};
        BValue[] returns = BRunUtil.invoke(result, "testCharToFloatCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat bFloat = (BFloat) returns[0];
        Assert.assertEquals(bFloat.floatValue(), (double) input, "Invalid float value returned.");
    }

    @Test(description = "Test character to any cast")
    public void testCharacterToAnyCast() {
        char input = 'D';
        BValue[] args = {new BCharacter(input)};
        BValue[] returns = BRunUtil.invoke(result, "testCharToAnyCast", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);
        Assert.assertSame(returns[1].getClass(), BCharacter.class);
        BCharacter bCharacter = (BCharacter) returns[1];
        Assert.assertEquals(bCharacter.charValue(), input, "Invalid character value returned.");
    }


    @Test(description = "Test character array to any cast")
    public void testCharacterArrayToAnyCast() {
        char input1 = 'D';
        char input2 = 'E';

        BCharArray bCharArrayIn = new BCharArray();
        bCharArrayIn.add(0, input1);
        bCharArrayIn.add(1, input2);
        BValue[] args = {bCharArrayIn};

        BValue[] returns = BRunUtil.invoke(result, "testCharArrayToAny", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);
        Assert.assertSame(returns[1].getClass(), BCharArray.class);

        BCharArray bCharArrayOut = (BCharArray) returns[1];
        Assert.assertSame((char) bCharArrayOut.get(0), input1);
        Assert.assertSame((char) bCharArrayOut.get(1), input2);
    }

    @Test(description = "Test integer to character conversion")
    public void testIntToCharConversion() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToCharConversion", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharacter.class);
        BCharacter bCharacter = (BCharacter) returns[0];
        Assert.assertEquals(bCharacter.charValue(), (char) input, "Invalid character value returned.");
    }

    @Test(description = "Test character to integer conversion")
    public void testCharToIntConversion() {
        char input = 'D';
        BValue[] args = {new BCharacter(input)};
        BValue[] returns = BRunUtil.invoke(result, "testCharToIntConversion", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), (long) input, "Invalid integer value returned.");
    }

    @Test(description = "Test float to character conversion")
    public void testFloatToCharConversion() {
        float input = 12.3f;
        BValue[] args = {new BFloat(input)};
        BValue[] returns = BRunUtil.invoke(result, "testFloatToCharConversion", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharacter.class);
        BCharacter bCharacter = (BCharacter) returns[0];
        Assert.assertEquals(bCharacter.charValue(), (char) input, "Invalid character value returned.");
    }

    @Test(description = "Test float to character conversion")
    public void testCharToFloatConversion() {
        char input = 'D';
        BValue[] args = {new BCharacter(input)};
        BValue[] returns = BRunUtil.invoke(result, "testCharToFloatConversion", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat bFloat = (BFloat) returns[0];
        Assert.assertEquals(bFloat.floatValue(), (double) input, "Invalid float value returned.");
    }

    @Test(description = "Test character array value")
    public void testCharacterArrayValue() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testCharArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharArray.class);
    }

    @Test(description = "Test character array assignment")
    public void testCharacterArrayAssignment() {
        char input1 = 'D';
        char input2 = 'E';
        char input3 = '3';
        char input4 = 'j';

        BCharArray bCharArrayIn = new BCharArray();
        bCharArrayIn.add(0, input1);
        bCharArrayIn.add(1, input2);
        bCharArrayIn.add(2, input3);
        bCharArrayIn.add(3, input4);
        BValue[] args = {bCharArrayIn};

        BValue[] returns = BRunUtil.invoke(result, "testCharArrayAssignment", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharArray.class);
        BCharArray bCharArrayOut = (BCharArray) returns[0];

        Assert.assertSame((char) bCharArrayOut.get(0), input1);
        Assert.assertSame((char) bCharArrayOut.get(1), input2);
        Assert.assertSame((char) bCharArrayOut.get(2), input3);
        Assert.assertSame((char) bCharArrayOut.get(3), input4);
    }


    @Test(description = "Test character array length")
    public void testCharacterArrayLength() {
        invokeArrayLengthFunction("testCharArrayLength", 4);
    }

    @Test(description = "Test character array zero length")
    public void testCharacterArrayZeroLength() {
        invokeArrayLengthFunction("testCharArrayZeroLength", 0);
    }

    private void invokeArrayLengthFunction(String functionName, int length) {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(length, bInteger.intValue(), "Invalid array size");
    }

    @Test(description = "Test character array size increase")
    public void testCharacterArrayIncreaseSize() {
        invokeArrayLengthFunction("testCharArrayIncreaseSize", 10);
    }

    @Test(description = "Test character array of array")
    public void testCharacterArrayOfArray() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testCharArrayOfArray", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        BInteger bInteger1 = (BInteger) returns[1];
        Assert.assertEquals(3, bInteger.intValue(), "Invalid array size");
        Assert.assertEquals(4, bInteger1.intValue(), "Invalid array size");
    }

    @Test(description = "Test character equal operation")
    public void testCharacterEqual() {
        BValue[] args = {new BCharacter('A'), new BCharacter('B'), new BCharacter('A')};
        BValue[] returns = BRunUtil.invoke(result, "testCharBinaryOperation", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        BBoolean boolean1 = (BBoolean) returns[0];
        BBoolean boolean2 = (BBoolean) returns[1];
        Assert.assertFalse(boolean1.booleanValue(), "Invalid result");
        Assert.assertTrue(boolean2.booleanValue(), "Invalid result");
    }

    @Test(description = "Test character not equal operation")
    public void testCharacterNotEqual() {
        BValue[] args = {new BCharacter('A'), new BCharacter('B'), new BCharacter('A')};
        BValue[] returns = BRunUtil.invoke(result, "testCharBinaryNotEqualOperation", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        BBoolean boolean1 = (BBoolean) returns[0];
        BBoolean boolean2 = (BBoolean) returns[1];
        Assert.assertTrue(boolean1.booleanValue(), "Invalid result");
        Assert.assertFalse(boolean2.booleanValue(), "Invalid result");
    }


    @Test
    public void simpleWorkerMessagePassingTest() {
        BRunUtil.invoke(result, "testWorkerWithCharVariable", new BValue[0]);
    }

    @Test(description = "Test string to character array")
    public void testStringToCharacterArray() {
        String input = "Hello Ballerina!";
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(result, "testStringToCharArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BCharArray.class);

        BCharArray bCharArray = (BCharArray) returns[0];
        for (int i = 0; i < input.length(); i++) {
            Assert.assertEquals((char) bCharArray.get(i), input.charAt(i), "Invalid character");
        }
    }
}
