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
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Primitive divide expression test.
 */
public class DivideExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/expressions/divide-expr.bal");
    }

    @Test(description = "Test two int divide expression")
    public void testIntDivideExpr() {
        BValue[] args = { new BInteger(2000), new BInteger(50) };
        BValue[] returns = Functions.invoke(bFile, "intDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 40;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two int divide expression", expectedExceptions = BallerinaException.class)
    public void testIntDivideByZeroExpr() {
        BValue[] args = { new BInteger(2000), new BInteger(0) };
        Functions.invoke(bFile, "intDivide", args);
    }

//    @Test(description = "Test two long divide expression")
    public void testLongDivideExpr() {
        BValue[] args = { new BLong(100), new BLong(50) };
        BValue[] returns = Functions.invoke(bFile, "longDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);

        long actual = ((BLong) returns[0]).longValue();
        long expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float divide expression")
    public void testFloatDivideExpr() {
        BValue[] args = { new BFloat(300.0f), new BFloat(150.0f) };
        BValue[] returns = Functions.invoke(bFile, "floatDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        float actual = ((BFloat) returns[0]).floatValue();
        float expected = 2.0f;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float divide expression", expectedExceptions = BallerinaException.class)
    public void testFloatDivideByZeroExpr() {
        BValue[] args = { new BFloat(300.0f), new BFloat(0) };
        Functions.invoke(bFile, "floatDivide", args);
    }

//    @Test(description = "Test two double divide expression")
    public void testDoubleDivideExpr() {
        BValue[] args = { new BDouble(8), new BDouble(2) };
        BValue[] returns = Functions.invoke(bFile, "doubleDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);

        double actual = ((BDouble) returns[0]).doubleValue();
        double expected = 4;
        Assert.assertEquals(actual, expected);
    }
    
    /*
     * Negative tests
     */
    
    @Test(description = "Test dividing values of two types",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "divide-incompatible-types.bal:5: incompatible types in " +
                    "binary expression: string vs float")
    public void testAddIncompatibleTypes() {
        ParserUtils.parseBalFile("lang/expressions/divide-incompatible-types.bal");
    }
    
    @Test(description = "Test dividing values of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Divide operation is not supported for type: json in " +
            "divide-unsupported-types.bal:10")
    public void testAddUnsupportedTypes() {
        ParserUtils.parseBalFile("lang/expressions/divide-unsupported-types.bal");
    }
}
