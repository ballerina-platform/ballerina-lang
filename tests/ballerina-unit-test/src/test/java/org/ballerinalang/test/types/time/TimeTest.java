/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.time;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of time.
 */
public class TimeTest {

    private CompileResult result;
    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/time/time-type.bal");
    }

    @Test(description = "Test current time representation.")
    public void testCurrentTime() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testCurrentTime", args);
        Assert.assertTrue(((BInteger) returns[0]).intValue() > 1498621376460L);
    }

    @Test(description = "Test nanoTime function.")
    public void testNanoTime() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testNanoTime", args);
        Assert.assertEquals(returns[0].getClass().getSimpleName(), "BInteger");
        Assert.assertTrue(((BInteger) returns[0]).intValue() > 0, "nanoTime returned should be greater than zero");
        Assert.assertTrue(((BInteger) returns[0]).intValue() < System.nanoTime(),
                "nanoTime returned should be less than the current system nano time");
    }

    @Test(description = "Test create time with offset ID provided.")
    public void testCreateTimeWithZoneID() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testCreateTimeWithZoneID", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
        Assert.assertEquals((returns[1]).stringValue(), "America/Panama");
    }

    @Test(description = "Test create time with offset values provided.")
    public void testCreateTimeWithOffset() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testCreateTimeWithOffset", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
        Assert.assertEquals((returns[1]).stringValue(), "-05:00");
    }

    @Test(description = "Test create time with no zone info provided.")
    public void testCreateTimeWithNoZone() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testCreateTimeWithNoZone", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
    }

    @Test(description = "Test parsing a given time string to time.")
    public void testParseTime() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testParseTime", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382444L);
        Assert.assertEquals((returns[1]).stringValue(), "-05:00");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), -18000);
    }

    @Test(description = "Test parsing a given time string to RFC 1123 format")
    public void testParseToRFC1123Time() {
        BValue[] args = {new BString("Wed, 28 Mar 2018 11:56:23 +0530")};
        BValue[] returns = BRunUtil.invoke(result, "testParseRFC1123Time", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1522218383000L);
        Assert.assertEquals((returns[1]).stringValue(), "+05:30");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 19800);

        args = new BValue[]{new BString("Tue, 27 Mar 2018 10:00:24 GMT")};
        returns = BRunUtil.invoke(result, "testParseRFC1123Time", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1522144824000L);
        Assert.assertEquals((returns[1]).stringValue(), "Z");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
    }

    @Test(description = "Test To String funciton.")
    public void testToStringWithCreateTime() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testToStringWithCreateTime", args);
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22-05:00");
    }

    @Test(description = "Test new time create using year month date values.")
    public void testCreateDateTime() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testCreateDateTime", args);
        Assert.assertEquals((returns[0]).stringValue(), "2017-03-28T23:42:45.554-05:00");
    }

    @Test(description = "Test Format Time according to the given format.")
    public void testFormatTime() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testFormatTime", args);
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22.444-0500");
    }

    @Test(description = "Test Format Time according to the given format.")
    public void testFormatTimeToRFC1123() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testFormatTimeToRFC1123", args);
        Assert.assertEquals((returns[0]).stringValue(), "Mon, 26 Jun 2017 09:46:22 -0500");
    }

    @Test(description = "Test Get Year Functions for date time values.")
    public void testGetFunctions() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testGetFunctions", args);
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
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testGetDateFunction", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2016);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
    }

    @Test(description = "Test Get Time Function.")
    public void testGetTimeFunction() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testGetTimeFunction", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 18);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 56);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 555);
    }

    @Test(description = "Test add Duration to Time.")
    public void testAddDuration() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testAddDuration", args);
        Assert.assertEquals((returns[0]).stringValue(), "2018-07-27T10:47:23.445-0500");
    }

    @Test(description = "Test subtract Duration to Time.")
    public void testSubtractDuration() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSubtractDuration", args);
        Assert.assertEquals((returns[0]).stringValue(), "2015-01-31T08:45:21.443-0500");
    }

    @Test(description = "Test changing the timezone.")
    public void testToTimezone() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testToTimezone", args);
        Assert.assertEquals((returns[0]).stringValue(), "2016-03-01T18:56:23.555-05:00");
        Assert.assertEquals((returns[1]).stringValue(), "2016-03-02T05:26:23.555+05:30");
    }

    @Test(description = "Test changing the timezone with a time already having a timezone.")
    public void testToTimezoneWithDateTime() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testToTimezoneWithDateTime", args);
        Assert.assertEquals((returns[0]).stringValue(), "2016-03-01T20:16:22.444+0530");
    }

    @Test(description = "Test Time struct create with struct initialization.")
    public void testManualTimeCreate() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testManualTimeCreate", args);
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22-05:00");
    }

    @Test(description = "Test Time struct create with struct initialization with no zone information.")
    public void testManualTimeCreateWithNoZone() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testManualTimeCreateWithNoZone", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test(description = "Test Time struct create with struct initialization with no zone information.")
    public void testManualTimeCreateWithEmptyZone() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testManualTimeCreateWithEmptyZone", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*unknown time-zone ID: test.*")
    public void testManualTimeCreateWithInvalidZone() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testManualTimeCreateWithInvalidZone", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid pattern for parsing test.*")
    public void testParseTimenvalidPattern() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testParseTimenvalidPattern", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*parse date \"2017-06-26T09:46:22.444-0500\" for the format "
                  + "\"yyyy-MM-dd\" failed.*")
    public void testParseTimenFormatMismatch() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testParseTimenFormatMismatch", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid pattern for formatting: test.*")
    public void testFormatTimeInvalidPattern() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testFormatTimeInvalidPattern", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid timezone id: test.*")
    public void testToTimezoneWithInvalidZone() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testToTimezoneWithInvalidZone", args);
        Assert.assertEquals((returns[0]).stringValue(), "2016-03-02T05:26:23.555+0530");
    }

    @Test(description = "Test parsing a given time string to time.")
    public void testParseTimeWithDifferentFormats() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testParseTimeWithDifferentFormats", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 31);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 16);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 59);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 58);
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 999);
        Assert.assertEquals(returns[7].stringValue(), "2017-09-23");
        Assert.assertEquals(returns[8].stringValue(), "2015-02-15+0800");
        Assert.assertEquals(returns[9].stringValue(), "08-23-59-544:+0700");
        Assert.assertEquals(returns[10].stringValue(), "2014-05-29-23:44:59.544");
    }
}
