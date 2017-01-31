/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Array access expression test.
 *
 * @since 0.8.0
 */
public class ArrayAccessExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/expressions/array-access-expr.bal");
    }

    @Test(description = "Test array access expression")
    public void testArrayAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = Functions.invoke(bFile, "arrayAccessTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 210;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array return value")
    public void testArrayReturnValue() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = Functions.invoke(bFile, "arrayReturnTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BInteger> arrayValue = (BArray<BInteger>) returns[0];
        Assert.assertEquals(arrayValue.size(), 106);

        Assert.assertEquals(arrayValue.get(0).intValue(), 100);
        Assert.assertEquals(arrayValue.get(1).intValue(), 5);
        Assert.assertEquals(arrayValue.get(105).intValue(), 105);
    }

    @Test(description = "Test array arg value")
    public void testArrayArgValue() {
        BArray<BInteger> arrayValue = new BArray<>(BInteger.class);
        arrayValue.add(0, new BInteger(10));
        arrayValue.add(1, new BInteger(1));

        BValue[] args = {arrayValue};
        BValue[] returns = Functions.invoke(bFile, "arrayArgTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 11;
        Assert.assertEquals(actual, expected);
    }
    
    @Test(description = "Test accessing an out of bound array-index",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = "Array index out of range: Index: 5, Size: 2")
    public void testArrayIndexOutOfBoundError() {
        Functions.invoke(bFile, "arrayIndexOutOfBoundTest");
    }
    
    @Test(description = "Test array access with a key",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Array index should be of type int, not string. Array name: animals in " +
            "incorrect-array-access.bal:4")
    public void testArrayAccessWithKey() {
        ParserUtils.parseBalFile("lang/expressions/incorrect-array-access.bal");
    }
    
    @Test(description = "Test access a primitive a an array",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Attempt to index non-array, non-map variable: animal in " +
            "access-primitive-as-array.bal:3")
    public void testAccessPrimitiveAsArray() {
        ParserUtils.parseBalFile("lang/expressions/access-primitive-as-array.bal");
    }
}
