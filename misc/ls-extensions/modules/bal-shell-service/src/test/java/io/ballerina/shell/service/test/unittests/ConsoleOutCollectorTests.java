/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.shell.service.test.unittests;

import io.ballerina.shell.service.ConsoleOutCollector;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Unit test for ConsoleOutCollector.
 *
 * @since 2201.1.1
 */
public class ConsoleOutCollectorTests {
    private PrintStream original;
    private ConsoleOutCollector consoleOutCollector;

    @BeforeClass
    public void setUp() {
        this.original = System.out;
        this.consoleOutCollector = new ConsoleOutCollector();
        System.setOut(new PrintStream(this.consoleOutCollector));
    }

    @AfterClass
    public void tearDown() {
        System.setOut(this.original);
    }

    @Test(description = "Test with output collection")
    public void testConsoleCollect() throws IOException {
        this.consoleOutCollector.write("first line\nsecond line\n".getBytes());
        Assert.assertEquals(this.consoleOutCollector.getLines().size(), 2);
        Assert.assertEquals(this.consoleOutCollector.getLines().get(0), "first line");
        Assert.assertEquals(this.consoleOutCollector.getLines().get(1), "second line");
    }
}
