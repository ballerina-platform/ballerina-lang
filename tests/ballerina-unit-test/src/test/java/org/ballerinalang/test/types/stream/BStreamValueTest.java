/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.stream;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test stream type.
 */
public class BStreamValueTest {

    private CompileResult result;
    private CompileResult failureResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/stream/stream-value.bal");
        failureResult = BCompileUtil.compile("test-src/types/stream/stream-negative.bal");
    }

    @Test(description = "Test streams for invalid scenarios")
    public void testConstrainedStreamNegative() {
        Assert.assertEquals(failureResult.getErrorCount(), 8);
        BAssertUtil.validateError(failureResult, 0, "incompatible types: expected 'stream<int>',"
                                  + " found 'stream'", 14, 12);
        BAssertUtil.validateError(failureResult, 1, "incompatible types: expected 'stream<int>',"
                                  + " found 'stream<string>'", 19, 12);
        BAssertUtil.validateError(failureResult, 2, "incompatible types: expected"
                                  + " 'stream<Person>', found 'stream<Employee>'", 24, 37);
        BAssertUtil.validateError(failureResult, 3, "incompatible types: expected"
                                  + " 'stream<Person>', found 'stream'", 30, 37);
        BAssertUtil.validateError(failureResult, 4, "incompatible types: 'stream<Employee>'"
                                  + " cannot be converted to 'stream<Person>'", 41, 24);
        BAssertUtil.validateError(failureResult, 5, "incompatible types: 'Employee' cannot be"
                                  + " converted to 'stream<int>'", 48, 17);
        BAssertUtil.validateError(failureResult, 6, "incompatible types: 'stream<Person>' cannot"
                                  + " be converted to 'stream<Employee>'", 55, 26);
        BAssertUtil.validateError(failureResult, 7, "incompatible types: 'any' cannot be"
                                  + " converted to 'stream<Employee>'", 63, 18);
    }

    @Test(description = "Test publishing records of invalid type to a stream",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*error: incompatible types: value of type:Job cannot be added to "
                    + "a stream of type:Employee.*")
    public void testInvalidRecordPublishingToStream() {
        BRunUtil.invoke(result, "testInvalidRecordPublishingToStream");
    }

    @Test(description = "Test subscribing with a function accepting a different kind of record",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*error: incompatible function: subscription function needs to be a "
                    + "function accepting:Employee.*")
    public void testSubscriptionFunctionWithIncorrectRecordParameter() {
        BRunUtil.invoke(result, "testSubscriptionFunctionWithIncorrectRecordParameter");
    }

    @Test(description = "Test publishing objects of invalid type to a stream",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*error: incompatible types: value of type:Coach cannot be added to "
                    + "a stream of type:Captain.*")
    public void testInvalidObjectPublishingToStream() {
        BRunUtil.invoke(result, "testInvalidObjectPublishingToStream");
    }

    @Test(description = "Test subscribing with a function accepting a different kind of object",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*error: incompatible function: subscription function needs to be a "
                    + "function accepting:Captain.*")
    public void testSubscriptionFunctionWithIncorrectObjectParameter() {
        BRunUtil.invoke(result, "testSubscriptionFunctionWithIncorrectObjectParameter");
    }

    @Test(description = "Test subscribing to a union type constrained stream with a function of whose parameter union"
            + " type does not contain all possible types or assignable types",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*error: incompatible function: subscription function needs to be a "
                    + "function accepting:.*")
    public void testSubscriptionFunctionWithUnassignableUnionParameter() {
        BRunUtil.invoke(result, "testSubscriptionFunctionWithUnassignableUnionParameter");
    }

    @Test(description = "Test subscribing to a tuple type constrained stream with a function where the elements of "
            + "the constraint tuple type are not assignable to those of the parameter of the subscription function",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*error: incompatible function: subscription function needs to be a "
                    + "function accepting:.*")
    public void testSubscriptionFunctionWithUnassignableTupleTypeParameter() {
        BRunUtil.invoke(result, "testSubscriptionFunctionWithUnassignableTupleTypeParameter");
    }

    @Test(description = "Test receipt of single record event with correct subscription and publishing, for a globally"
            + " declared stream")
    public void testGlobalStream() {
        BValue[] returns = BRunUtil.invoke(result, "testGlobalStream");
        BMap<String, BValue> origEmployee = (BMap<String, BValue>) returns[0];
        BMap<String, BValue> publishedEmployee = (BMap<String, BValue>) returns[1];
        BMap<String, BValue> modifiedOrigEmployee = (BMap<String, BValue>) returns[2];
        Assert.assertEquals(((BInteger) origEmployee.get("id")).intValue(), 0);
        Assert.assertTrue(origEmployee.get("name").stringValue().isEmpty());
        Assert.assertEquals(modifiedOrigEmployee.getType().getName(), publishedEmployee.getType().getName());
        Assert.assertEquals(((BInteger) modifiedOrigEmployee.get("id")).intValue(),
                ((BInteger) publishedEmployee.get("id")).intValue(),
                "Record field \"id\" of received event does not match that of published event");
        Assert.assertEquals(modifiedOrigEmployee.get("name").stringValue(),
                publishedEmployee.get("name").stringValue(),
                "Record field \"name\" of received event does not match that of published event");
    }

    @Test(description = "Test receipt of single record event with correct subscription and publishing")
    public void testStreamPublishingAndSubscriptionForRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForRecord");
        BMap<String, BValue> origEmployee = (BMap<String, BValue>) returns[0];
        BMap<String, BValue> publishedEmployee = (BMap<String, BValue>) returns[1];
        BMap<String, BValue> modifiedOrigEmployee = (BMap<String, BValue>) returns[2];
        Assert.assertEquals(((BInteger) origEmployee.get("id")).intValue(), 0);
        Assert.assertTrue(origEmployee.get("name").stringValue().isEmpty());
        Assert.assertEquals(publishedEmployee.getType().getName(), modifiedOrigEmployee.getType().getName());
        Assert.assertEquals(((BInteger) modifiedOrigEmployee.get("id")).intValue(),
                ((BInteger) publishedEmployee.get("id")).intValue(),
                "Record field \"id\" of received event does not match that of published event");
        Assert.assertEquals(modifiedOrigEmployee.get("name").stringValue(),
                publishedEmployee.get("name").stringValue(),
                "Record field \"name\" of received event does not match that of published event");
    }

    @Test(description = "Test receipt of multiple record events with correct subscription and publishing")
    public void testStreamPublishingAndSubscriptionForMultipleRecordEvents() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForMultipleRecordEvents");
        BRefValueArray publishedEmployeeEvents = (BRefValueArray) returns[0];
        BRefValueArray receivedEmployeeEvents = (BRefValueArray) returns[1];

        Assert.assertNotNull(publishedEmployeeEvents);
        Assert.assertNotNull(receivedEmployeeEvents);
        Assert.assertEquals(receivedEmployeeEvents.size(), publishedEmployeeEvents.size(), "Number of Employee "
                + "Events received does not match the number published");
        for (int i = 0; i < publishedEmployeeEvents.size(); i++) {
            BMap<String, BValue> publishedEmployeeEvent = (BMap<String, BValue>) publishedEmployeeEvents.get(i);
            BMap<String, BValue> receivedEmployeeEvent = (BMap<String, BValue>) receivedEmployeeEvents.get(i);
            Assert.assertEquals(publishedEmployeeEvent.getType().getName(), receivedEmployeeEvent.getType().getName());
            Assert.assertEquals(((BInteger) receivedEmployeeEvent.get("id")).intValue(),
                    ((BInteger) publishedEmployeeEvent.get("id")).intValue(),
                    "Record field \"id\" of received event does not match that of published event");
            Assert.assertEquals(receivedEmployeeEvent.get("name").stringValue(),
                    publishedEmployeeEvent.get("name").stringValue(),
                    "Record field \"name\" of received event does not match that of published event");
        }
    }

    @Test(description = "Test receipt of multiple integer events with correct subscription and publishing")
    public void testStreamPublishingAndSubscriptionForMultipleIntegerEvents() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForIntegerStream");
        BIntArray publishedIntegerEvents = (BIntArray) returns[0];
        BIntArray receivedIntegerEvents = (BIntArray) returns[1];

        Assert.assertNotNull(publishedIntegerEvents);
        Assert.assertNotNull(receivedIntegerEvents);
        Assert.assertEquals(receivedIntegerEvents.size(), publishedIntegerEvents.size(), "Number of Integer "
                + "Events received does not match the number published");
        for (int i = 0; i < publishedIntegerEvents.size(); i++) {
            Assert.assertEquals(receivedIntegerEvents.get(i), publishedIntegerEvents.get(i),
                                "Received Integer event does not match the published boolean event");
        }
    }

    @Test(description = "Test receipt of multiple boolean events with correct subscription and publishing")
    public void testStreamPublishingAndSubscriptionForMultipleBooleanEvents() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForBooleanStream");
        BBooleanArray publishedBooleanEvents = (BBooleanArray) returns[0];
        BBooleanArray receivedBooleanEvents = (BBooleanArray) returns[1];

        Assert.assertNotNull(publishedBooleanEvents);
        Assert.assertNotNull(receivedBooleanEvents);
        Assert.assertEquals(receivedBooleanEvents.size(), publishedBooleanEvents.size(), "Number of Boolean "
                + "Events received does not match the number published");
        for (int i = 0; i < publishedBooleanEvents.size(); i++) {
            Assert.assertEquals(receivedBooleanEvents.get(i), publishedBooleanEvents.get(i),
                                "Received boolean event does not match the published boolean event");
        }
    }

    @Test(description = "Test receipt of stream constrained by union type with correct subscription and publishing")
    public void testStreamPublishingAndSubscriptionForUnionTypeStream() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForUnionTypeStream");
        assertEventEquality((BRefValueArray) returns[0], (BRefValueArray) returns[1]);
    }

    @Test(description = "Test receipt of stream constrained by tuple type with correct subscription and publishing")
    public void testStreamPublishingAndSubscriptionForTupleTypeStream() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForTupleTypeStream");
        BRefValueArray publishedEvents = (BRefValueArray) returns[0];
        BRefValueArray receivedEvents = (BRefValueArray) returns[1];

        Assert.assertNotNull(publishedEvents);
        Assert.assertNotNull(receivedEvents);
        Assert.assertEquals(receivedEvents.size(), publishedEvents.size(), "Number of Events received does not "
                + "match the number published: Received" + receivedEvents.stringValue() + ", but Expected"
                + publishedEvents.stringValue() + ", ");
        for (int i = 0; i < publishedEvents.size(); i++) {
            Assert.assertEquals(receivedEvents.get(i), publishedEvents.get(i),
                                "Received event does not match the published event");
        }
    }

    @Test(description = "Test receipt of stream constrained by tuple type with correct subscription and publishing, "
            + "where the constrain types are assignable to the subscription function's parameter types")
    public void testStreamPublishingAndSubscriptionForAssignableTupleTypeStream() {
        BString s1 = new BString("Maryam");
        BInteger i1 = new BInteger(100);
        BString s2 = new BString("Ziyad");
        BInteger i2 = new BInteger(101);

        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForAssignableTupleTypeStream",
                                           new BValue[]{ s1, i1, s2, i2 });
        Assert.assertEquals(s1.stringValue(), returns[0].stringValue());
        Assert.assertEquals(i1.floatValue(), ((BFloat) returns[1]).value());
        Assert.assertEquals(s2.stringValue(), returns[2].stringValue());
        Assert.assertEquals(i2.floatValue(), ((BFloat) returns[3]).value());
    }

    @Test(description = "Test receipt of stream constrained by any type with correct subscription and publishing")
    public void testStreamPublishingAndSubscriptionForAnyTypeStream() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForAnyTypeStream");
        assertEventEquality((BRefValueArray) returns[0], (BRefValueArray) returns[1]);
    }

    @Test(description = "Test stream declaration without constraint")
    public void testStreamPublishingAndSubscriptionForUnconstrainedStream() {
        BValue[] returns = BRunUtil.invoke(result,
                                           "testStreamPublishingAndSubscriptionForUnconstrainedStream");
        assertEventEquality((BRefValueArray) returns[0], (BRefValueArray) returns[1]);
    }

    @Test(description = "Test stream publish with structurally equivalent records")
    public void testStreamsPublishingForStructurallyEquivalentRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamsPublishingForStructurallyEquivalentRecords");
        assertEventEquality((BRefValueArray) returns[0], (BRefValueArray) returns[1]);
    }

    @Test(description = "Test stream publish with structurally equivalent objects")
    public void testStreamsPublishingForStructurallyEquivalentObjects() {
        BValue[] returns = BRunUtil.invoke(result, "testStreamsPublishingForStructurallyEquivalentObjects");
        assertEventEquality((BRefValueArray) returns[0], (BRefValueArray) returns[1]);
    }

    @Test(description = "Test receipt of stream constrained by union type with correct publishing and subscribing "
            + "with a function to whose parameter types the stream constraint is assignable")
    public void testStreamPublishingAndSubscriptionForAssignableUnionTypeStream() {
        BInteger intVal = new BInteger(100);
        BValue[] returns = BRunUtil.invoke(result, "testStreamPublishingAndSubscriptionForAssignableUnionTypeStream",
                                           new BValue[]{ intVal });
        BRefValueArray publishedEvents = (BRefValueArray) returns[0];
        BRefValueArray receivedEvents = (BRefValueArray) returns[1];

        Assert.assertNotNull(publishedEvents);
        Assert.assertNotNull(receivedEvents);
        Assert.assertEquals(receivedEvents.size(), publishedEvents.size(), "Number of Events received does not "
                + "match the number published");
        for (int i = 0; i < publishedEvents.size() - 1; i++) {
            Assert.assertEquals(receivedEvents.get(i), publishedEvents.get(i),
                                "Received event does not match the published event");
        }
        Assert.assertEquals(receivedEvents.get(receivedEvents.size() - 1).value(), intVal.floatValue(),
                            "Received event does not match the expected casted value");
    }

    private void assertEventEquality(BRefValueArray publishedEvents, BRefValueArray receivedEvents) {
        Assert.assertNotNull(publishedEvents);
        Assert.assertNotNull(receivedEvents);
        Assert.assertEquals(receivedEvents.size(), publishedEvents.size(), "Number of Events received does not "
                + "match the number published");
        for (int i = 0; i < publishedEvents.size(); i++) {
            Assert.assertEquals(receivedEvents.get(i), publishedEvents.get(i),
                                "Received event does not match the published event");
        }
    }
}
