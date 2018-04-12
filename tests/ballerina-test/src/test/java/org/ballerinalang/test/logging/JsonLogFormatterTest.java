/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.logging;

import org.ballerinalang.logging.formatters.JsonLogFormatter;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.testng.Assert.fail;

/**
 * Test cases JsonLogFormatter.
 */
public class JsonLogFormatterTest extends TestNG {

    JsonLogFormatter jsonLogFormatter;
    LogRecord logRecord;

    private static String traceLogMessage = "[id: 0x65d56de4, correlatedSource: n/a, "
            + "host:localhost/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:52872] OUTBOUND: "
            + "DefaultFullHttpResponse(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf"
            + "(ridx: 0, widx: 55, cap: 55, components=1))\n" + "HTTP/1.1 200 OK\ncontent-type: application/json\n"
            + "content-length: 55\n" + "server: wso2-http-transport\ndate: Fri, 16 Mar 2018 14:26:12 +0530, 55B\n"
            + "{\"message\":\"Max entity body size resource is invoked.\"} ";

    @BeforeClass(description = "Initialize variables required for test.")
    protected void setUp() throws Exception {
        jsonLogFormatter = new JsonLogFormatter();
        logRecord = new LogRecord(Level.FINEST, traceLogMessage);
    }

    @Test(description = "Test for service availability check")
    public void testFormatNull() {
        try {
            jsonLogFormatter.format(null);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }
    }

    @Test(description = "Test default formatting of JsonLogFormatter.")
    public void testJsonLogFormat() {
        String formatterMessagePart = "[id: 0x65d56de4, correlatedSource: n/a, host:localhost/0:0:0:0:0:0:0:1:9090 - "
                + "remote:/0:0:0:0:0:0:0:1:52872] OUTBOUND: DefaultFullHttpResponse(decodeResult: success, version: "
                + "HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 55, cap: 55, components=1))\\nHTTP/1.1 200 OK\\n"
                + "content-type: application/json\\ncontent-length: 55\\nserver: wso2-http-transport\\ndate: Fri, "
                + "16 Mar 2018 14:26:12 +0530, 55B\\n{\\\"message\\\":\\\"Max entity body size resource "
                + "is invoked.\\\"}";

        String str = jsonLogFormatter.format(logRecord);
        Assert.assertTrue((str.indexOf("record") > 0), "Json log format doesn't contain record.");
        Assert.assertTrue(str.contains(formatterMessagePart), "Json log format is incorrect.");
    }

    @Test(description = "Test setting custom values for log records.")
    public void testFormat() {
        String str = jsonLogFormatter.format(logRecord);

        logRecord.setMessage(traceLogMessage + " {0,number}");
        logRecord.setLoggerName("logger");
        logRecord.setResourceBundleName("rb name");
        logRecord.setSourceClassName("class");
        logRecord.setSourceMethodName("method");
        logRecord.setParameters(new Object[] { new Integer(100), new Object() });
        logRecord.setThreadID(1000);
        logRecord.setSequenceNumber(12321312);
        logRecord.setMillis(0);
        str = jsonLogFormatter.format(logRecord);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(12321312);
        Assert.assertTrue(str.indexOf(String.valueOf(cal.get(Calendar.YEAR))) >= 0,
                "Incorrect year in the json log string.");
        Assert.assertTrue(str.indexOf("class") > 0, "Log record doesn't contain class.");
        Assert.assertTrue(str.indexOf("method") > 0, "Log record doesn't contain method.");
        Assert.assertTrue(str.indexOf("100") > 0, "Log record doesn't contain parameters.");
        Assert.assertTrue(str.indexOf(Level.FINEST.getLocalizedName()) > 0, "Json log format doesn't contain record.");
    }

    @Test(description = "Test header string for a set of formatted records.")
    public void testGetHead() {
        Assert.assertEquals("", jsonLogFormatter.getHead(null),
                "Incorrect header string for a set of formatted records.");
    }

    @Test(description = "Test tail string for a set of formatted records.")
    public void testGetTail() {
        Assert.assertEquals("", jsonLogFormatter.getTail(null),
                "Incorrect the tail string for a set of formatted records.");
    }
}
