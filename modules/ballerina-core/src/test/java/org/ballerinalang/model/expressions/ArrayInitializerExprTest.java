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
import org.ballerinalang.model.values.BMap;
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

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 110;
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
                    "incompatible types: 'string' cannot be converted to 'int'")
    public void testMultiTypeMapInit() {
        BTestUtils.parseBalFile("lang/expressions/multi-type-array-initializer.bal");
    }
    
    @Test(description = "Test nested array inline initializing")
    public void testNestedArrayInit() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNestedArrayInit", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BString> arrayValue = (BArray<BString>) returns[0];
        Assert.assertEquals(arrayValue.size(), 2);

        BValue element = arrayValue.get(0);
        Assert.assertTrue(element instanceof BArray<?>);
        BArray<BInteger> elementArray = (BArray<BInteger>) element;
        Assert.assertEquals(elementArray.size(), 3);
        Assert.assertEquals(elementArray.get(0).intValue(), 1);
        Assert.assertEquals(elementArray.get(1).intValue(), 2);
        Assert.assertEquals(elementArray.get(2).intValue(), 3);
        
        element = arrayValue.get(1);
        Assert.assertTrue(element instanceof BArray<?>);
        elementArray = (BArray<BInteger>) element;
        Assert.assertEquals(elementArray.size(), 4);
        Assert.assertEquals(elementArray.get(0).intValue(), 6);
        Assert.assertEquals(elementArray.get(1).intValue(), 7);
        Assert.assertEquals(elementArray.get(2).intValue(), 8);
        Assert.assertEquals(elementArray.get(3).intValue(), 9);
    }
    
    @Test(description = "Test array of maps inline initializing")
    public void testArrayOfMapsInit() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testArrayOfMapsInit", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BValue> arrayValue = (BArray<BValue>) returns[0];
        Assert.assertEquals(arrayValue.size(), 3);

        BValue adrs1 = arrayValue.get(0);
        Assert.assertTrue(adrs1 instanceof BMap<?, ?>);
        BValue address = ((BMap) adrs1).get(new BString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(new BString("city")).stringValue(), "Colombo");

        BValue adrs2 = arrayValue.get(1);
        Assert.assertTrue(adrs2 instanceof BMap<?, ?>);
        address = ((BMap) adrs2).get(new BString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(new BString("city")).stringValue(), "Kandy");

        BValue adrs3 = arrayValue.get(2);
        Assert.assertTrue(adrs3 instanceof BMap<?, ?>);
        address = ((BMap) adrs3).get(new BString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(new BString("city")).stringValue(), "Galle");
    }
    
    @Test(description = "Test array of maps inline initializing")
    public void testAnyAsArray() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyAsArray", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BInteger> arrayValue = (BArray<BInteger>) returns[0];
        Assert.assertEquals(arrayValue.size(), 3);
        Assert.assertEquals(arrayValue.get(0).intValue(), 1);
    }
}
