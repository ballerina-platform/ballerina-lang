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
package org.ballerinalang.values;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina null value.
 */
public class BNullValueTest   {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/values/null/null-value.bal");
    }

    @Test(description = "Test null value of a xml")
    public void testXmlNull() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testXmlNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(5));
    }
    
    @Test(description = "Test null value of a json")
    public void testJsonNull() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testJsonNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(6));
    }
    
    @Test(description = "Test null value of a struct")
    public void testStructNull() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testStructNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(7));
    }
    
    @Test(description = "Test null value of a connector")
    public void testConnectorNull() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testConnectorNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(8));
    }
    
    @Test(description = "Test null value of a array")
    public void testArrayNull() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testArrayNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(9));
    }
    
    @Test(description = "Test null value of a map")
    public void testMapNull() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testMapNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(10));
    }
        
    @Test(description = "Test casting a nullified value")
    void testCastingNullValue() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testCastingNull", new BValue[] { null });
        Assert.assertEquals(vals[0], null);

//        vals = BLangFunctions.invoke(bLangProgram, "testCastingNull", new BValue[] { new BJSON("{}") });
//        Assert.assertTrue(vals[0] instanceof BXML);
//        Assert.assertEquals(((BXML) vals[0]).getMessageAsString(), "<name>converted xml</name>");
    }

    @Test(description = "Test passing null to a function expects a reference type")
    public void testFunctionCallWithNull() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testFunctionCallWithNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }
    
    @Test(description = "Test comparing null vs null")
    public void testNullLiteralComparison() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testNullLiteralComparison", new BValue[]{});
        Assert.assertTrue(vals[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) vals[0]).booleanValue(), true);
    }
    
    @Test(description = "Test returning a null literal")
    public void testReturnNullLiteral() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testReturnNullLiteral", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }
    
    //@Test(description = "Test null in worker")
    public void testNullInWorker() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testNullInWorker", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }
    
    //@Test(description = "Test null in fork-join")
    public void testNullInForkJoin() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testNullInForkJoin", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertTrue(vals[1] instanceof BMessage);
        Assert.assertEquals(((BMessage) vals[1]).stringValue(), "");
    }
    
    @Test(description = "Test array of null values")
    public void testArrayOfNulls() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testArrayOfNulls", new BValue[] {});
        BRefValueArray nullArray = (BRefValueArray) vals[0];
        Assert.assertTrue(nullArray.get(0) instanceof BStruct);
        Assert.assertEquals(nullArray.get(1), null);
        Assert.assertEquals(nullArray.get(2), null);
        Assert.assertTrue(nullArray.get(3) instanceof BStruct);
        Assert.assertEquals(nullArray.get(4), null);
    }
    
    @Test(description = "Test map of null values")
    public void testMapOfNulls() {
        BValue[] vals = BLangFunctions.invokeNew(programFile, "testMapOfNulls", new BValue[] {});
        BMap<String, BValue> nullMap = (BMap<String, BValue>) vals[0];
        Assert.assertEquals(nullMap.get("x2"), null);
        Assert.assertEquals(nullMap.get("x3"), null);
        Assert.assertTrue(nullMap.get("x4") instanceof BString);
        Assert.assertEquals(nullMap.get("x5"), null);
    }
    
    // Negative Tests
    
    @Test(description = "Test accessing an element in a null array",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    void testNullArrayAccess() {
        BLangFunctions.invokeNew(programFile, "testNullArrayAccess", new BValue[]{});
    }
    
    @Test(description = "Test accessing an element in a null map",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    void testNullMapAccess() {
        BLangFunctions.invokeNew(programFile, "testNullMapAccess", new BValue[] {});
    }

    @Test(description = "Test comparison of null values of two types",
            expectedExceptions = SemanticException.class, 
            expectedExceptionsMessageRegExp = "null-of-different-types.bal:5: invalid operation: incompatible types" +
            " 'xml' and 'json'")
    void testCompareNullOfDifferentTypes() {
        BTestUtils.getProgramFile("lang/values/null/null-of-different-types.bal");
    }
    
    @Test(description = "Test assigning null to a value type", 
            expectedExceptions = SemanticException.class, 
            expectedExceptionsMessageRegExp = "null-for-value-types.bal:2: incompatible types: 'null' cannot be " +
            "assigned to 'int'")
    void testNullAssignToValueType() {
        BTestUtils.getProgramFile("lang/values/null/null-for-value-types.bal");
    }
    
    @Test(description = "Test casting a null literal to a value type", 
            expectedExceptions = SemanticException.class, 
            expectedExceptionsMessageRegExp = "null-cast-to-value-type.bal:2: incompatible types: 'null' cannot be " +
            "cast to 'string'")
    void testCastNullToValueType() {
        BTestUtils.getProgramFile("lang/values/null/null-cast-to-value-type.bal");
    }
    
    @Test(description = "Test casting a null literal to a reference type", 
            expectedExceptions = SemanticException.class, 
            expectedExceptionsMessageRegExp = "null-cast-to-reference-type.bal:2: incompatible types: 'null' cannot " +
            "be cast to 'json'")
    void testCastNullToReferenceType() {
        BTestUtils.getProgramFile("lang/values/null/null-cast-to-reference-type.bal");
    }
    
    @Test(description = "Test passing null to a function expects a value type", 
            expectedExceptions = SemanticException.class, 
            expectedExceptionsMessageRegExp = "invalid-function-call-with-nulll.bal:2: undefined function 'foo'")
    void testInvalidFunctionCallWithNull() {
        BTestUtils.getProgramFile("lang/values/null/invalid-function-call-with-nulll.bal");
    }
    
    @Test(description = "Test accessing an element in a null array",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    void testActionInNullConenctor() {
        BLangFunctions.invokeNew(programFile, "testActionInNullConenctor", new BValue[]{});
    }
    
    @Test(description = "Test logical operations on null", 
            expectedExceptions = SemanticException.class, 
            expectedExceptionsMessageRegExp = "logical-operation-on-null.bal:3: invalid operation: incompatible " +
            "types 'null' and 'xml'")
    void testLogicalOperationOnNull() {
        BTestUtils.getProgramFile("lang/values/null/logical-operation-on-null.bal");
    }
    
    @Test(description = "Test arithmatic operations on null", 
            expectedExceptions = SemanticException.class, 
            expectedExceptionsMessageRegExp = "arithmatic-operation-on-null.bal:2: invalid operation: operator \\+ " +
            "not defined on 'null'")
    void testArithmaticOperationOnNull() {
        BTestUtils.getProgramFile("lang/values/null/arithmatic-operation-on-null.bal");
    }
}
