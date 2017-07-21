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

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for validating the Time and associated functions.
 */
public class TimeTest {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/timeTest.bal");
    }

    @Test(description = "Test current time representation.")
    public void testCurrentTime() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCurrentTime");
        Assert.assertTrue(((BInteger) returns[0]).intValue() > 1498621376460L);
        //Assert.assertEquals((returns[1]).stringValue(), "Asia/Colombo");
        //Assert.assertTrue(((BInteger) returns[2]).intValue() > 0);
    }

    @Test(description = "Test create time with offset ID provided.")
    public void testCreateTimeWithZoneID() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCreateTimeWithZoneID");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
        Assert.assertEquals((returns[1]).stringValue(), "America/Panama");
    }

    @Test(description = "Test create time with offset values provided.")
    public void testCreateTimeWithOffset() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCreateTimeWithOffset");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
        Assert.assertEquals((returns[1]).stringValue(), "-05:00");
    }

    @Test(description = "Test create time with no zone info provided.")
    public void testCreateTimeWithNoZone() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCreateTimeWithNoZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
        //Assert.assertEquals((returns[1]).stringValue(), "Asia/Colombo");
        //Assert.assertEquals(((BInteger) returns[2]).intValue(), 19800);
    }

    @Test(description = "Test create time with no zone info provided.")
    public void testCreateTimeWithNullZone() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCreateTimeWithNullZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test(description = "Test parsing a given time string to time.")
    public void testParseTime() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testParseTime");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382444L);
        Assert.assertEquals((returns[1]).stringValue(), "-05:00");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), -18000);
    }

    @Test(description = "Test To String funciton.")
    public void testToStringWithCreateTime() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToStringWithCreateTime");
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22-05:00");
    }

    @Test(description = "Test new time create using year month date values.")
    public void testCreateDateTime() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCreateDateTime");
        Assert.assertEquals((returns[0]).stringValue(), "2017-03-28T23:42:45.554-05:00");
    }

    @Test(description = "Test Format Time according to the given format.")
    public void testFormatTime() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testFormatTime");
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22.444-0500");
    }

    @Test(description = "Test Get Year Functions for date time values.")
    public void testGetFunctions() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetFunctions");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2016);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 18);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 56);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 555);
        Assert.assertEquals((returns[7]).stringValue(), "TUESDAY");
    }


    @Test(description = "Test Get Date Function.")
    public void testGetDateFunction() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetDateFunction");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2016);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
    }

    @Test(description = "Test Get Time Function.")
    public void testGetTimeFunction() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetTimeFunction");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 18);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 56);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 555);
    }

    @Test(description = "Test add Duration to Time.")
    public void testAddDuration() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAddDuration");
        Assert.assertEquals((returns[0]).stringValue(), "2018-07-27T10:47:23.445-0500");
    }

    @Test(description = "Test subtract Duration to Time.")
    public void testSubtractDuration() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSubtractDuration");
        Assert.assertEquals((returns[0]).stringValue(), "2015-01-31T08:45:21.443-0500");
    }

    @Test(description = "Test changing the timezone.")
    public void testToTimezone() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToTimezone");
        Assert.assertEquals((returns[0]).stringValue(), "2016-03-01T18:56:23.555-05:00");
        Assert.assertEquals((returns[1]).stringValue(), "2016-03-02T05:26:23.555+05:30");
    }

    @Test(description = "Test changing the timezone with a time already having a timezone.")
    public void testToTimezoneWithDateTime() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToTimezoneWithDateTime");
        Assert.assertEquals((returns[0]).stringValue(), "2016-03-01T20:16:22.444+0530");
    }

    @Test(description = "Test Time struct create with struct initialization.")
    public void testManualTimeCreate() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testManualTimeCreate");
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22-05:00");
    }

    @Test(description = "Test Time struct create with struct initialization with no zone information.")
    public void testManualTimeCreateWithNoZone() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testManualTimeCreateWithNoZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test(description = "Test Time struct create with struct initialization with no zone information.")
    public void testManualTimeCreateWithEmptyZone() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testManualTimeCreateWithEmptyZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*unknown time-zone ID: test.*")
    public void testManualTimeCreateWithInvalidZone() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testManualTimeCreateWithInvalidZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid pattern for parsing test.*")
    public void testParseTimenvalidPattern() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testParseTimenvalidPattern");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*parse date 2017-06-26T09:46:22.444-0500 for the format "
                  + "yyyy-MM-dd failed.*")
    public void testParseTimenFormatMismatch() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testParseTimenFormatMismatch");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid pattern for formatting test.*")
    public void testFormatTimeInvalidPattern() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testFormatTimeInvalidPattern");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid timezone id test.*")
    public void testToTimezoneWithInvalidZone() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToTimezoneWithInvalidZone");
        Assert.assertEquals((returns[0]).stringValue(), "2016-03-02T05:26:23.555+0530");
    }
}
