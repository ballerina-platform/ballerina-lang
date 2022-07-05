/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.benchmark;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Test Native util functions used with benchmarks.
 */
public class BenchmarkUtilTest {
    private final String printFuncName = "testPrintAndPrintln";
    private CompileResult compileResult;
    private PrintStream original;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        original = System.out;
        compileResult = BCompileUtil.compileOffline("test-src/benchmark/benchmark-util-test.bal");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        System.setOut(original);
    }

    @Test
    public void testStringPrintAndPrintln() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final String s1 = "Hello World...!!!";
            final String s2 = "A Greeting from Ballerina...!!!";
            final String expected = s1 + "\n" + s2;
            Object[] args = {StringUtils.fromString(s1), StringUtils.fromString(s2)};
            BRunUtil.invoke(compileResult, printFuncName + "String", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testIntPrintAndPrintln() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final long v1 = 1000;
            final long v2 = 1;
            final String expected = v1 + "\n" + v2;
            Object[] args = {v1, v2};
            BRunUtil.invoke(compileResult, printFuncName + "Int", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testFloatPrintAndPrintln() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final double v1 = 1000;
            final double v2 = 1;
            final String expected = v1 + "\n" + v2;
            Object[] args = {v1, v2};
            BRunUtil.invoke(compileResult, printFuncName + "Float", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testBooleanPrintAndPrintln() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final boolean v1 = false;
            final boolean v2 = true;
            final String expected = v1 + "\n" + v2;
            Object[] args = {v1, v2};
            BRunUtil.invoke(compileResult, printFuncName + "Boolean", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testConnectorPrintAndPrintln() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final String expected = "object Foo\nobject Foo";
            BRunUtil.invoke(compileResult, printFuncName + "Connector");
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testFunctionPointerPrintAndPrintln() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final String expected = "function function (int,int) returns (int)\n" +
                    "function function (int,int) returns (int)";
            BRunUtil.invoke(compileResult, printFuncName + "FunctionPointer");
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrintVarargs() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final String s1 = "Hello World...!!!";
            final String s2 = "A Greeting from Ballerina...!!!";
            final String s3 = "Adios";
            final String expected = s1 + s2 + s3;
            Object[] args = {StringUtils.fromString(s1), StringUtils.fromString(s2), StringUtils.fromString(s3)};
            BRunUtil.invoke(compileResult, "testPrintVarargs", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrintMixVarargs() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final String s1 = "Hello World...!!!";
            final long l1 = 123456789L;
            final double d1 = 123456789.123456789;
            final boolean b1 = true;
            final String expected = s1 + l1 + d1 + b1;
            Object[] args = {StringUtils.fromString(s1), l1, d1, b1};
            BRunUtil.invoke(compileResult, "testPrintMixVarargs", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrintlnVarargs() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final String s1 = "Hello World...!!!";
            final String s2 = "A Greeting from Ballerina...!!!";
            final String s3 = "Adios";
            final String expected = s1 + s2 + s3 + "\n";
            Object[] args = {StringUtils.fromString(s1), StringUtils.fromString(s2), StringUtils.fromString(s3)};
            BRunUtil.invoke(compileResult, "testPrintlnVarargs", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            System.setOut(original);
        }
    }

    @Test(description = "Test new line character in string")
    public void testNewlineCharacter() {
        PrintStream mainStream = System.out;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(out));
            BRunUtil.invoke(compileResult, "printNewline");
            String outPut = out.toString();
            Assert.assertNotNull(outPut, "string is not printed");
            //getting the last new line character
            Assert.assertEquals(outPut.charAt(outPut.length() - 1), '\n'
                    , "New line character not found in output string");
        } catch (IOException e) {
            //ignore
        } finally {
            System.setOut(mainStream);
        }

    }

    @Test
    public void testFormatBooleanTrue() {
        BArray fArgs = getAnyArrayValue(new Object[] {true});
        Object[] args = {StringUtils.fromString("%b"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "true");
    }

    private BArray getAnyArrayValue(Object[] values) {
        return ValueCreator.createArrayValue(values, TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY));
    }

    @Test
    public void testFormatBooleanFalse() {
        BArray fArgs = getAnyArrayValue(new Object[] {false});
        Object[] args = {StringUtils.fromString("%b"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "false");
    }

    @Test
    public void testFormatDecimal() {
        BArray fArgs = getAnyArrayValue(new Object[] {65L});
        Object[] args = {StringUtils.fromString("%d"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "65");
    }

    @Test
    public void testFormatFloat() {
        BArray fArgs = getAnyArrayValue(new Object[] {3.25d});
        Object[] args = {StringUtils.fromString("%f"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "3.250000");
    }

    @Test
    public void testFormatString() {
        String name = "John";
        BArray fArgs = getAnyArrayValue(new Object[]{StringUtils.fromString(name)});
        Object[] args = {StringUtils.fromString("%s"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), name);
    }

    @Test
    public void testFormatHex() {
        BArray fArgs = getAnyArrayValue(new Object[]{57005L});
        Object[] args = {StringUtils.fromString("%x"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "dead");
    }

    @Test
    public void testFormatIntArray() {
        BArray arr = ValueCreator.createArrayValue(new Object[]{111L, 222L, 333L},
                TypeCreator.createArrayType(PredefinedTypes.TYPE_INT));
        BArray fArgs = getAnyArrayValue(new Object[]{arr});
        Object[] args = {StringUtils.fromString("%s"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "[111,222,333]");
    }

    @Test
    public void testFormatLiteralPercentChar() {
        BArray fArgs = getAnyArrayValue(new Object[]{StringUtils.fromString("test")});
        Object[] args = {StringUtils.fromString("%% %s"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "% test");
    }

    @Test
    public void testFormatStringWithPadding() {
        BArray fArgs = getAnyArrayValue(new Object[]{StringUtils.fromString("Hello Ballerina")});
        Object[] args = {StringUtils.fromString("%9.2s"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "       He");
    }

    @Test
    public void testFormatFloatWithPadding() {
        BArray fArgs = getAnyArrayValue(new Object[]{123456789.9876543d});
        Object[] args = {StringUtils.fromString("%5.4f"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "123456789.9877");
    }

    @Test
    public void testFormatDecimalWithPadding() {
        BArray fArgs = getAnyArrayValue(new Object[]{12345L});
        Object[] args = {StringUtils.fromString("%15d"), fArgs};
        Object returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns.toString(), "          12345");
    }

    @Test
    public void testSprintfMix() {
        Object[] args = {StringUtils.fromString("the %s jumped over the %s, %d times"),
                StringUtils.fromString("cow"), StringUtils.fromString("moon"), 2L};
        Object returns = BRunUtil.invoke(compileResult, "testSprintfMix", args);
        Assert.assertEquals(returns.toString(), "the cow jumped over the moon, 2 times");
    }

    @Test
    public void testSprintfForNilInputString() {
        Object returns = BRunUtil.invoke(compileResult, "testSprintfNilString");
        Assert.assertTrue(returns.toString().isEmpty());
    }

}
