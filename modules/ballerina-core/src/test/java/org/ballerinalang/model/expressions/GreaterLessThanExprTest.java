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

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Greater than/ less than expression test..
 */
public class GreaterLessThanExprTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/expressions/greater-less-than-expr.bal");
    }

    @Test(description = "Test int greater than, less than expression")
    public void testIntRangeExpr() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIntRanges", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BInteger(50)};
        returns = BLangFunctions.invokeNew(programFile, "testIntRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BInteger(200)};
        returns = BLangFunctions.invokeNew(programFile, "testIntRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test float greater than, less than expression")
    public void testFloatRangeExpr() {
        BValue[] args = {new BFloat(-123.8f)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testFloatRanges", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BFloat(75.4f)};
        returns = BLangFunctions.invokeNew(programFile, "testFloatRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BFloat(321.45f)};
        returns = BLangFunctions.invokeNew(programFile, "testFloatRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test Integer and long comparison")
    public void testIntAndFloatComparison() {
        int a = 10;
        float b = 20f;

        boolean expectedResult = a > b;

        BValue[] args = {new BInteger(a), new BFloat(b)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIntAndFloatCompare", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        boolean actualResult = ((BBoolean) returns[0]).booleanValue();

        Assert.assertEquals(actualResult, expectedResult);
    }
    
    /*
     * Negative tests
     */

    @Test(description = "Test greater-than check for two different types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "lang[/\\\\]expressions[/\\\\]btype[/\\\\]incompatible[/\\\\]gt[/\\\\]"
                    + "incompatible-type-greater-than.bal:6: invalid operation: incompatible types 'int' and 'string'")
    public void testIncompatibleGreaterThan() {
        BTestUtils.getProgramFile("lang/expressions/btype/incompatible/gt");
    }

    @Test(description = "Test greater-than-equal check for two different types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "lang[/\\\\]expressions[/\\\\]btype[/\\\\]incompatible[/\\\\]gte[/\\\\]"
                    + "incompatible-type-greater-than-equal.bal:6: invalid operation: "
                    + "incompatible types 'int' and 'string'")
    public void testIncompatibleGreaterThanEqual() {
        BTestUtils.getProgramFile("lang/expressions/btype/incompatible/gte");
    }

    @Test(description = "Test less-than check for two different types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp =
                  "lang[/\\\\]expressions[/\\\\]btype[/\\\\]incompatible[/\\\\]lt[/\\\\]"
                          + "incompatible-type-less-than.bal:6: invalid operation: "
                          + "incompatible types 'int' and 'string'")
    public void testIncompatibleLessThan() {
        BTestUtils.getProgramFile("lang/expressions/btype/incompatible/lt");
    }

    @Test(description = "Test less-than-equal check for two different types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "lang[/\\\\]expressions[/\\\\]btype[/\\\\]incompatible[/\\\\]lte[/\\\\]"
                    + "incompatible-type-less-than-equal.bal:6: "
                    + "invalid operation: incompatible types 'int' and 'string'")
    public void testIncompatibleLessThanEqual() {
        BTestUtils.getProgramFile("lang/expressions/btype/incompatible/lte");
    }
    
    @Test(description = "Test less-than check for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                  "lang[/\\\\]expressions[/\\\\]btype[/\\\\]unsupported[/\\\\]lt[/\\\\]" +
                  "unsupported-type-less-than.bal:9: invalid operation: operator < not defined on 'json'")
    public void testUnsupportedTypeLessThan() {
        BTestUtils.getProgramFile("lang/expressions/btype/unsupported/lt");
    }
    
    @Test(description = "Test greater-than check for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                  "lang[/\\\\]expressions[/\\\\]btype[/\\\\]unsupported[/\\\\]gt[/\\\\]" +
                  "unsupported-type-greater-than.bal:9: invalid operation: operator > not defined on 'json'")
    public void testUnsupportedTypeGreaterThan() {
        BTestUtils.getProgramFile("lang/expressions/btype/unsupported/gt");
    }
    
    @Test(description = "Test greater-than-equal check for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                    "lang[/\\\\]expressions[/\\\\]btype[/\\\\]unsupported[/\\\\]gte[/\\\\]" +
                    "unsupported-type-greater-than-equal.bal:9: invalid operation: operator >= not defined on 'json'")
    public void testUnsupportedTypeGreaterThanEqual() {
        BTestUtils.getProgramFile("lang/expressions/btype/unsupported/gte");
    }
    
    @Test(description = "Test less-than-equal check for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                  "lang[/\\\\]expressions[/\\\\]btype[/\\\\]unsupported[/\\\\]lte[/\\\\]" +
                  "unsupported-type-less-than-equal.bal:9: invalid operation: operator <= not defined on 'json'")
    public void testUnsupportedTypeLessThanEqual() {
        BTestUtils.getProgramFile("lang/expressions/btype/unsupported/lte");
    }
}
