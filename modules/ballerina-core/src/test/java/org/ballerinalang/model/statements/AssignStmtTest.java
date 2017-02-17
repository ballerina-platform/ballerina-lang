/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Assign statement test.
 */
public class AssignStmtTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/statements/assignment/assign-stmt.bal");
    }

    @Test(description = "Test successful assignment")
    public void testIntAssignmentStatement() {
        // Int assignment test
        BValue[] args = { new BInteger(100) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testIntAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 100;
        Assert.assertEquals(actual, expected);

        // Long assignment test
        args = new BValue[] { new BLong(100) };
        returns = BLangFunctions.invoke(bLangProgram, "testLongAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);

        long actualLong = ((BLong) returns[0]).longValue();
        long expectedLong = 100;
        Assert.assertEquals(actualLong, expectedLong);

        // Float assignment test
        args = new BValue[] { new BFloat(2.3f) };
        returns = BLangFunctions.invoke(bLangProgram, "testFloatAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        float actualFloat = ((BFloat) returns[0]).floatValue();
        float expectedFloat = 2.3f;
        Assert.assertEquals(actualFloat, expectedFloat);

        // Double assignment test
        args = new BValue[] { new BDouble(1234.1234) };
        returns = BLangFunctions.invoke(bLangProgram, "testDoubleAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);

        double actualDouble = ((BDouble) returns[0]).doubleValue();
        double expectedDouble = 1234.1234;
        Assert.assertEquals(actualDouble, expectedDouble);

        // Boolean assignment test
        args = new BValue[] { new BBoolean(true) };
        returns = BLangFunctions.invoke(bLangProgram, "testBooleanAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        boolean actualBoolean = ((BBoolean) returns[0]).booleanValue();
        Assert.assertEquals(actualBoolean, true);

        // String assignment test
        args = new BValue[] { new BString("Test Value") };
        returns = BLangFunctions.invoke(bLangProgram, "testStringAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actualString = returns[0].stringValue();
        String expectedString = "Test Value";
        Assert.assertEquals(actualString, expectedString);

        // Array index to int assignment test
        BArray<BInteger> bIntegerBArray = new BArray<>(BInteger.class);
        bIntegerBArray.add(0, new BInteger(150));
        args = new BValue[] { bIntegerBArray };
        returns = BLangFunctions.invoke(bLangProgram, "testArrayIndexToIntAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 150;
        Assert.assertEquals(actual, expected);

        // Array index to int assignment test
        args = new BValue[] { new BInteger(250) };
        returns = BLangFunctions.invoke(bLangProgram, "testIntToArrayAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 250;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test successful assignment")
    public void testAssignmentStmtWithMultiReturnFunc() {
        // Int assignment test
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testMultiReturn", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(5, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("john", ((BString) returns[1]).stringValue());
        Assert.assertEquals(6, ((BInteger) returns[2]).intValue());
    }
    
    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "constant-assignment.bal:6: cannot assign a value to constant 'a'")
    public void testAssignmentToConst() {
        BTestUtils.parseBalFile("lang/expressions/constant-assignment.bal");
    }
}
