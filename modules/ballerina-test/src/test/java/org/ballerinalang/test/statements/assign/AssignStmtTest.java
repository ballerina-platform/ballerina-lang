/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.ifelse;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AssignStmtTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("org/ballerinalang/test/statements/assign/assign-stmt.bal");
        resultNegative = BTestUtils.compile("org/ballerinalang/test/statements/assign/assign-stmt-negative.bal");
    }

    @Test
    public void invokeAssignmentTest() {
        BValue[] args = { new BInteger(100) };
        BValue[] returns = BTestUtils.invoke(result.getProgFile(), "testIntAssignStmt", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 100;
        Assert.assertEquals(actual, expected);

        // Float assignment test
        args = new BValue[] { new BFloat(2.3f) };
        returns = BLangFunctions.invokeNew(result.getProgFile(), "testFloatAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actualFloat = ((BFloat) returns[0]).floatValue();
        double expectedFloat = 2.3f;
        Assert.assertEquals(actualFloat, expectedFloat);

        // Boolean assignment test
        args = new BValue[] { new BBoolean(true) };
        returns = BLangFunctions.invokeNew(result.getProgFile(), "testBooleanAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        boolean actualBoolean = ((BBoolean) returns[0]).booleanValue();
        Assert.assertEquals(actualBoolean, true);

        // String assignment test
        args = new BValue[] { new BString("Test Value") };
        returns = BLangFunctions.invokeNew(result.getProgFile(), "testStringAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actualString = returns[0].stringValue();
        String expectedString = "Test Value";
        Assert.assertEquals(actualString, expectedString);

        // Array index to int assignment test
        BIntArray arrayValue = new BIntArray();
        arrayValue.add(0, 150);
        args = new BValue[] { arrayValue };
        returns = BLangFunctions.invokeNew(result.getProgFile(), "testArrayIndexToIntAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 150;
        Assert.assertEquals(actual, expected);

        // Int to array index assignment test
        args = new BValue[] { new BInteger(250) };
        returns = BLangFunctions.invokeNew(result.getProgFile(), "testIntToArrayAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 250;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test assignment statement with multi return function")
    public void testAssignmentStmtWithMultiReturnFunc() {
        // Int assignment test
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(result.getProgFile(), "testMultiReturn", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(5, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("john", returns[1].stringValue());
        Assert.assertEquals(6, ((BInteger) returns[2]).intValue());
    }

    @Test(description = "Test assignment statement with errors")
    public void testAssignmentNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 7);
        //testIncompatibleTypeAssign
        BTestUtils.validateError(resultNegative, 0, "incompatible types: expected 'boolean', found 'int'", 3, 8);
        //testAssignCountMismatch1
        BTestUtils.validateError(resultNegative, 1, "assignment count mismatch: 2 != 3", 11, 14);
        //testAssignCountMismatch2
        BTestUtils.validateError(resultNegative, 2, "assignment count mismatch: 4 != 3", 21, 20);
        //testAssignTypeMismatch1
        BTestUtils.validateError(resultNegative, 3, "incompatible types: expected 'int', found 'string'", 30, 16);
        BTestUtils.validateError(resultNegative, 4, "incompatible types: expected 'string', found 'int'", 35, 11);
        //testAssignTypeMismatch2
        BTestUtils.validateError(resultNegative, 5, "incompatible types: expected 'int', found 'string'", 43, 17);
        BTestUtils.validateError(resultNegative, 6, "incompatible types: expected 'string', found 'int'", 44, 14);


    }
}
