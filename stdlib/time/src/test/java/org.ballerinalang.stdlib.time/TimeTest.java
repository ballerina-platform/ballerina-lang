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
package org.ballerinalang.stdlib.time;

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
        result = BCompileUtil.compile("test-src/time-type.bal");
    }

    @Test(description = "Test current time representation.")
    public void testCurrentTime() {
        BValue[] returns = BRunUtil.invoke(result, "testCurrentTime");
        Assert.assertTrue(((BInteger) returns[0]).intValue() > 1498621376460L);
    }

    @Test(description = "Test nanoTime function.")
    public void testNanoTime() {
        BValue[] returns = BRunUtil.invoke(result, "testNanoTime");
        Assert.assertEquals(returns[0].getClass().getSimpleName(), "BInteger");
        Assert.assertTrue(((BInteger) returns[0]).intValue() > 0, "nanoTime returned should be greater than zero");
        Assert.assertTrue(((BInteger) returns[0]).intValue() < System.nanoTime(),
                "nanoTime returned should be less than the current system nano time");
    }

    @Test(description = "Test create time with offset ID provided.")
    public void testCreateTimeWithZoneID() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTimeWithZoneID");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
        Assert.assertEquals((returns[1]).stringValue(), "America/Panama");
    }

    @Test(description = "Test create time with offset values provided.")
    public void testCreateTimeWithOffset() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTimeWithOffset");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
        Assert.assertEquals((returns[1]).stringValue(), "-05:00");
    }

    @Test(description = "Test create time with no zone info provided.")
    public void testCreateTimeWithNoZone() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTimeWithNoZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382000L);
    }

    @Test(description = "Test parsing a given time string to time.")
    public void testParseTime() {
        BValue[] returns = BRunUtil.invoke(result, "testParseTime");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1498488382444L);
        Assert.assertEquals((returns[1]).stringValue(), "-05:00");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), -18000);
    }

    @Test(description = "Test parsing a given time string to time.")
    public void testParseTimeWithTimePartOnly() {
        BValue[] returns = BRunUtil.invoke(result, "testParseTimeWithTimePartOnly");
        Assert.assertTrue(((BInteger) returns[0]).intValue() > 0);
        Assert.assertEquals((returns[1]).stringValue(), "09:46:22");
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

    @Test(description = "Test To String function.")
    public void testToStringWithCreateTime() {
        BValue[] returns = BRunUtil.invoke(result, "testToStringWithCreateTime");
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22-05:00");
    }

    @Test(description = "Test new time create using year month date values.")
    public void testCreateDateTime() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateDateTime");
        Assert.assertEquals((returns[0]).stringValue(), "2017-03-28T23:42:45.554-05:00");
    }

    @Test(description = "Test Format Time according to the given format.")
    public void testFormatTime() {
        BValue[] returns = BRunUtil.invoke(result, "testFormatTime");
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22.444-0500");
    }

    @Test(description = "Test Format Time according to the given format.")
    public void testFormatTimeToRFC1123() {
        BValue[] returns = BRunUtil.invoke(result, "testFormatTimeToRFC1123");
        Assert.assertEquals((returns[0]).stringValue(), "Mon, 26 Jun 2017 09:46:22 -0500");
    }

    @Test(description = "Test Get Year Functions for date time values.")
    public void testGetFunctions() {
        BValue[] returns = BRunUtil.invoke(result, "testGetFunctions");
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
        BValue[] returns = BRunUtil.invoke(result, "testGetDateFunction");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2016);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
    }

    @Test(description = "Test Get Time Function.")
    public void testGetTimeFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testGetTimeFunction");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 18);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 56);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 555);
    }

    @Test(description = "Test add Duration to Time.")
    public void testAddDuration() {
        BValue[] returns = BRunUtil.invoke(result, "testAddDuration");
        Assert.assertEquals((returns[0]).stringValue(), "2018-07-27T10:47:23.445-0500");
    }

    @Test(description = "Test subtract Duration to Time.")
    public void testSubtractDuration() {
        BValue[] returns = BRunUtil.invoke(result, "testSubtractDuration");
        Assert.assertEquals((returns[0]).stringValue(), "2015-01-31T08:45:21.443-0500");
    }

    @Test(description = "Test changing the timezone.")
    public void testToTimezone() {
        BValue[] returns = BRunUtil.invoke(result, "testToTimezone");
        Assert.assertEquals((returns[0]).stringValue(), "2016-03-01T18:56:23.555-05:00");
        Assert.assertEquals((returns[1]).stringValue(), "2016-03-02T05:26:23.555+05:30");
    }

    @Test(description = "Test changing the timezone with a time already having a timezone.")
    public void testToTimezoneWithDateTime() {
        BValue[] returns = BRunUtil.invoke(result, "testToTimezoneWithDateTime");
        Assert.assertEquals((returns[0]).stringValue(), "2016-03-01T20:16:22.444+0530");
    }

    @Test(description = "Test Time struct create with struct initialization.")
    public void testManualTimeCreate() {
        BValue[] returns = BRunUtil.invoke(result, "testManualTimeCreate");
        Assert.assertEquals((returns[0]).stringValue(), "2017-06-26T09:46:22-05:00");
    }

    @Test(description = "Test Time struct create with struct initialization with no zone information.")
    public void testManualTimeCreateWithNoZone() {
        BValue[] returns = BRunUtil.invoke(result, "testManualTimeCreateWithNoZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test(description = "Test Time struct create with struct initialization with no zone information.")
    public void testManualTimeCreateWithEmptyZone() {
        BValue[] returns = BRunUtil.invoke(result, "testManualTimeCreateWithEmptyZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    // TODO: Re-enable this when #19893 fixed
    @Test(
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*TimeError message=invalid timezone id: test\n"
                  + "\tat ballerina.time:getYear.*",
            enabled = false
    )
    public void testManualTimeCreateWithInvalidZone() {
        BValue[] returns = BRunUtil.invoke(result, "testManualTimeCreateWithInvalidZone");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2017);
    }

    @Test
    public void testCreateDateTimeWithInvalidZone() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateDateTimeWithInvalidZone");
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BError) returns[0]).getMessage(), "invalid timezone id: TEST");
    }

    @Test
    public void testParseTimeValidPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testParseTimeValidPattern");
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BError) returns[0]).getMessage(), "invalid pattern: test");
    }

    @Test
    public void testParseTimeFormatMismatch() {
        BValue[] returns = BRunUtil.invoke(result, "testParseTimeFormatMismatch");
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BError) returns[0]).getMessage(),
                "parse date \"2017-06-26T09:46:22.444-0500\" for the format \"yyyy-MM-dd\" failed:Text " +
                        "'2017-06-26T09:46:22.444-0500' could not be parsed, unparsed text found at index 10");
    }

    @Test
    public void testFormatTimeInvalidPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testFormatTimeInvalidPattern");
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BError) returns[0]).getMessage(), "Invalid Pattern: test");
    }

    @Test
    public void testToTimezoneWithInvalidZone() {
        BValue[] returns = BRunUtil.invoke(result, "testToTimezoneWithInvalidZone");
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BError) returns[0]).getMessage(), "invalid timezone id: test");
    }

    @Test(description = "Test parsing a given time string to time.")
    public void testParseTimeWithDifferentFormats() {
        BValue[] returns = BRunUtil.invoke(result, "testParseTimeWithDifferentFormats");
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
