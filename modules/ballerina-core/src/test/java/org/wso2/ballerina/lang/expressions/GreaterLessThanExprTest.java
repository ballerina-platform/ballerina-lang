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

package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.BTestUtils;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * Greater than/ less than expression test..
 */
public class GreaterLessThanExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = BTestUtils.parseBalFile("lang/expressions/greater-less-than-expr.bal");
    }

    @Test(description = "Test int greater than, less than expression")
    public void testIntRangeExpr() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BLangFunctions.invoke(bFile, "testIntRanges", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BInteger(50)};
        returns = BLangFunctions.invoke(bFile, "testIntRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BInteger(200)};
        returns = BLangFunctions.invoke(bFile, "testIntRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test float greater than, less than expression")
    public void testFloatRangeExpr() {
        BValue[] args = {new BFloat(-123.8f)};
        BValue[] returns = BLangFunctions.invoke(bFile, "testFloatRanges", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BFloat(75.4f)};
        returns = BLangFunctions.invoke(bFile, "testFloatRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BFloat(321.45f)};
        returns = BLangFunctions.invoke(bFile, "testFloatRanges", args);

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
        BValue[] returns = BLangFunctions.invoke(bFile, "testIntAndFloatCompare", args);

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
            expectedExceptionsMessageRegExp = "incompatible-type-greater-than.bal:6: invalid operation: " +
                    "incompatible types 'int' and 'boolean'")
    public void testIncompatibleGreaterThan() {
        BTestUtils.parseBalFile("lang/expressions/incompatible-type-greater-than.bal");
    }

    @Test(description = "Test greater-than-equal check for two different types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "incompatible-type-greater-than-equal.bal:6: invalid operation: " +
                    "incompatible types 'int' and 'boolean'")
    public void testIncompatibleGreaterThanEqual() {
        BTestUtils.parseBalFile("lang/expressions/incompatible-type-greater-than-equal.bal");
    }

    @Test(description = "Test less-than check for two different types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "incompatible-type-less-than.bal:6: invalid operation: " +
                    "incompatible types 'int' and 'boolean'")
    public void testIncompatibleLessThan() {
        BTestUtils.parseBalFile("lang/expressions/incompatible-type-less-than.bal");
    }

    @Test(description = "Test less-than-equal check for two different types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "incompatible-type-less-than-equal.bal:6: invalid operation: " +
                    "incompatible types 'int' and 'boolean'")
    public void testIncompatibleLessThanEqual() {
        BTestUtils.parseBalFile("lang/expressions/incompatible-type-less-than-equal.bal");
    }
    
    @Test(description = "Test less-than check for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "unsupported-type-less-than.bal:9: invalid operation: " +
                    "operator < not defined on 'json'")
    public void testUnsupportedTypeLessThan() {
        BTestUtils.parseBalFile("lang/expressions/unsupported-type-less-than.bal");
    }
    
    @Test(description = "Test greater-than check for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "unsupported-type-greater-than.bal:9: invalid operation: " +
                    "operator > not defined on 'json'")
    public void testUnsupportedTypeGreaterThan() {
        BTestUtils.parseBalFile("lang/expressions/unsupported-type-greater-than.bal");
    }
    
    @Test(description = "Test greater-than-equal check for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "unsupported-type-greater-than-equal.bal:9: invalid operation: " +
                    "operator >= not defined on 'json'")
    public void testUnsupportedTypeGreaterThanEqual() {
        BTestUtils.parseBalFile("lang/expressions/unsupported-type-greater-than-equal.bal");
    }
    
    @Test(description = "Test less-than-equal check for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "unsupported-type-less-than-equal.bal:9: invalid operation: " +
                    "operator <= not defined on 'json'")
    public void testUnsupportedTypeLessThanEqual() {
        BTestUtils.parseBalFile("lang/expressions/unsupported-type-less-than-equal.bal");
    }
}
