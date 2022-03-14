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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        compileResult = BCompileUtil.compile(
                "test-src/structs/ObjectWithPrivateFieldsTestProject/struct-access-with-index.bal");
        negativeResult = BCompileUtil.compile("test-src/structs/struct-access-with-index-negative.bal");
    }

    @Test(description = "Test Basic struct operations")
    public void testBasicStruct() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testCreateStructSt");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Jack");

        Assert.assertTrue(returns.get(1) instanceof BMap);
        BMap<String, ?> adrsMap = ((BMap) returns.get(1));
        Assert.assertEquals(adrsMap.get(StringUtils.fromString("country")), StringUtils.fromString("USA"));
        Assert.assertEquals(adrsMap.get(StringUtils.fromString("state")), StringUtils.fromString("CA"));

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 25L);
    }

    @Test(description = "Test using expressions as index for struct arrays")
    public void testExpressionAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testExpressionAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Jane");
    }

    @Test(description = "Test using structs inside structs")
    public void testStructOfStructs() {
        Object returns = BRunUtil.invoke(compileResult, "testStructOfStruct");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "USA");
    }

    @Test(description = "Test returning fields of a struct")
    public void testReturnStructAttributes() {
        Object returns = BRunUtil.invoke(compileResult, "testReturnStructAttributes");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "emily");
    }

    @Test(description = "Test using struct expression as a index in another struct expression")
    public void testStructExpressionAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testStructExpressionAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "emily");
    }

    @Test(description = "Test default value of a struct field")
    public void testDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testDefaultVal");

        // Check default value of a field where the default value is set
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "default first name");

        // Check the default value of a field where the default value is not set
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "");

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 999L);
    }

    @Test(description = "Test default value of a nested struct field")
    public void testNestedFieldDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testNestedFieldDefaultVal");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "default first name");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "Smith");

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 999L);
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
        Object returns = BRunUtil.invoke(compileResult, "testExpressionAsStructIndex");
        Assert.assertEquals(returns.toString(), "Jack");
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

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negativeResult = null;
    }
}
