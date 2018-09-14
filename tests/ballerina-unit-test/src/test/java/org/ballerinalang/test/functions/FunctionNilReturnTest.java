/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValueType;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Test nil return from functions.
 */
public class FunctionNilReturnTest {

    private CompileResult compileResult;
    private PrintStream original;
    private static final String EXPECTED_OUTPUT = "Hello\n\nBallerina\n";

    @BeforeClass(alwaysRun = true)
    public void setup() {
        original = System.out;
        compileResult = BCompileUtil.compile("test-src/functions/function-nil-return.bal");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        System.setOut(original);
    }

    @Test
    public void testPrint1() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            BRunUtil.invoke(compileResult, "testPrint1", new BValueType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrint2() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            BRunUtil.invoke(compileResult, "testPrint2", new BValueType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrint3() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            BRunUtil.invoke(compileResult, "testPrint3", new BValueType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrint4() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            BRunUtil.invoke(compileResult, "testPrint4", new BValueType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrintInMatchBlock() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            BRunUtil.invoke(compileResult, "testPrintInMatchBlock", new BValueType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrintInWorkers() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            BRunUtil.invoke(compileResult, "testPrintInWorkers", new BValueType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }
}
