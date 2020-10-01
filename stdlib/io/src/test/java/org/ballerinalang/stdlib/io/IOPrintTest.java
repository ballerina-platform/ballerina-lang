/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.io;

import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BValueType;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Test Native functions in ballerina/io.
 */
public class IOPrintTest {

    private CompileResult compileResult;
    private final String printFuncName = "testPrintAndPrintln";

    private PrintStream original;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        original = System.out;
        compileResult = BCompileUtil.compileOffline("test-src/io/io-test.bal");
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

            BValueType[] args = {new BString(s1), new BString(s2)};
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
            final int v1 = 1000;
            final int v2 = 1;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BInteger(v1), new BInteger(v2)};
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
            final float v1 = 1000;
            final float v2 = 1;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BFloat(v1), new BFloat(v2)};
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

            BValueType[] args = {new BBoolean(v1), new BBoolean(v2)};
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

            BValueType[] args = {new BString(s1), new BString(s2), new BString(s3)};
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

            BValueType[] args = {new BString(s1), new BInteger(l1), new BFloat(d1), new BBoolean(b1)};
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

            BValueType[] args = {new BString(s1), new BString(s2), new BString(s3)};
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
            BValue[] args = {};
            BRunUtil.invoke(compileResult, "printNewline", args);
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
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BBoolean(true));
        BValue[] args = {new BString("%b"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testFormatBooleanFalse() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BBoolean(false));
        BValue[] args = {new BString("%b"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "false");
    }

    @Test
    public void testFormatDecimal() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BInteger(65));
        BValue[] args = {new BString("%d"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "65");
    }

    @Test
    public void testFormatFloat() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BFloat(3.25));
        BValue[] args = {new BString("%f"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "3.250000");
    }

    @Test
    public void testFormatString() {
        String name = "John";
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BString(name));
        BValue[] args = {new BString("%s"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), name);
    }

    @Test
    public void testFormatHex() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BInteger(57005));
        BValue[] args = {new BString("%x"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "dead");
    }

    @Test
    public void testFormatIntArray() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        BValueArray arr = new BValueArray(BTypes.typeInt);
        arr.add(0, 111L);
        arr.add(1, 222L);
        arr.add(2, 333L);

        fArgs.add(0, arr);
        BValue[] args = {new BString("%s"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "[111,222,333]");
    }

    @Test
    public void testFormatLiteralPercentChar() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BString("test"));
        BValue[] args = {new BString("%% %s"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "% test");
    }

    @Test
    public void testFormatStringWithPadding() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BString("Hello Ballerina"));
        BValue[] args = {new BString("%9.2s"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "       He");
    }

    @Test
    public void testFormatFloatWithPadding() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BFloat(123456789.9876543));
        BValue[] args = {new BString("%5.4f"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "123456789.9877");
    }

    @Test
    public void testFormatDecimalWithPadding() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BInteger(12345));
        BValue[] args = {new BString("%15d"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "          12345");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*unknown format conversion 'z'.*")
    public void testSprintfInvalidFormatSpecifier() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BString("cow"));
        BValue[] args = {new BString("%z"), fArgs};
        BRunUtil.invoke(compileResult, "testSprintf", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*illegal format conversion 'x != string'.*")
    public void testSprintfIllegalFormatConversion() {
        BValueArray fArgs = new BValueArray(BTypes.typeAny);
        fArgs.add(0, new BString("cow"));
        BValue[] args = {new BString("%x"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
    }

    @Test
    public void testSprintfMix() {
        BValue[] args = {new BString("the %s jumped over the %s, %d times"),
                new BString("cow"), new BString("moon"), new BInteger(2)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintfMix", args);
        Assert.assertEquals(returns[0].stringValue(), "the cow jumped over the moon, 2 times");
    }

    @Test
    public void testSprintfForNilInputString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintfNilString");
        Assert.assertTrue(returns[0].stringValue().isEmpty());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*illegal format conversion 'f != \\(\\)'.*")
    public void testSprintfForNilInputFloat() {
        BRunUtil.invoke(compileResult, "testSprintfNilFloat");
    }

//    @Test(expectedExceptions = BallerinaException.class)
//    public void testGetEnvNonExisting() throws IOException {
//        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
//            System.setOut(new PrintStream(outContent));
//            BValueType[] args = {new BString("PATH2")};
//            BLangFunctions.invokeNew(compileResult, "getEnvVar", args);
//            outContent.toString();
//        } finally {
//            System.setOut(original);
//        }
//    }

//    @Test(expectedExceptions = BLangRuntimeException.class)
//    public void testGetEnvEmptyKey() throws IOException {
//        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
//            System.setOut(new PrintStream(outContent));
//            BValueType[] args = {new BString("")};
//            BLangFunctions.invokeNew(compileResult, "getEnvVar", args);
//            outContent.toString();
//        } finally {
//            System.setOut(original);
//        }
//    }
}
