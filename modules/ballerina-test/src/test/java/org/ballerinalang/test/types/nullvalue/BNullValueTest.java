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
package org.ballerinalang.test.types.nullvalue;

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
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina null value.
 */
public class BNullValueTest {

    private CompileResult positiveCompileResult;
    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        positiveCompileResult = BTestUtils.compile("test-src/types/null/null-value.bal");
        negativeCompileResult = BTestUtils.compile("test-src/types/null/null-value-negative.bal");
    }

    @Test(description = "Test null value of a xml")
    public void testXmlNull() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testXmlNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(5));
    }

    @Test(description = "Test null value of a json")
    public void testJsonNull() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testJsonNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(6));
    }

    @Test(description = "Test null value of a struct")
    public void testStructNull() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testStructNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(7));
    }

    // Todo - Fix issue in comparing
    @Test(description = "Test null value of a connector", enabled = false)
    public void testConnectorNull() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testConnectorNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(8));
    }

    // Todo - Fix issue in comparing
    @Test(description = "Test null value of a array", enabled = false)
    public void testArrayNull() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testArrayNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(9));
    }

    @Test(description = "Test null value of a map")
    public void testMapNull() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testMapNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(10));
    }

    @Test(description = "Test casting a nullified value")
    void testCastingNullValue() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testCastingNull", new BValue[]{null});
        Assert.assertEquals(vals[0], null);

//        vals = BLangFunctions.invoke(bLangProgram, "testCastingNull", new BValue[] { new BJSON("{}") });
//        Assert.assertTrue(vals[0] instanceof BXML);
//        Assert.assertEquals(((BXML) vals[0]).getMessageAsString(), "<name>converted xml</name>");
    }

    @Test(description = "Test passing null to a function expects a reference type")
    public void testFunctionCallWithNull() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testFunctionCallWithNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    // Todo - Fix issue in comparing
    @Test(description = "Test comparing null vs null", enabled = false)
    public void testNullLiteralComparison() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testNullLiteralComparison", new BValue[]{});
        Assert.assertTrue(vals[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) vals[0]).booleanValue(), true);
    }

    @Test(description = "Test returning a null literal")
    public void testReturnNullLiteral() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testReturnNullLiteral", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    // Todo
    @Test(description = "Test null in worker", enabled = false)
    public void testNullInWorker() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testNullInWorker", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    // Todo - Fix after casting issue is fixed in fork-join
    @Test(description = "Test null in fork-join", enabled = false)
    public void testNullInForkJoin() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testNullInForkJoin", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertTrue(vals[1] instanceof BMessage);
        Assert.assertEquals(((BMessage) vals[1]).stringValue(), "");
    }

    @Test(description = "Test array of null values")
    public void testArrayOfNulls() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testArrayOfNulls", new BValue[]{});
        BRefValueArray nullArray = (BRefValueArray) vals[0];
        Assert.assertTrue(nullArray.get(0) instanceof BStruct);
        Assert.assertEquals(nullArray.get(1), null);
        Assert.assertEquals(nullArray.get(2), null);
        Assert.assertTrue(nullArray.get(3) instanceof BStruct);
        Assert.assertEquals(nullArray.get(4), null);
    }

    @Test(description = "Test map of null values")
    public void testMapOfNulls() {
        BValue[] vals = BTestUtils.invoke(positiveCompileResult, "testMapOfNulls", new BValue[]{});
        BMap<String, BValue> nullMap = (BMap<String, BValue>) vals[0];
        Assert.assertEquals(nullMap.get("x2"), null);
        Assert.assertEquals(nullMap.get("x3"), null);
        Assert.assertTrue(nullMap.get("x4") instanceof BString);
        Assert.assertEquals(nullMap.get("x5"), null);
    }

    @Test(description = "Test accessing an element in a null array", expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: NullReferenceException.*")
    void testNullArrayAccess() {
        BTestUtils.invoke(positiveCompileResult, "testNullArrayAccess", new BValue[]{});
    }

    @Test(description = "Test accessing an element in a null map", expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: NullReferenceException.*")
    void testNullMapAccess() {
        BTestUtils.invoke(positiveCompileResult, "testNullMapAccess", new BValue[]{});
    }

    @Test(description = "Test accessing an element in a null array", expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: NullReferenceException.*")
    void testActionInNullConenctor() {
        BTestUtils.invoke(positiveCompileResult, "testActionInNullConenctor", new BValue[]{});
    }

    @Test(description = "Test negative test cases", enabled = false)
    void testNullValueNegative() {
        // Todo - Update errors
        BTestUtils.validateError(negativeCompileResult, 0, "", 0, 0);
    }
}
