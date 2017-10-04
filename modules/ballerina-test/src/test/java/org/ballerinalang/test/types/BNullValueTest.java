/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina null value.
 */
public class BNullValueTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
//        compileResult = BTestUtils.compile("test-src/types/null/null-value.bal");
    }

    //    @Test(description = "Test null value of a xml")
    public void testXmlNull() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testXmlNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(5));
    }

    //    @Test(description = "Test null value of a json")
    public void testJsonNull() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testJsonNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(6));
    }

    //    @Test(description = "Test null value of a struct")
    public void testStructNull() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testStructNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(7));
    }

    //    @Test(description = "Test null value of a connector")
    public void testConnectorNull() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testConnectorNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(8));
    }

    //    @Test(description = "Test null value of a array")
    public void testArrayNull() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testArrayNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(9));
    }

    //    @Test(description = "Test null value of a map")
    public void testMapNull() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testMapNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(10));
    }

    //    @Test(description = "Test casting a nullified value")
    void testCastingNullValue() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testCastingNull", new BValue[]{null});
        Assert.assertEquals(vals[0], null);

//        vals = BLangFunctions.invoke(bLangProgram, "testCastingNull", new BValue[] { new BJSON("{}") });
//        Assert.assertTrue(vals[0] instanceof BXML);
//        Assert.assertEquals(((BXML) vals[0]).getMessageAsString(), "<name>converted xml</name>");
    }

    //    @Test(description = "Test passing null to a function expects a reference type")
    public void testFunctionCallWithNull() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testFunctionCallWithNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    //    @Test(description = "Test comparing null vs null")
    public void testNullLiteralComparison() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testNullLiteralComparison", new BValue[]{});
        Assert.assertTrue(vals[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) vals[0]).booleanValue(), true);
    }

    //    @Test(description = "Test returning a null literal")
    public void testReturnNullLiteral() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testReturnNullLiteral", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    //@Test(description = "Test null in worker")
    public void testNullInWorker() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testNullInWorker", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    //@Test(description = "Test null in fork-join")
    public void testNullInForkJoin() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testNullInForkJoin", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertTrue(vals[1] instanceof BMessage);
        Assert.assertEquals(((BMessage) vals[1]).stringValue(), "");
    }

    //    @Test(description = "Test array of null values")
    public void testArrayOfNulls() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testArrayOfNulls", new BValue[]{});
        BRefValueArray nullArray = (BRefValueArray) vals[0];
        Assert.assertTrue(nullArray.get(0) instanceof BStruct);
        Assert.assertEquals(nullArray.get(1), null);
        Assert.assertEquals(nullArray.get(2), null);
        Assert.assertTrue(nullArray.get(3) instanceof BStruct);
        Assert.assertEquals(nullArray.get(4), null);
    }

    //    @Test(description = "Test map of null values")
    public void testMapOfNulls() {
        BValue[] vals = BTestUtils.invoke(compileResult, "testMapOfNulls", new BValue[]{});
        BMap<String, BValue> nullMap = (BMap<String, BValue>) vals[0];
        Assert.assertEquals(nullMap.get("x2"), null);
        Assert.assertEquals(nullMap.get("x3"), null);
        Assert.assertTrue(nullMap.get("x4") instanceof BString);
        Assert.assertEquals(nullMap.get("x5"), null);
    }

    // Negative Tests

    //    @Test(description = "Test accessing an element in a null array",
//          expectedExceptions = BLangRuntimeException.class,
//          expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    void testNullArrayAccess() {
        BTestUtils.invoke(compileResult, "testNullArrayAccess", new BValue[]{});
    }

    //    @Test(description = "Test accessing an element in a null map",
//          expectedExceptions = BLangRuntimeException.class,
//          expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    void testNullMapAccess() {
        BTestUtils.invoke(compileResult, "testNullMapAccess", new BValue[]{});
    }

    //    @Test(description = "Test accessing an element in a null array",
//          expectedExceptions = BLangRuntimeException.class,
//          expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    void testActionInNullConenctor() {
        BTestUtils.invoke(compileResult, "testActionInNullConenctor", new BValue[]{});
    }

    //    @Test(description = "Test comparison of null values of two types")
    void testCompareNullOfDifferentTypes() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/null/null-of-different-types.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "");
    }

    @Test(description = "Test assigning null to a value type")
    void testNullAssignToValueType() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/null/null-for-value-types.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "incompatible types: expected 'int', found 'other'");
    }

    @Test(description = "Test casting a null literal to a value type")
    void testCastNullToValueType() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/null/null-cast-to-value-type.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "incompatible types: 'other' cannot be cast to 'string'");
    }

    //    @Test(description = "Test casting a null literal to a reference type")
    void testCastNullToReferenceType() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/null/null-cast-to-reference-type.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "");
    }

    //    @Test(description = "Test passing null to a function expects a value type")
    void testInvalidFunctionCallWithNull() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/null/invalid-function-call-with-nulll.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "");
    }

    @Test(description = "Test logical operations on null")
    void testLogicalOperationOnNull() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/null/logical-operation-on-null.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "operator '>' not defined for 'other' and 'xml'");
    }

    @Test(description = "Test arithmatic operations on null")
    void testArithmaticOperationOnNull() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/null/arithmatic-operation-on-null.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "operator '+' not defined for 'other' and 'other'");
    }
}
