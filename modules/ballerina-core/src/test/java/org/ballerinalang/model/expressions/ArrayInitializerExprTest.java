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
package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test arrays initializer expression.
 *
 * @since 0.8.0
 */
public class ArrayInitializerExprTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/array-initializer-expr.bal");
    }

    @Test(description = "Test arrays initializer expression")
    public void testArrayInitExpr() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayInitTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 110;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test arrays return value")
    public void testArrayReturnValueTest() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayReturnTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BString> arrayValue = (BArray<BString>) returns[0];
        Assert.assertEquals(arrayValue.size(), 6);

        Assert.assertEquals(arrayValue.get(0).stringValue(), "Lion");
        Assert.assertEquals(arrayValue.get(1).stringValue(), "Cat");
        Assert.assertEquals(arrayValue.get(5).stringValue(), "Croc");
    }
    
    @Test(description = "Test arrays initializing with different types",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "multi-type-array-initializer.bal:3: " +
                    "array initializer is not allowed here")
    public void testMultiTypeMapInit() {
        BTestUtils.parseBalFile("lang/expressions/multi-type-array-initializer.bal");
    }
}
