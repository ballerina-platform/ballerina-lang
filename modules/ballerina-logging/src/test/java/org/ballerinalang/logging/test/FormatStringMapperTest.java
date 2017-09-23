/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.logging.test;

import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.util.Constants;
import org.ballerinalang.logging.util.FormatStringMapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Tests for the FormatStringMapper to ensure the format strings are parsed and mapped correctly to JDK format strings
 */
public class FormatStringMapperTest {

    private PrintStream original;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        original = BLogManager.stdErr;
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws IOException {
        BLogManager.stdErr = original;
    }

    @Test
    public void testInvalidTimestamp1() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(out)) {
            BLogManager.stdErr = printStream;
            String invalidFormat = "{{timestamp}}[] {{level}} [{{package}}:{{unit}}] " +
                    "[{{file}}:{{line}}] [{{worker}}] - \"{{msg}}\" {{err}}";
            String expectedLogFormat = "%1$s %2$s [%4$s:%5$s] [%6$s:%7$s] [%8$s] - \"%9$s\" %10$s";

            String jdkLogFormat = FormatStringMapper.getInstance().buildJDKLogFormat(Constants.BALLERINA_LOG_FORMAT,
                                                                                     invalidFormat);

            Assert.assertEquals(jdkLogFormat, expectedLogFormat);
        } finally {
            BLogManager.stdErr = original;
        }
    }

    @Test
    public void testInvalidTimestamp2() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(out)) {
            BLogManager.stdErr = printStream;
            String invalidFormat = "{{timestamp}} {{level}} [{{package}}:{{unit}}] " +
                    "[{{file}}:{{line}}] [{{worker}}] - \"{{msg}}\" {{err}}";
            String expectedMsg = "Invalid timestamp format detected. Defaulting to: \"yyyy-MM-dd HH:mm:ss,SSS\"\n";

            FormatStringMapper.getInstance().buildJDKLogFormat(Constants.BALLERINA_LOG_FORMAT, invalidFormat);
            Assert.assertEquals(out.toString(), expectedMsg);
        } finally {
            BLogManager.stdErr = original;
        }
    }
}
