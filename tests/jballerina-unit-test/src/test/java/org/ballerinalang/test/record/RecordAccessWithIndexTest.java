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
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testCreateStruct");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Jack");

        Assert.assertTrue(returns.get(1) instanceof BMap);
        BMap<String, ?> adrsMap = ((BMap) returns.get(1));
        Assert.assertEquals(adrsMap.get(StringUtils.fromString("country")), StringUtils.fromString("USA"));
        Assert.assertEquals(adrsMap.get(StringUtils.fromString("state")), StringUtils.fromString("CA"));

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 25L);
    }

    @Test(description = "Test using expressions as index for record arrays")
    public void testExpressionAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testExpressionAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Jane");
    }

    @Test(description = "Test using records inside records")
    public void testStructOfStructs() {
        Object returns = BRunUtil.invoke(compileResult, "testStructOfStruct");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "USA");
    }

    @Test(description = "Test returning fields of a record")
    public void testReturnStructAttributes() {
        Object returns = BRunUtil.invoke(compileResult, "testReturnStructAttributes");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "emily");
    }

    @Test(description = "Test using record expression as a index in another record expression")
    public void testStructExpressionAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testStructExpressionAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "emily");
    }

    @Test(description = "Test default value of a record field")
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

    @Test(description = "Test default value of a nested record field")
    public void testNestedFieldDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testNestedFieldDefaultVal");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "default first name");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "Smith");

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 999L);
    }

    @Test(description = "Test using expression as the index")
    public void testExpressionAsStructIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testExpressionAsStructIndex");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Jack");
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessTypes() {
        Object returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypes");
        Assert.assertEquals(returns.toString(), "string:string:int:100:boolean:true:()::float:25.5:decimal:96.9");
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessTypesWithRestParam() {
        Object[] args = {StringUtils.fromString("fieldOne")};
        Object returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithRestParam", args);
        Assert.assertEquals(returns.toString(), ":int:50");

        Object[] args1 = {StringUtils.fromString("fieldTwo")};
        returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithRestParam", args1);
        Assert.assertEquals(returns.toString(), ":string:string");

        Object[] args2 = {StringUtils.fromString("fieldThree")};
        returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithRestParam", args2);
        Assert.assertEquals(returns.toString(), ":boolean:true");

        Object[] args3 = {StringUtils.fromString("fieldFour")};
        returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithRestParam", args3);
        Assert.assertEquals(returns.toString(), "()");
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessTypesWithOpenRecord() {
        Object returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessTypesWithOpenRecord");
        Assert.assertEquals(returns.toString(),
                ":object:10:function:16:json:json-string:boolean:true:():");
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessWithSingleType() {
        Object returns = BRunUtil.invoke(compileResult, "testDynamicIndexAccessWithSingleType");
        Assert.assertEquals(returns, 582L);
    }

    @Test(description = "Test using expression as the index")
    public void testDynamicIndexAccessWithRecordInsideRecord() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testDynamicIndexAccessWithRecordInsideRecord");
        Assert.assertEquals(returns.get(0), returns.get(1));
    }

    @Test(description = "Test using expression as the index")
    public void testFiniteTypeAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testFiniteTypeAsIndex");
        Assert.assertEquals(returns.toString(), "stringbarField98.995");
    }

    @Test(description = "Test using expression as the index")
    public void testUnionInFiniteTypeAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testUnionInFiniteTypeAsIndex");
        Assert.assertEquals(returns.toString(), "string100true()");
    }

    @Test(description = "Test using expression as the index")
    public void testUnionInFiniteTypeAsIndexNoField() {
        Object returns = BRunUtil.invoke(compileResult, "testUnionInFiniteTypeAsIndexNoField");
        Assert.assertEquals(returns.toString(), "Passed");
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
                "incompatible types: expected 'string', found 'FiniteOne'", 58, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'FiniteTwo'", 59, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'FiniteOne'", 62, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'FiniteTwo'", 63, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "invalid record member access expression: value space 'NoIntersection' " +
                        "out of range", 64, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "invalid record member access expression: value space 'NoIntersection' " +
                        "out of range", 65, 18);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(description = "Test accessing an field of a noninitialized record",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error:.*array index out of range: index: 0, size: 0.*")
    public void testGetNonInitField() {
        BRunUtil.invoke(compileResult, "testGetNonInitAttribute");
    }

    @Test(description = "Test accessing an arrays field of a noninitialized record",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error:.*array index out of range: index: 0, size: 0.*")
    public void testGetNonInitArrayField() {
        BRunUtil.invoke(compileResult, "testGetNonInitArrayAttribute");
    }

    @Test(description = "Test accessing the field of a noninitialized record",
            expectedExceptions = {BLangRuntimeException.class},
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

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negativeResult = null;
    }
}
