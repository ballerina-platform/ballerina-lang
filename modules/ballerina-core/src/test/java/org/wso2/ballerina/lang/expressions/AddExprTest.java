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
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Primitive add expression test.
 */
public class AddExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/expressions/add-expr.bal");
    }

    @Test(description = "Test two int add expression")
    public void testIntAddExpr() {
        BValue[] args = { new BInteger(100), new BInteger(200)};

        BValue[] returns = Functions.invoke(bFile, "intAdd", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int actual = ((BInteger) returns[0]).intValue();
        int expected = 300;
        Assert.assertEquals(actual, expected);

        returns = Functions.invoke(bFile, "intSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        actual = ((BInteger) returns[0]).intValue();
        expected = -100;
        Assert.assertEquals(actual, expected);
    }

//    @Test(description = "Test two long add expression")
    public void testLongAddExpr() {
        BValue[] args = { new BLong(100), new BLong(200)};
        BValue[] returns = Functions.invoke(bFile, "longAdd", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);

        long actual = ((BLong) returns[0]).longValue();
        long expected = 300;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float add expression")
    public void testFloatAddExpr() {
        BValue[] args = { new BFloat(100.0f), new BFloat(200.0f)};

        BValue[] returns = Functions.invoke(bFile, "floatAdd", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        float actual = ((BFloat) returns[0]).floatValue();
        float expected = 300.0f;
        Assert.assertEquals(actual, expected);

        returns = Functions.invoke(bFile, "floatSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        actual = ((BFloat) returns[0]).floatValue();
        expected = -100.0f;
        Assert.assertEquals(actual, expected);
    }

//    @Test(description = "Test two double add expression")
    public void testDoubleAddExpr() {
        BValue[] args = { new BDouble(100), new BDouble(200)};
        BValue[] returns = Functions.invoke(bFile, "doubleAdd", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);

        double actual = ((BDouble) returns[0]).doubleValue();
        double expected = 300;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two string add expression")
    public void testStringAddExpr() {
        BValue[] args = { new BString("WSO2"), new BString(" Inc.")};
        BValue[] returns = Functions.invoke(bFile, "stringAdd", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "WSO2 Inc.";
        Assert.assertEquals(actual, expected);
    }
    
    
    /*
     * Negative tests
     */
    
    @Test(description = "Test adding values of two types",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "add-incompatible-types.bal:5: incompatible types " +
                    "in binary expression: int vs boolean")
    public void testAddIncompatibleTypes() {
        ParserUtils.parseBalFile("lang/expressions/add-incompatible-types.bal");
    }
    
    @Test(description = "Test adding values of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Add operation is not supported for type: json in " +
            "add-unsupported-types.bal:10")
    public void testAddUnsupportedTypes() {
        ParserUtils.parseBalFile("lang/expressions/add-unsupported-types.bal");
    }
}
