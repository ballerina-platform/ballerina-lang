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
package org.ballerinalang.test.record;

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

;

/**
 * Test cases for user defined record types in ballerina.
 */
public class RecordAccessWithIndexTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/record_access_with_index.bal");
        negativeResult = BCompileUtil.compile("test-src/record/record_access_with_index_negative.bal");
    }

    @Test(description = "Test Basic record operations")
    public void testBasicStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");

        Assert.assertTrue(returns[1] instanceof BMap);
        BMap<String, ?> adrsMap = ((BMap) returns[1]);
        Assert.assertEquals(adrsMap.get("country"), new BString("USA"));
        Assert.assertEquals(adrsMap.get("state"), new BString("CA"));

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 25);
    }

    @Test(description = "Test using expressions as index for record arrays")
    public void testExpressionAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jane");
    }

    @Test(description = "Test using records inside records")
    public void testStructOfStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructOfStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "USA");
    }

    @Test(description = "Test returning fields of a record")
    public void testReturnStructAttributes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnStructAttributes");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test using record expression as a index in another record expression")
    public void testStructExpressionAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test default value of a record field")
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

    @Test(description = "Test default value of a nested record field")
    public void testNestedFieldDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedFieldDefaultVal");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Smith");

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }

    @Test(description = "Test using expression as the index")
    public void testExpressionAsStructIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpressionAsStructIndex");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessTypes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypes");
        Assert.assertEquals(returns[0].stringValue(), "string:string:int:100:boolean:true:()::float:25.5:decimal:96.9");
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessTypesWithRestParam() {
        BValue[] args = {new BString("fieldOne")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithRestParam", args);
        Assert.assertEquals(returns[0].stringValue(), ":int:50");

        BValue[] args1 = {new BString("fieldTwo")};
        returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithRestParam", args1);
        Assert.assertEquals(returns[0].stringValue(), ":string:string");

        BValue[] args2 = {new BString("fieldThree")};
        returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithRestParam", args2);
        Assert.assertEquals(returns[0].stringValue(), ":boolean:true");

        BValue[] args3 = {new BString("fieldFour")};
        returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithRestParam", args3);
        Assert.assertEquals(returns[0].stringValue(), "()");
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessTypesWithOpenRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithOpenRecord");
        Assert.assertEquals(returns[0].stringValue(),
                ":object:10:function:16:json:json-string:boolean:true:():");
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessWithSingleType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessWithSingleType");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 582);
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessWithRecordInsideRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessWithRecordInsideRecord");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), ((BInteger) returns[1]).intValue());
    }

    @Test(description = "Test using expression as the index")
    public void testFiniteTypeAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFiniteTypeAsIndex");
        Assert.assertEquals(returns[0].stringValue(), "stringbarField98.995");
    }

    @Test(description = "Test using expression as the index")
    public void testUnionInFiniteTypeAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUnionInFiniteTypeAsIndex");
        Assert.assertEquals(returns[0].stringValue(), "string100true()");
    }

    @Test(description = "Test using expression as the index")
    public void testUnionInFiniteTypeAsIndexNoField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUnionInFiniteTypeAsIndexNoField");
        Assert.assertEquals(returns[0].stringValue(), "Passed");
    }

    // Negative tests

    @Test(description = "Test accessing an undeclared record")
    public void testUndeclaredStructAccess() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'dpt1'", 3, 5);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'id' in 'Department'", 9, 5);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 20, 17);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found '(string|int)?'", 26, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'fieldOne|fieldTwo|0'", 58, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found '0|1'", 59, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'fieldOne|fieldTwo|0'", 62, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found '0|1'", 63, 40);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid record index expression: value space '(fieldFour|F1|F2|F3)' out of range",
                                  64, 40);
        BAssertUtil.validateError(negativeResult, i++,
                                  "invalid record index expression: value space '(fieldFour|F1|F2|F3)' out of range",
                                  65, 18);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(description = "Test accessing an field of a noninitialized record",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error:.*array index out of range: index: 0, size: 0.*")
    public void testGetNonInitField() {
        BRunUtil.invoke(compileResult, "testGetNonInitAttribute");
    }

    @Test(description = "Test accessing an arrays field of a noninitialized record",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error:.*array index out of range: index: 0, size: 0.*")
    public void testGetNonInitArrayField() {
        BRunUtil.invoke(compileResult, "testGetNonInitArrayAttribute");
    }

    @Test(description = "Test accessing the field of a noninitialized record",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error:.*array index out of range: index: 0, size: 0.*")
    public void testGetNonInitLastField() {
        BRunUtil.invoke(compileResult, "testGetNonInitLastAttribute");
    }

    @Test(description = "Test setting an field of a noninitialized child record")
    public void testSetNonInitField() {
        BRunUtil.invoke(compileResult, "testSetFieldOfNonInitChildStruct");
    }

    @Test(description = "Test setting the field of a noninitialized root record")
    public void testSetNonInitLastField() {
        BRunUtil.invoke(compileResult, "testSetFieldOfNonInitStruct");
    }
}
