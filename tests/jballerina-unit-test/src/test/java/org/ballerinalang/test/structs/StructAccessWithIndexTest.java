/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.structs;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.BLangConstants;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types in ballerina.
 */
public class StructAccessWithIndexTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/structs/proj/src/default/struct-access-with-index.bal");
        negativeResult = BCompileUtil.compile("test-src/structs/struct-access-with-index-negative.bal");
    }

    @Test(description = "Test Basic struct operations")
    public void testBasicStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateStructSt");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");

        Assert.assertTrue(returns[1] instanceof BMap);
        BMap<String, ?> adrsMap = ((BMap) returns[1]);
        Assert.assertEquals(adrsMap.get("country"), new BString("USA"));
        Assert.assertEquals(adrsMap.get("state"), new BString("CA"));

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 25);
    }

    @Test(description = "Test using expressions as index for struct arrays")
    public void testExpressionAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jane");
    }

    @Test(description = "Test using structs inside structs")
    public void testStructOfStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructOfStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "USA");
    }

    @Test(description = "Test returning fields of a struct")
    public void testReturnStructAttributes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnStructAttributes");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test using struct expression as a index in another struct expression")
    public void testStructExpressionAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test default value of a struct field")
    public void testDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDefaultVal");

        // Check default value of a field where the default value is set
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");

        // Check the default value of a field where the default value is not set
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), BLangConstants.STRING_EMPTY_VALUE);

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }

    @Test(description = "Test default value of a nested struct field")
    public void testNestedFieldDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedFieldDefaultVal");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Smith");

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }
    
    // Negative tests

    @Test(description = "Test accessing an undeclared struct")
    public void testUndeclaredStructAccess() {
        BAssertUtil.validateError(negativeResult, 0, "undefined symbol 'dpt1'", 3, 5);
    }

    @Test(description = "Test accessing an undeclared field of a struct")
    public void testUndeclaredFieldAccess() {
        BAssertUtil.validateError(negativeResult, 1, "undefined field 'id' in 'Department'", 9, 5);
    }

    @Test(description = "Test accesing a struct with a dynamic index")
    public void testExpressionAsStructIndex() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-access-dynamic-index-negative.bal");
        BValue[] result = BRunUtil.invoke(compileResult, "testExpressionAsStructIndex");
        Assert.assertEquals(result[0].stringValue(), "Jack");
    }

    @Test(description = "Test accessing an field of a noninitialized struct",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error:.*array index out of range: index: 0, size: 0.*")
    public void testGetNonInitField() {
        BRunUtil.invoke(compileResult, "testGetNonInitAttribute");
    }

    @Test(description = "Test accessing an arrays field of a noninitialized struct",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error:.*array index out of range: index: 0, size: 0.*")
    public void testGetNonInitArrayField() {
        BRunUtil.invoke(compileResult, "testGetNonInitArrayAttribute");
    }

    @Test(description = "Test accessing the field of a noninitialized struct",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error:.*array index out of range: index: 0, size: 0.*")
    public void testGetNonInitLastField() {
        BRunUtil.invoke(compileResult, "testGetNonInitLastAttribute");
    }

    @Test(description = "Test setting an field of a noninitialized child struct")
    public void testSetNonInitField() {
        BRunUtil.invoke(compileResult, "testSetFieldOfNonInitChildStruct");
    }

    @Test(description = "Test setting the field of a noninitialized root struct")
    public void testSetNonInitLastField() {
        BRunUtil.invoke(compileResult, "testSetFieldOfNonInitStruct");
    }
}
