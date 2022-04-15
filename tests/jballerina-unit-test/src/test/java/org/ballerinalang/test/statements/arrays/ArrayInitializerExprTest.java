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
package org.ballerinalang.test.statements.arrays;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test arrays initializer expression.
 *
 * @since 0.8.0
 */
public class ArrayInitializerExprTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-initializer-expr.bal");
    }

    @Test(description = "Test arrays initializer expression")
    public void testArrayInitExpr() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "arrayInitTest", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 110;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test arrays return value")
    public void testArrayReturnValueTest() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "arrayReturnTest", args);

        Assert.assertTrue(returns instanceof BArray);

        BArray arrayValue = (BArray) returns;
        Assert.assertEquals(arrayValue.size(), 6);

        Assert.assertEquals(arrayValue.getString(0), "Lion");
        Assert.assertEquals(arrayValue.getString(1), "Cat");
        Assert.assertEquals(arrayValue.getString(5), "Croc");
    }

    @Test(description = "Test array of finite type and nil")
    public void finiteTypeArray() {
        Object returns = BRunUtil.invoke(compileResult, "finiteTypeArray");
        Assert.assertEquals(returns.toString(), "Terminating");
    }
    
    @Test
    public void testMultiTypeMapInit() {
        CompileResult result = BCompileUtil.compile("test-src/statements/arrays/multi-type-array-initializer.bal");
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'int', found 'string'", 3, 10);
    }
    
    @Test(description = "Test nested array inline initializing")
    public void testNestedArrayInit() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "testNestedArrayInit", args);

        Assert.assertTrue(returns instanceof BArray);

        BArray arrayValue = (BArray) returns;
        Assert.assertEquals(arrayValue.size(), 2);

        Object element = arrayValue.getRefValue(0);
        Assert.assertTrue(element instanceof  BArray);
        BArray elementArray = (BArray) element;
        Assert.assertEquals(elementArray.size(), 3);
        Assert.assertEquals(elementArray.getInt(0), 1);
        Assert.assertEquals(elementArray.getInt(1), 2);
        Assert.assertEquals(elementArray.getInt(2), 3);
        
        element = arrayValue.getRefValue(1);
        Assert.assertTrue(element instanceof  BArray);
        elementArray = (BArray) element;
        Assert.assertEquals(elementArray.size(), 4);
        Assert.assertEquals(elementArray.getInt(0), 6);
        Assert.assertEquals(elementArray.getInt(1), 7);
        Assert.assertEquals(elementArray.getInt(2), 8);
        Assert.assertEquals(elementArray.getInt(3), 9);
    }

    @Test(description = "Test array of maps inline initializing")
    public void testArrayOfMapsInit() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "testArrayOfMapsInit", args);

        Assert.assertTrue(returns instanceof BArray);

        BArray arrayValue = (BArray) returns;
        Assert.assertEquals(arrayValue.size(), 3);

        Object adrs1 = arrayValue.getRefValue(0);
        Assert.assertTrue(adrs1 instanceof BMap<?, ?>);
        Object address = ((BMap) adrs1).get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(StringUtils.fromString("city")).toString(), "Colombo");

        Object adrs2 = arrayValue.getRefValue(1);
        Assert.assertTrue(adrs2 instanceof BMap<?, ?>);
        address = ((BMap) adrs2).get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(StringUtils.fromString("city")).toString(), "Kandy");

        Object adrs3 = arrayValue.getRefValue(2);
        Assert.assertTrue(adrs3 instanceof BMap<?, ?>);
        address = ((BMap) adrs3).get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(StringUtils.fromString("city")).toString(), "Galle");
    }

    @Test(description = "Test float array initialization with integer values")
    public void testFloatArrayInitWithIntExpr() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "floatArrayInitWithInt", args);

        Assert.assertTrue(returns instanceof BArray);

        BArray arrayValue = (BArray) returns;
        Assert.assertEquals(arrayValue.size(), 3);

        Assert.assertEquals(arrayValue.getFloat(0), 2.0);
        Assert.assertEquals(arrayValue.getFloat(1), 4.0);
        Assert.assertEquals(arrayValue.getFloat(2), 5.0);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
