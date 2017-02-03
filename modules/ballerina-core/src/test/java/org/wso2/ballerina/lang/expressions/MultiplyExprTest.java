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
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Primitive multiply expression test.
 */
public class MultiplyExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/expressions/mult-expr.bal");
    }

    @Test(description = "Test two int multiply expression")
    public void testIntMultiplyExpr() {
        BValue[] args = { new BInteger(100), new BInteger(50) };
        BValue[] returns = Functions.invoke(bFile, "intMultiply", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 5000;
        Assert.assertEquals(actual, expected);
    }

//    @Test(description = "Test two long multiply expression")
    public void testLongMultiplyExpr() {
        BValue[] args = { new BLong(10), new BLong(50) };
        BValue[] returns = Functions.invoke(bFile, "longMultiply", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);

        long actual = ((BLong) returns[0]).longValue();
        long expected = 500;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float multiply expression")
    public void testFloatMultiplyExpr() {
        BValue[] args = { new BFloat(40.0f), new BFloat(40.0f) };
        BValue[] returns = Functions.invoke(bFile, "floatMultiply", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        float actual = ((BFloat) returns[0]).floatValue();
        float expected = 1600.0f;
        Assert.assertEquals(actual, expected);
    }

//    @Test(description = "Test two double multiply expression")
    public void testDoubleMultiplyExpr() {
        BValue[] args = { new BDouble(8), new BDouble(2) };
        BValue[] returns = Functions.invoke(bFile, "doubleMultiply", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);

        double actual = ((BDouble) returns[0]).doubleValue();
        double expected = 16;
        Assert.assertEquals(actual, expected);
    }
    
    /*
     * Negative tests
     */
    
//    @Test(description = "Test multiplying values of two types",
//            expectedExceptions = {SemanticException.class },
//            expectedExceptionsMessageRegExp = "multiply-incompatible-types.bal:5: incompatible " +
//                    "types in binary expression: float vs int")
//    public void testAddIncompatibleTypes() {
//        ParserUtils.parseBalFile("lang/expressions/multiply-incompatible-types.bal");
//    }
    
    @Test(description = "Test multiplying values of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Multiply operation is not supported for type: json in " +
            "multiply-unsupported-types.bal:10")
    public void testMultiplyUnsupportedTypes() {
        ParserUtils.parseBalFile("lang/expressions/multiply-unsupported-types.bal");
    }
}
