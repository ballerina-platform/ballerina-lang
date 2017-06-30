/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

/**
 * Test cases for ballerina.model.arrays.
 */
public class ArrayTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/arrayTest.bal");
    }

    @Test
    public void testFloatArrayLength() {
        BFloatArray arrayValue = new BFloatArray();
        arrayValue.add(0, 10);
        arrayValue.add(1, 11.1f);
        arrayValue.add(2, 12.2f);

        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testFloatArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testIntArrayLength() {
        BIntArray arrayValue = new BIntArray();
        arrayValue.add(0, 10);
        arrayValue.add(1, 11);
        arrayValue.add(2, 12);
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testIntArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 4, "Length didn't match");
    }

    @Test
    public void testStringArrayLength() {
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, "Hello");
        arrayValue.add(1, "World");
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testStringArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testXMLArrayLength() {
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BXMLItem("<t>a</t>"));
        arrayValue.add(1, new BXMLItem("<t>b</t>"));
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testXMLArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testJSONArrayLength() {
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BJSON("{ \"test\" : \"1\"}"));
        arrayValue.add(1, new BJSON("{ \"test\" : \"1\"}"));
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testJSONArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testMessageArrayLength() {
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BMessage(new DefaultCarbonMessage()));
        arrayValue.add(1, new BMessage(new DefaultCarbonMessage()));
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMessageArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testFloatArrayCopyOf() {
        final float v1 = 10f;
        final float v2 = 11.1f;

        BFloatArray arrayValue = new BFloatArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);

        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testFloatArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BFloatArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertNotEquals(((BFloatArray) returnVals[0]).get(0), v1, "Found same value");
        Assert.assertNotEquals(((BFloatArray) returnVals[0]).get(1), v2, "Found same value");
    }

    @Test
    public void testIntArrayCopyOf() {
        final int v1 = 10;
        final int v2 = 11;
        BIntArray arrayValue = new BIntArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testIntArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BIntArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertNotEquals(((BIntArray) returnVals[0]).get(0), v1, "Found same value");
        Assert.assertNotEquals(((BIntArray) returnVals[0]).get(0), v2, "Found same value");
    }

    @Test
    public void testStringArrayCopyOf() {
        final String v1 = "a";
        final String v2 = "b";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testStringArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BStringArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertNotEquals(((BStringArray) returnVals[0]).get(0), v1, "Found same value");
        Assert.assertNotEquals(((BStringArray) returnVals[0]).get(1), v2, "Found same value");
    }

    @Test
    public void testXMLArrayCopyOf() {
        final String v1 = "<xml>a</xml>";
        final String v2 = "<xml>b</xml>";
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0,  new BXMLItem(v1));
        arrayValue.add(1,  new BXMLItem(v2));
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testXMLArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).get(0), v1, "Found same value");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).get(1), v2, "Found same value");
    }

    @Test
    public void testJSONArrayCopyOf() {
        final String v1 = "{ \"json\" : \"1\"}";
        final String v2 = "{ \"json\" : \"2\"}";
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0,  new BJSON(v1));
        arrayValue.add(1,  new BJSON(v2));
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testJSONArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).get(0), v1, "Found same value");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).get(1), v2, "Found same value");
    }

    @Test
    public void testMessageArrayCopyOf() {
        final CarbonMessage v1 = new DefaultCarbonMessage();
        final CarbonMessage v2 = new DefaultCarbonMessage();
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0,  new BMessage(v1));
        arrayValue.add(1,  new BMessage(v2));
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMessageArrayCopy", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
    }

    @Test
    public void testFloatArrayCopyOfRanged() {
        final double v1 = 10d;
        final double v2 = 11.1d;
        final double v3 = 11.2d;
        final double v4 = 11.3d;

        BFloatArray arrayValue = new BFloatArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        arrayValue.add(2, v3);
        arrayValue.add(3, v4);

        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testFloatArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BFloatArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertEquals(((BFloatArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertEquals(((BFloatArray) returnVals[0]).get(0), v2, "Value didn't match");
        Assert.assertEquals(((BFloatArray) returnVals[0]).get(1), v3, "Value didn't match");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testFloatArrayCopyOfRangeNegative() {
        final double v1 = 10d;
        final double v2 = 11.1d;

        BFloatArray arrayValue = new BFloatArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testFloatArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testFloatArrayCopyOfRangNegativeMinusArgs() {
        final double v1 = 10d;
        final double v2 = 11.1d;
        BFloatArray arrayValue = new BFloatArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        BValue[] args = {arrayValue , new BInteger(-1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testFloatArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testIntArrayCopyOfRanged() {
        final int v1 = 10;
        final int v2 = 11;
        final int v3 = 12;
        final int v4 = 13;
        BIntArray arrayValue = new BIntArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        arrayValue.add(2, v3);
        arrayValue.add(3, v4);
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testIntArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BIntArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertEquals(((BIntArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertEquals(((BIntArray) returnVals[0]).get(0), v2, "Value didn't match");
        Assert.assertEquals(((BIntArray) returnVals[0]).get(1), v3, "Value didn't match");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testIntArrayCopyOfRangeNegative() {
        final int v1 = 10;
        final int v2 = 11;
        BIntArray arrayValue = new BIntArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testIntArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testIntArrayCopyOfRangNegativeMinusArgs() {
        final int v1 = 10;
        final int v2 = 11;
        BIntArray arrayValue = new BIntArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        BValue[] args = {arrayValue , new BInteger(-1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testIntArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testStringArrayCopyOfRange() {
        final String v1 = "a";
        final String v2 = "b";
        final String v3 = "c";
        final String v4 = "d";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        arrayValue.add(2, v3);
        arrayValue.add(3, v4);
        BValue[] args = {arrayValue, new BInteger(1), new BInteger(3)};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testStringArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BStringArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertEquals(((BStringArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertEquals(((BStringArray) returnVals[0]).get(0), v2, "Value didn't match");
        Assert.assertEquals(((BStringArray) returnVals[0]).get(1), v3, "Value didn't match");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testStringArrayCopyOfRangeNegative() {
        final String v1 = "a";
        final String v2 = "b";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testStringArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testStringArrayCopyOfRangNegativeMinusArgs() {
        final String v1 = "a";
        final String v2 = "b";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        BValue[] args = {arrayValue , new BInteger(-1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testStringArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testXMLArrayCopyOfRange() {
        final String v1 = "<xml>a</xml>";
        final String v2 = "<xml>b</xml>";
        final String v3 = "<xml>c</xml>";
        final String v4 = "<xml>d</xml>";
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BXMLItem(v1));
        arrayValue.add(1, new BXMLItem(v2));
        arrayValue.add(2, new BXMLItem(v3));
        arrayValue.add(3, new BXMLItem(v4));
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testXMLArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertEquals(((BRefValueArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).get(0), v1, "Found same value");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).get(1), v2, "Found same value");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testXMLArrayCopyOfRangeNegative() {
        final String v1 = "<xml>a</xml>";
        final String v2 = "<xml>b</xml>";
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BXMLItem(v1));
        arrayValue.add(1, new BXMLItem(v2));
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testXMLArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testXMLArrayCopyOfRangNegativeMinusArgs() {
        final String v1 = "<xml>a</xml>";
        final String v2 = "<xml>b</xml>";
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BXMLItem(v1));
        arrayValue.add(1, new BXMLItem(v2));
        BValue[] args = {arrayValue , new BInteger(-1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testXMLArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testJSONArrayCopyOfRange() {
        final String v1 = "{ \"json\" : \"1\"}";
        final String v2 = "{ \"json\" : \"2\"}";
        final String v3 = "{ \"json\" : \"3\"}";
        final String v4 = "{ \"json\" : \"4\"}";
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BJSON(v1));
        arrayValue.add(1, new BJSON(v2));
        arrayValue.add(2, new BJSON(v3));
        arrayValue.add(3, new BJSON(v4));
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testJSONArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertEquals(((BRefValueArray) returnVals[0]).size(), 2, "Incorrect Array size.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).get(0), v1, "Found same value");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).get(1), v2, "Found same value");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testJSONArrayCopyOfRangeNegative() {
        final String v1 = "{ \"json\" : \"1\"}";
        final String v2 = "{ \"json\" : \"2\"}";
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BJSON(v1));
        arrayValue.add(1, new BJSON(v2));
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testJSONArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testJSONArrayCopyOfRangNegativeMinusArgs() {
        final String v1 = "{ \"json\" : \"1\"}";
        final String v2 = "{ \"json\" : \"2\"}";
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BJSON(v1));
        arrayValue.add(1, new BJSON(v2));
        BValue[] args = {arrayValue , new BInteger(-1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testJSONArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testMessageArrayCopyOfRange() {
        final CarbonMessage v1 = new DefaultCarbonMessage();
        final CarbonMessage v2 = new DefaultCarbonMessage();
        final CarbonMessage v3 = new DefaultCarbonMessage();
        final CarbonMessage v4 = new DefaultCarbonMessage();
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BMessage(v1));
        arrayValue.add(1, new BMessage(v2));
        arrayValue.add(2, new BMessage(v3));
        arrayValue.add(3, new BMessage(v4));
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMessageArrayCopyRange", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertNotEquals(((BRefValueArray) returnVals[0]).size(), arrayValue.size(), "Found Same size arrays.");
        Assert.assertEquals(((BRefValueArray) returnVals[0]).size(), 2, "Incorrect Array size.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testMessageArrayCopyOfRangNegative() {
        final CarbonMessage v1 = new DefaultCarbonMessage();
        final CarbonMessage v2 = new DefaultCarbonMessage();
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BMessage(v1));
        arrayValue.add(1, new BMessage(v2));
        BValue[] args = {arrayValue , new BInteger(1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testMessageArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Negative test case for checking arg range.", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range.*")
    public void testMessageArrayCopyOfRangNegativeMinusArgs() {
        final CarbonMessage v1 = new DefaultCarbonMessage();
        final CarbonMessage v2 = new DefaultCarbonMessage();
        BRefValueArray arrayValue = new BRefValueArray();
        arrayValue.add(0, new BMessage(v1));
        arrayValue.add(1, new BMessage(v2));
        BValue[] args = {arrayValue , new BInteger(-1), new BInteger(3)};
        BLangFunctions.invokeNew(programFile, "testMessageArrayCopyRange", args);
        Assert.fail("Test should fail at this point.");
    }

    @Test
    public void testStringArraySort() {
        final String v1 = "currency";
        final String v2 = "states";
        final String v3 = "country";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, v1);
        arrayValue.add(1, v2);
        arrayValue.add(2, v3);
        BValue[] args = {arrayValue};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testStringArraySort", args);
        Assert.assertEquals(((BStringArray) returnVals[0]).get(0), "country");
        Assert.assertEquals(((BStringArray) returnVals[0]).get(1), "currency");
        Assert.assertEquals(((BStringArray) returnVals[0]).get(2), "states");
    }
    
    @Test
    public void testArrayToString() {
        String[] strArray = { "aaa", "bbb", "ccc" };
        BStringArray bStringArray = new BStringArray(strArray);
        Assert.assertEquals(bStringArray.stringValue(), "[\"aaa\",\"bbb\",\"ccc\"]");

        long[] longArray = { 6, 3, 8, 4 };
        BIntArray bIntArray = new BIntArray(longArray);
        Assert.assertEquals(bIntArray.stringValue(), "[6,3,8,4]");

        double[] doubleArray = { 6.4, 3.7, 8.8, 7.4 };
        BFloatArray bFloatArray = new BFloatArray(doubleArray);
        Assert.assertEquals(bFloatArray.stringValue(), "[6.4,3.7,8.8,7.4]");

        int[] boolArray = { 1, 1, 0 };
        BBooleanArray bBooleanArray = new BBooleanArray(boolArray);
        Assert.assertEquals(bBooleanArray.stringValue(), "[true,true,false]");

        BXMLItem[] xmlArray = { new BXMLItem("<foo/>"), new BXMLItem("<bar>hello</bar>") };
        BRefValueArray bXmlArray = new BRefValueArray(xmlArray);
        Assert.assertEquals(bXmlArray.stringValue(), "[<foo/>,<bar>hello</bar>]");
    }
}
